package dev.cronis.spotify.os.windows;

/**
 * Selects and exposes the active Windows SMTC backend.
 */
final class WindowsSmtcNative {
	private static final WindowsSmtcBackend BACKEND = createBackend();

	private WindowsSmtcNative() {
	}

	static WindowsSmtcBackend backend() {
		return BACKEND;
	}

	static boolean isBackendAvailable() {
		return BACKEND != null;
	}

	private static WindowsSmtcBackend createBackend() {
		NativeLibraryLoader.NativeLoadResult loadResult = NativeLibraryLoader.load("cronis_smtc");
		if (loadResult.loaded()) {
			WindowsSmtcLog.stage("backend_selected", "JNI (" + loadResult.source() + ")");
			return new JniWindowsSmtcBackend();
		}

		WindowsSmtcLog.warn("JNI library unavailable. Falling back to PowerShell WinRT backend. Reason: " + loadResult.error());
		return new PowerShellWindowsSmtcBackend();
	}
}
