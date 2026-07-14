package dev.cronis.spotify;

import java.util.Objects;
import java.util.Optional;

/**
 * Result of a single Spotify playback fetch.
 *
 * @param track          optional current track
 * @param playbackState  player transport state
 * @param sessionState   session state
 * @param accountId      active account id
 * @param displayName    active account display name
 */
public record SpotifyPlaybackUpdate(
		Optional<SpotifyTrack> track,
		SpotifyPlaybackState playbackState,
		SpotifySession.State sessionState,
		String accountId,
		String displayName
) {
	/**
	 * Returns an update representing a disconnected session.
	 *
	 * @return disconnected update
	 */
	public static SpotifyPlaybackUpdate disconnected() {
		return new SpotifyPlaybackUpdate(
				Optional.empty(),
				SpotifyPlaybackState.STOPPED,
				SpotifySession.State.DISCONNECTED,
				"",
				""
		);
	}

	public SpotifyPlaybackUpdate {
		track = Objects.requireNonNullElse(track, Optional.empty());
		playbackState = Objects.requireNonNull(playbackState, "playbackState");
		sessionState = Objects.requireNonNull(sessionState, "sessionState");
		accountId = Objects.requireNonNullElse(accountId, "");
		displayName = Objects.requireNonNullElse(displayName, "");
	}
}
