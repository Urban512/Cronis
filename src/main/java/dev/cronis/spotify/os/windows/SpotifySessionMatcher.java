package dev.cronis.spotify.os.windows;

import dev.cronis.spotify.os.MediaSession;

/**
 * Identifies Spotify media sessions exposed through Windows SMTC.
 */
public final class SpotifySessionMatcher {
	private SpotifySessionMatcher() {
	}

	/**
	 * Returns whether the session belongs to the Spotify desktop or store client.
	 *
	 * @param session media session
	 * @return {@code true} when the session is Spotify
	 */
	public static boolean matches(MediaSession session) {
		return session.isSpotify();
	}
}
