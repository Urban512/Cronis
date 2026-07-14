package dev.cronis.spotify.os.windows;

/**
 * JNI implementation of the Windows SMTC backend.
 */
final class JniWindowsSmtcBackend implements WindowsSmtcBackend {
	private String lastError = "";

	@Override
	public String backendName() {
		return "JNI";
	}

	@Override
	public boolean initialize() {
		WindowsSmtcLog.stage("native_initialize", "Calling nativeInitialize()");
		try {
			boolean initialized = WindowsSmtcNativeJni.nativeInitialize();
			if (initialized) {
				WindowsSmtcLog.stage("winrt_initialized", "WinRT apartment and session manager acquired");
				lastError = "";
				return true;
			}

			lastError = safeError(WindowsSmtcNativeJni.nativeGetLastError());
			WindowsSmtcLog.failure("native_initialize", lastError.isBlank() ? "nativeInitialize() returned false" : lastError);
			return false;
		} catch (UnsatisfiedLinkError exception) {
			lastError = exception.getMessage();
			WindowsSmtcLog.failure("native_initialize", lastError);
			return false;
		}
	}

	@Override
	public void shutdown() {
		try {
			WindowsSmtcNativeJni.nativeShutdown();
		} catch (UnsatisfiedLinkError exception) {
			WindowsSmtcLog.warn("nativeShutdown() failed", exception);
		}
	}

	@Override
	public boolean synchronize() {
		try {
			boolean synchronizedState = WindowsSmtcNativeJni.nativeSynchronize();
			if (!synchronizedState) {
				lastError = safeError(WindowsSmtcNativeJni.nativeGetLastError());
				if (!lastError.isBlank()) {
					WindowsSmtcLog.stage("native_synchronize", lastError);
				}
			} else {
				lastError = "";
				WindowsSmtcLog.stage(
						"spotify_metadata",
						"title='" + safeString(WindowsSmtcNativeJni.nativeGetTitle()) + "', artist='" + safeString(WindowsSmtcNativeJni.nativeGetArtist()) + "'"
				);
			}
			return synchronizedState;
		} catch (UnsatisfiedLinkError exception) {
			lastError = exception.getMessage();
			WindowsSmtcLog.failure("native_synchronize", lastError);
			return false;
		}
	}

	@Override
	public boolean hasActiveSession() {
		return WindowsSmtcNativeJni.nativeHasActiveSession();
	}

	@Override
	public String getSessionId() {
		return safeString(WindowsSmtcNativeJni.nativeGetSessionId());
	}

	@Override
	public String getApplicationId() {
		return safeString(WindowsSmtcNativeJni.nativeGetApplicationId());
	}

	@Override
	public String getApplicationName() {
		return safeString(WindowsSmtcNativeJni.nativeGetApplicationName());
	}

	@Override
	public String getTitle() {
		return safeString(WindowsSmtcNativeJni.nativeGetTitle());
	}

	@Override
	public String getArtist() {
		return safeString(WindowsSmtcNativeJni.nativeGetArtist());
	}

	@Override
	public String getAlbumTitle() {
		return safeString(WindowsSmtcNativeJni.nativeGetAlbumTitle());
	}

	@Override
	public String getAlbumArtist() {
		return safeString(WindowsSmtcNativeJni.nativeGetAlbumArtist());
	}

	@Override
	public int getPlaybackStatus() {
		return WindowsSmtcNativeJni.nativeGetPlaybackStatus();
	}

	@Override
	public long getPositionMs() {
		return WindowsSmtcNativeJni.nativeGetPositionMs();
	}

	@Override
	public long getDurationMs() {
		return WindowsSmtcNativeJni.nativeGetDurationMs();
	}

	@Override
	public String getLastError() {
		return lastError;
	}

	private static String safeString(String value) {
		return value == null ? "" : value;
	}

	private static String safeError(String value) {
		return value == null ? "" : value.trim();
	}
}
