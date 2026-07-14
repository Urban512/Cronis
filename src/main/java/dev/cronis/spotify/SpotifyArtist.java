package dev.cronis.spotify;

import java.util.Objects;

/**
 * Identifies a Spotify artist.
 *
 * @param id   Spotify artist identifier
 * @param name display name
 */
public record SpotifyArtist(String id, String name) {
	public SpotifyArtist {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(name, "name");
	}
}
