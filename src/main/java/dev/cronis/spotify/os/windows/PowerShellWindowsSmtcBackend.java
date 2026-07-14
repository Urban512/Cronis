package dev.cronis.spotify.os.windows;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * PowerShell WinRT fallback used when the JNI library is unavailable.
 */
final class PowerShellWindowsSmtcBackend implements WindowsSmtcBackend {
	private static final String SCRIPT_RESOURCE = "/scripts/windows-smtc-query.ps1";
	private static final long SCRIPT_TIMEOUT_SECONDS = 10L;

	private final Map<String, String> state = new HashMap<>();
	private Path scriptPath;
	private String lastError = "";
	private boolean initialized;

	@Override
	public String backendName() {
		return "PowerShell";
	}

	@Override
	public boolean initialize() {
		WindowsSmtcLog.stage("powershell_initialize", "Preparing WinRT query script");

		try {
			scriptPath = extractScript();
			WindowsSmtcLog.stage("powershell_script_ready", scriptPath.toString());

			ScriptResult result = runScript(true);
			if (!result.success()) {
				lastError = result.errorMessage();
				WindowsSmtcLog.failure("powershell_initialize", lastError);
				return false;
			}

			if (!"winrt_initialized".equals(result.stage())) {
				lastError = "Unexpected probe stage: " + result.stage();
				WindowsSmtcLog.failure("powershell_initialize", lastError);
				return false;
			}

			initialized = true;
			lastError = "";
			WindowsSmtcLog.stage("powershell_initialized", "WinRT accessible through PowerShell");
			return true;
		} catch (IOException | InterruptedException exception) {
			if (exception instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			lastError = exception.getMessage();
			WindowsSmtcLog.failure("powershell_initialize", lastError);
			return false;
		}
	}

	@Override
	public void shutdown() {
		initialized = false;
		state.clear();
	}

	@Override
	public boolean synchronize() {
		if (!initialized) {
			lastError = "PowerShell backend is not initialized";
			return false;
		}

		try {
			ScriptResult result = runScript(false);
			state.clear();

			if (!result.success()) {
				lastError = result.errorMessage();
				WindowsSmtcLog.failure("powershell_synchronize", lastError);
				return false;
			}

			state.putAll(result.values());
			String stage = result.stage();
			WindowsSmtcLog.stage("powershell_synchronize", stage);

			return switch (stage) {
				case "spotify_session" -> {
					lastError = "";
					WindowsSmtcLog.stage(
							"spotify_metadata",
							"title='" + state.getOrDefault("title", "") + "', artist='" + state.getOrDefault("artist", "") + "'"
					);
					yield true;
				}
				case "no_current_session" -> {
					lastError = "No current Windows media session";
					yield false;
				}
				case "not_spotify" -> {
					lastError = "Current session is not Spotify: " + state.getOrDefault("app_id", "unknown");
					yield false;
				}
				case "metadata_empty" -> {
					lastError = "Spotify session has no track title";
					yield false;
				}
				default -> {
					lastError = "Unexpected synchronization stage: " + stage;
					yield false;
				}
			};
		} catch (IOException | InterruptedException exception) {
			if (exception instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			lastError = exception.getMessage();
			WindowsSmtcLog.failure("powershell_synchronize", lastError);
			return false;
		}
	}

	@Override
	public boolean hasActiveSession() {
		return "spotify_session".equals(state.get("stage"));
	}

	@Override
	public String getSessionId() {
		return state.getOrDefault("session_id", "");
	}

	@Override
	public String getApplicationId() {
		return state.getOrDefault("app_id", "");
	}

	@Override
	public String getApplicationName() {
		return state.getOrDefault("app_name", "");
	}

	@Override
	public String getTitle() {
		return state.getOrDefault("title", "");
	}

	@Override
	public String getArtist() {
		return state.getOrDefault("artist", "");
	}

	@Override
	public String getAlbumTitle() {
		return state.getOrDefault("album_title", "");
	}

	@Override
	public String getAlbumArtist() {
		return state.getOrDefault("album_artist", "");
	}

	@Override
	public int getPlaybackStatus() {
		return parseInt(state.get("playback_status"), 3);
	}

	@Override
	public long getPositionMs() {
		return parseLong(state.get("position_ms"), 0L);
	}

	@Override
	public long getDurationMs() {
		return parseLong(state.get("duration_ms"), 0L);
	}

	@Override
	public String getLastError() {
		return lastError;
	}

	private Path extractScript() throws IOException {
		try (InputStream input = PowerShellWindowsSmtcBackend.class.getResourceAsStream(SCRIPT_RESOURCE)) {
			if (input == null) {
				throw new IOException("Script resource not found: " + SCRIPT_RESOURCE);
			}

			Path tempDirectory = Files.createTempDirectory("cronis-smtc-script-");
			tempDirectory.toFile().deleteOnExit();
			Path script = tempDirectory.resolve("windows-smtc-query.ps1");
			Files.copy(input, script, StandardCopyOption.REPLACE_EXISTING);
			return script;
		}
	}

	private ScriptResult runScript(boolean probe) throws IOException, InterruptedException {
		ProcessBuilder builder = new ProcessBuilder(
				"powershell.exe",
				"-NoProfile",
				"-ExecutionPolicy",
				"Bypass",
				"-File",
				scriptPath.toString()
		);

		if (probe) {
			builder.command().add("-Probe");
		}

		builder.redirectErrorStream(true);
		Process process = builder.start();

		String output;
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
			output = reader.lines().reduce((left, right) -> left + System.lineSeparator() + right).orElse("");
		}

		boolean finished = process.waitFor(SCRIPT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		if (!finished) {
			process.destroyForcibly();
			return ScriptResult.failure("PowerShell script timed out after " + SCRIPT_TIMEOUT_SECONDS + " seconds");
		}

		int exitCode = process.exitValue();
		Map<String, String> values = parseOutput(output);
		String stage = values.getOrDefault("stage", "unknown");
		String detail = values.getOrDefault("detail", "");

		if (exitCode != 0) {
			String message = detail.isBlank() ? "PowerShell script failed with exit code " + exitCode : detail;
			return ScriptResult.failure(message);
		}

		return new ScriptResult(true, stage, detail, values, "");
	}

	private static Map<String, String> parseOutput(String output) {
		Map<String, String> values = new HashMap<>();
		for (String line : output.split("\\R")) {
			String trimmed = line.trim();
			if (trimmed.isEmpty()) {
				continue;
			}

			int separator = trimmed.indexOf('=');
			if (separator <= 0) {
				continue;
			}

			String key = trimmed.substring(0, separator).trim();
			String value = trimmed.substring(separator + 1).trim();
			values.put(key, value);
		}
		return values;
	}

	private static int parseInt(String value, int fallback) {
		if (value == null || value.isBlank()) {
			return fallback;
		}

		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException ignored) {
			return fallback;
		}
	}

	private static long parseLong(String value, long fallback) {
		if (value == null || value.isBlank()) {
			return fallback;
		}

		try {
			return Long.parseLong(value.trim());
		} catch (NumberFormatException ignored) {
			return fallback;
		}
	}

	private record ScriptResult(boolean success, String stage, String detail, Map<String, String> values, String errorMessage) {
		static ScriptResult failure(String message) {
			return new ScriptResult(false, "error", message, Map.of(), message);
		}
	}
}
