package dev.cronis.spotify;

import java.util.Objects;
import java.util.Optional;

/**
 * Provider hook for future Spotify Web API integration.
 * <p>
 * Implementations perform network I/O on the manager's background thread and
 * must never block the Minecraft render thread.
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
	 * Returns a provider that reports a disconnected session without network I/O.
	 *
	 * @return unavailable provider
	 */
	static SpotifyPlaybackProvider unavailable() {
		return session -> SpotifyPlaybackUpdate.disconnected();
	}
}
