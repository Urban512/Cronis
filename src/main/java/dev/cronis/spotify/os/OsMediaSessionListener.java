package dev.cronis.spotify.os;

/**
 * Receives notifications when a platform media session changes.
 */
@FunctionalInterface
public interface OsMediaSessionListener {
	/**
	 * Called when session metadata, transport state, or timeline information changes.
	 */
	void onSessionChanged();
}
