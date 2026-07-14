package dev.cronis.spotify.os;

import java.util.Objects;

/**
 * Immutable metadata supplied by an operating-system media session.
 *
 * @param trackId      optional stable track identifier when provided by the platform
 * @param title        track title
 * @param artist       primary artist line
 * @param albumTitle   album title
 * @param albumArtist  album artist when available
 * @param durationMs   total duration in milliseconds
 * @param artworkUri   optional artwork location supplied by the platform
 * @param explicit     whether the track is explicit; {@code false} when unknown
 */
public record MediaMetadata(
		String trackId,
		String title,
		String artist,
		String albumTitle,
		String albumArtist,
		long durationMs,
		String artworkUri,
		boolean explicit
) {
	public MediaMetadata {
		trackId = Objects.requireNonNullElse(trackId, "");
		title = Objects.requireNonNullElse(title, "");
		artist = Objects.requireNonNullElse(artist, "");
		albumTitle = Objects.requireNonNullElse(albumTitle, "");
		albumArtist = Objects.requireNonNullElse(albumArtist, "");
		artworkUri = Objects.requireNonNullElse(artworkUri, "");
	}

	/**
	 * Returns whether meaningful track metadata is present.
	 *
	 * @return {@code true} when a title is available
	 */
	public boolean hasTrack() {
		return !title.isEmpty();
	}
}
