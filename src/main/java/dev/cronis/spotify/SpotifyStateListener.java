package dev.cronis.spotify;

import java.util.Optional;

/**
 * Receives Spotify playback updates from {@link SpotifyService}.
 */
@FunctionalInterface
public interface SpotifyStateListener {
	/**
	 * Called when cached playback information changes meaningfully.
	 *
	 * @param track         current track, if any
	 * @param playbackState player transport state
	 * @param sessionState  session state at fetch time
	 */
	void onPlaybackUpdated(
			Optional<SpotifyTrack> track,
			SpotifyPlaybackState playbackState,
			SpotifySession.State sessionState
	);
}
