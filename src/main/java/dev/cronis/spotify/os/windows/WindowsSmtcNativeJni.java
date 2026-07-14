package dev.cronis.spotify.os.windows;

/**
 * Low-level JNI bindings. This class is only referenced after the native library loads.
 */
final class WindowsSmtcNativeJni {
	private WindowsSmtcNativeJni() {
	}

	static native boolean nativeInitialize();

	static native void nativeShutdown();

	static native boolean nativeSynchronize();

	static native boolean nativeHasActiveSession();

	static native String nativeGetSessionId();

	static native String nativeGetApplicationId();

	static native String nativeGetApplicationName();

	static native String nativeGetTitle();

	static native String nativeGetArtist();

	static native String nativeGetAlbumTitle();

	static native String nativeGetAlbumArtist();

	static native int nativeGetPlaybackStatus();

	static native long nativeGetPositionMs();

	static native long nativeGetDurationMs();

	static native String nativeGetLastError();
}
