package dev.cronis.spotify.os;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

final class UnavailablePlatformProvider implements OsMediaPlatformProvider {
	static final UnavailablePlatformProvider INSTANCE = new UnavailablePlatformProvider();

	private final List<OsMediaSessionListener> listeners = new CopyOnWriteArrayList<>();

	private UnavailablePlatformProvider() {
	}

	@Override
	public void start() {
	}

	@Override
	public void stop() {
	}

	@Override
	public void refresh() {
	}

	@Override
	public OsMediaPlaybackSnapshot captureSnapshot() {
		return OsMediaPlaybackSnapshot.empty();
	}

	@Override
	public void addListener(OsMediaSessionListener listener) {
		listeners.add(listener);
	}

	@Override
	public boolean removeListener(OsMediaSessionListener listener) {
		return listeners.remove(listener);
	}
}
