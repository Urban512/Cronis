package dev.cronis.spotify;

import java.util.List;
import java.util.Objects;

/**
 * Immutable snapshot of the currently playing Spotify track and player flags.
 *
 * @param id          Spotify track identifier
 * @param title       track title
 * @param artists     contributing artists in display order
 * @param album       parent album metadata
 * @param durationMs  total track duration in milliseconds
 * @param progressMs  current playback position in milliseconds
 * @param playing     whether playback is actively progressing
 * @param shuffle     whether shuffle mode is enabled
 * @param repeat      active repeat mode
 * @param explicit    whether the track is marked explicit
 */
public record SpotifyTrack(
		String id,
		String title,
		List<SpotifyArtist> artists,
		SpotifyAlbum album,
		long durationMs,
		long progressMs,
		boolean playing,
		boolean shuffle,
		SpotifyRepeatMode repeat,
		boolean explicit
) {
	/**
	 * Repeat modes exposed by the Spotify player.
	 */
	public enum SpotifyRepeatMode {
		OFF,
		CONTEXT,
		TRACK
	}

	public SpotifyTrack {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(title, "title");
		Objects.requireNonNull(album, "album");
		Objects.requireNonNull(repeat, "repeat");
		artists = List.copyOf(Objects.requireNonNull(artists, "artists"));

		if (durationMs < 0L) {
			throw new IllegalArgumentException("durationMs must not be negative");
		}
		if (progressMs < 0L) {
			throw new IllegalArgumentException("progressMs must not be negative");
		}
	}

	/**
	 * Returns a formatted artist line suitable for display.
	 *
	 * @return comma-separated artist names
	 */
	public String artistLine() {
		if (artists.isEmpty()) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < artists.size(); i++) {
			if (i > 0) {
				builder.append(", ");
			}
			builder.append(artists.get(i).name());
		}
		return builder.toString();
	}

	/**
	 * Returns normalized playback progress in the range {@code 0.0-1.0}.
	 *
	 * @return progress ratio
	 */
	public float progressRatio() {
		if (durationMs <= 0L) {
			return 0f;
		}
		return Math.min(1f, progressMs / (float) durationMs);
	}
}
