package dev.cronis.spotify;

import java.util.Objects;
import java.util.Optional;

/**
 * Provider hook for Spotify playback integration.
 * <p>
 * Implementations perform work on the manager's background thread and must
 * never block the Minecraft render thread.
 */
@FunctionalInterface
public interface SpotifyPlaybackProvider {
	/**
	 * Fetches the latest playback snapshot.
	 *
	 * @param session active session state
	 * @return playback update
	 */
	SpotifyPlaybackUpdate fetch(SpotifySession session);

	/**
	 * Called when the Spotify manager starts its background lifecycle.
	 */
	default void onManagerStarted() {
	}

	/**
	 * Called when the Spotify manager stops its background lifecycle.
	 */
	default void onManagerStopped() {
	}

	/**
	 * Returns a provider that reports a disconnected session without network I/O.
	 *
	 * @return unavailable provider
	 */
	static SpotifyPlaybackProvider unavailable() {
		return session -> SpotifyPlaybackUpdate.disconnected();
	}
}
