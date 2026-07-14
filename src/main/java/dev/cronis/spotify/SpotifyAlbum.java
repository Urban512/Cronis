package dev.cronis.spotify;

import java.util.Objects;

/**
 * Identifies a Spotify album associated with the current track.
 *
 * @param name        album title
 * @param imageUrl    cover image URL; empty when artwork is unavailable
 * @param releaseDate release date string as provided by Spotify
 */
public record SpotifyAlbum(String name, String imageUrl, String releaseDate) {
	public SpotifyAlbum {
		Objects.requireNonNull(name, "name");
		imageUrl = Objects.requireNonNullElse(imageUrl, "");
		releaseDate = Objects.requireNonNullElse(releaseDate, "");
	}

	/**
	 * Returns whether album artwork metadata is available.
	 *
	 * @return {@code true} when an image URL is present
	 */
	public boolean hasArtwork() {
		return !imageUrl.isEmpty();
	}
}
