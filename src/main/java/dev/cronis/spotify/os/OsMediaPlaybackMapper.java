package dev.cronis.spotify.os;

import dev.cronis.spotify.SpotifyAlbum;
import dev.cronis.spotify.SpotifyArtist;
import dev.cronis.spotify.SpotifyPlaybackState;
import dev.cronis.spotify.SpotifyPlaybackUpdate;
import dev.cronis.spotify.SpotifySession;
import dev.cronis.spotify.SpotifyTrack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Maps OS media snapshots into Spotify domain models consumed by widgets.
 */
final class OsMediaPlaybackMapper {
	private OsMediaPlaybackMapper() {
	}

	static SpotifyPlaybackUpdate toPlaybackUpdate(OsMediaPlaybackSnapshot snapshot) {
		if (!snapshot.isAvailable()) {
			return SpotifyPlaybackUpdate.disconnected();
		}

		MediaSession session = snapshot.session().orElseThrow();
		MediaMetadata metadata = snapshot.metadata().orElseThrow();
		SpotifyTrack track = toTrack(snapshot, metadata);

		return new SpotifyPlaybackUpdate(
				Optional.of(track),
				snapshot.playbackState(),
				SpotifySession.State.CONNECTED,
				"",
				session.applicationName()
		);
	}

	private static SpotifyTrack toTrack(OsMediaPlaybackSnapshot snapshot, MediaMetadata metadata) {
		long durationMs = Math.max(metadata.durationMs(), snapshot.positionMs());
		boolean playing = snapshot.playbackState() == SpotifyPlaybackState.PLAYING
				|| snapshot.playbackState() == SpotifyPlaybackState.BUFFERING;

		return new SpotifyTrack(
				resolveTrackId(metadata),
				metadata.title(),
				toArtists(metadata.artist()),
				new SpotifyAlbum(metadata.albumTitle(), metadata.artworkUri(), ""),
				durationMs,
				snapshot.positionMs(),
				playing,
				snapshot.shuffle(),
				snapshot.repeat(),
				metadata.explicit()
		);
	}

	private static String resolveTrackId(MediaMetadata metadata) {
		if (!metadata.trackId().isEmpty()) {
			return metadata.trackId();
		}

		int hash = 17;
		hash = 31 * hash + metadata.title().hashCode();
		hash = 31 * hash + metadata.artist().hashCode();
		hash = 31 * hash + metadata.albumTitle().hashCode();
		return "os-media:" + Integer.toUnsignedString(hash);
	}

	private static List<SpotifyArtist> toArtists(String artistLine) {
		if (artistLine.isEmpty()) {
			return List.of();
		}

		String[] parts = artistLine.split(",");
		List<SpotifyArtist> artists = new ArrayList<>(parts.length);
		for (String part : parts) {
			String name = part.trim();
			if (name.isEmpty()) {
				continue;
			}
			String id = "artist:" + name.toLowerCase(Locale.ROOT);
			artists.add(new SpotifyArtist(id, name));
		}
		return List.copyOf(artists);
	}
}
