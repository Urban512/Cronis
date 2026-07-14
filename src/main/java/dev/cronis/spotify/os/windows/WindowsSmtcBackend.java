package dev.cronis.spotify.os.windows;

/**
 * Backend contract for reading Windows SMTC state.
 */
interface WindowsSmtcBackend {
	String backendName();

	boolean initialize();

	void shutdown();

	boolean synchronize();

	boolean hasActiveSession();

	String getSessionId();

	String getApplicationId();

	String getApplicationName();

	String getTitle();

	String getArtist();

	String getAlbumTitle();

	String getAlbumArtist();

	int getPlaybackStatus();

	long getPositionMs();

	long getDurationMs();

	String getLastError();
}
