package dev.cronis.spotify;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Distributes Spotify playback updates to registered listeners.
 */
final class SpotifyStateBroadcaster {
	private final List<SpotifyStateListener> listeners = new CopyOnWriteArrayList<>();

	void addListener(SpotifyStateListener listener) {
		listeners.add(Objects.requireNonNull(listener, "listener"));
	}

	boolean removeListener(SpotifyStateListener listener) {
		return listeners.remove(listener);
	}

	void broadcast(SpotifyManager.SpotifySnapshot snapshot) {
		for (SpotifyStateListener listener : listeners) {
			listener.onPlaybackUpdated(
					snapshot.track(),
					snapshot.playbackState(),
					snapshot.sessionState()
			);
		}
	}
}
