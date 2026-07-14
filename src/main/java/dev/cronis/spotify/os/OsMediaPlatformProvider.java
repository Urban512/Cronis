package dev.cronis.spotify.os;

/**
 * Platform-specific bridge for operating-system media transport APIs.
 * <p>
 * Windows implements this with SMTC. Future Linux and macOS providers will
 * plug into the same contract without changing {@link OsMediaPlaybackProvider}.
 */
public interface OsMediaPlatformProvider {
	/**
	 * Starts platform listeners and session discovery.
	 */
	void start();

	/**
	 * Stops platform listeners and releases resources.
	 */
	void stop();

	/**
	 * Performs a watchdog synchronization with the platform.
	 * <p>
	 * Event-driven updates remain the primary path. Refresh exists so the manager
	 * can recover from missed callbacks or temporarily unavailable native bridges.
	 */
	void refresh();

	/**
	 * Captures the latest immutable playback snapshot.
	 *
	 * @return current snapshot
	 */
	OsMediaPlaybackSnapshot captureSnapshot();

	/**
	 * Registers a listener for session changes.
	 *
	 * @param listener change listener
	 */
	void addListener(OsMediaSessionListener listener);

	/**
	 * Removes a previously registered listener.
	 *
	 * @param listener change listener
	 * @return {@code true} when the listener was removed
	 */
	boolean removeListener(OsMediaSessionListener listener);

	/**
	 * Creates the provider for the current operating system.
	 *
	 * @return platform provider
	 */
	static OsMediaPlatformProvider createForCurrentPlatform() {
		String osName = System.getProperty("os.name", "").toLowerCase();
		if (osName.contains("win")) {
			return new dev.cronis.spotify.os.windows.WindowsMediaProvider();
		}
		return unavailable();
	}

	/**
	 * Returns a no-op provider used on unsupported platforms until native
	 * backends are implemented.
	 *
	 * @return unavailable provider
	 */
	static OsMediaPlatformProvider unavailable() {
		return UnavailablePlatformProvider.INSTANCE;
	}
}
