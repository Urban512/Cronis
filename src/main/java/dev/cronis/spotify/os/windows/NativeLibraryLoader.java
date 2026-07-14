package dev.cronis.spotify.os.windows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the Windows SMTC native library and reports the exact load result.
 */
final class NativeLibraryLoader {
	private static final String NATIVE_RESOURCE_DIR = "/natives/windows-amd64/";
	private static final String LIBRARY_NAME = "cronis_smtc";

	private NativeLibraryLoader() {
	}

	static NativeLoadResult load(String libraryName) {
		WindowsSmtcLog.stage("native_library_load", "Attempting to load '" + libraryName + "'");

		try {
			System.loadLibrary(libraryName);
			WindowsSmtcLog.stage("native_library_loaded", "Loaded via System.loadLibrary");
			return NativeLoadResult.success("System.loadLibrary");
		} catch (UnsatisfiedLinkError systemLibraryError) {
			WindowsSmtcLog.stage("native_library_system_path", "System.loadLibrary failed: " + systemLibraryError.getMessage());
		}

		String fileName = System.mapLibraryName(libraryName);
		List<String> attemptedPaths = new ArrayList<>();

		for (String candidate : candidatePaths(fileName)) {
			attemptedPaths.add(candidate);
			NativeLoadResult resourceResult = tryLoadPath(candidate, "classpath:" + candidate);
			if (resourceResult.loaded()) {
				return resourceResult;
			}

			NativeLoadResult fileResult = tryLoadFile(candidate, fileName);
			if (fileResult.loaded()) {
				return fileResult;
			}
		}

		String reason = "Native library '" + fileName + "' was not found. Attempted: " + String.join(", ", attemptedPaths);
		WindowsSmtcLog.failure("native_library_load", reason);
		return NativeLoadResult.failure(reason);
	}

	private static List<String> candidatePaths(String fileName) {
		return List.of(
				NATIVE_RESOURCE_DIR + fileName,
				"/natives/windows-x86_64/" + fileName,
				"/natives/windows/" + fileName
		);
	}

	private static NativeLoadResult tryLoadPath(String resourcePath, String description) {
		try (var input = NativeLibraryLoader.class.getResourceAsStream(resourcePath)) {
			if (input == null) {
				WindowsSmtcLog.stage("native_library_resource_missing", description);
				return NativeLoadResult.failure("Resource missing: " + description);
			}

			Path tempDirectory = Files.createTempDirectory("cronis-natives-");
			tempDirectory.toFile().deleteOnExit();
			Path libraryPath = tempDirectory.resolve(System.mapLibraryName(LIBRARY_NAME));
			Files.copy(input, libraryPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
			System.load(libraryPath.toAbsolutePath().toString());
			WindowsSmtcLog.stage("native_library_loaded", "Loaded from extracted resource " + description);
			return NativeLoadResult.success(description);
		} catch (Exception exception) {
			WindowsSmtcLog.failure("native_library_resource_load", description + " — " + exception.getMessage());
			return NativeLoadResult.failure(exception.getMessage());
		}
	}

	private static NativeLoadResult tryLoadFile(String resourcePath, String fileName) {
		String relative = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
		List<Path> fileCandidates = List.of(
				Path.of("build", "native-output", relative),
				Path.of("build", "resources", "main", relative),
				Path.of("native", "windows", "smtc", "build", "Release", fileName),
				Path.of("native", "windows", "smtc", "build", fileName)
		);

		for (Path candidate : fileCandidates) {
			if (!Files.exists(candidate)) {
				continue;
			}

			try {
				System.load(candidate.toAbsolutePath().toString());
				WindowsSmtcLog.stage("native_library_loaded", "Loaded from filesystem path " + candidate);
				return NativeLoadResult.success(candidate.toString());
			} catch (UnsatisfiedLinkError exception) {
				WindowsSmtcLog.failure("native_library_file_load", candidate + " — " + exception.getMessage());
			}
		}

		return NativeLoadResult.failure("No filesystem candidate for " + resourcePath);
	}

	record NativeLoadResult(boolean loaded, String source, String error) {
		static NativeLoadResult success(String source) {
			return new NativeLoadResult(true, source, "");
		}

		static NativeLoadResult failure(String error) {
			return new NativeLoadResult(false, "", error);
		}
	}
}
