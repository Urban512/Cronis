package dev.cronis.spotify.os;

import dev.cronis.spotify.SpotifyPlaybackState;
import dev.cronis.spotify.SpotifyTrack;

import java.util.Objects;
import java.util.Optional;

/**
 * Immutable playback snapshot captured from an OS media session.
 *
 * @param session        active media session, if any
 * @param metadata       current metadata, if any
 * @param playbackState  transport state
 * @param shuffle        shuffle flag reported by the platform
 * @param repeat         repeat mode reported by the platform
 * @param positionMs     resolved position in milliseconds
 * @param capturedAtMs   capture timestamp in epoch milliseconds
 */
public record OsMediaPlaybackSnapshot(
		Optional<MediaSession> session,
		Optional<MediaMetadata> metadata,
		SpotifyPlaybackState playbackState,
		boolean shuffle,
		SpotifyTrack.SpotifyRepeatMode repeat,
		long positionMs,
		long capturedAtMs
) {
	public OsMediaPlaybackSnapshot {
		session = Objects.requireNonNullElse(session, Optional.empty());
		metadata = Objects.requireNonNullElse(metadata, Optional.empty());
		playbackState = Objects.requireNonNull(playbackState, "playbackState");
		repeat = Objects.requireNonNull(repeat, "repeat");
	}

	/**
	 * Returns an empty snapshot representing no active Spotify session.
	 *
	 * @return empty snapshot
	 */
	public static OsMediaPlaybackSnapshot empty() {
		return new OsMediaPlaybackSnapshot(
				Optional.empty(),
				Optional.empty(),
				SpotifyPlaybackState.STOPPED,
				false,
				SpotifyTrack.SpotifyRepeatMode.OFF,
				0L,
				0L
		);
	}

	/**
	 * Returns whether Spotify playback metadata is available.
	 *
	 * @return {@code true} when a Spotify session with track metadata is present
	 */
	public boolean isAvailable() {
		return session.filter(MediaSession::isSpotify).isPresent()
				&& metadata.filter(MediaMetadata::hasTrack).isPresent();
	}
}
