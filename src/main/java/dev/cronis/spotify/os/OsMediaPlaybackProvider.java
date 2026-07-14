package dev.cronis.spotify.os;

import dev.cronis.spotify.SpotifyPlaybackProvider;
import dev.cronis.spotify.SpotifyPlaybackUpdate;
import dev.cronis.spotify.SpotifySession;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Default playback provider that reads Spotify state from operating-system media
 * transport APIs without OAuth or network access.
 * <p>
 * This provider integrates with {@link SpotifyPlaybackProvider} and maps OS
 * media snapshots into the existing Spotify domain models consumed by widgets.
 */
public final class OsMediaPlaybackProvider implements SpotifyPlaybackProvider {
	private static final OsMediaPlaybackProvider INSTANCE = new OsMediaPlaybackProvider(OsMediaPlatformProvider.createForCurrentPlatform());

	private final OsMediaPlatformProvider platform;
	private final AtomicReference<OsMediaPlaybackSnapshot> cache = new AtomicReference<>(OsMediaPlaybackSnapshot.empty());
	private final OsMediaSessionListener cacheListener = this::refreshCache;

	private volatile boolean active;

	OsMediaPlaybackProvider(OsMediaPlatformProvider platform) {
		this.platform = Objects.requireNonNull(platform, "platform");
		this.platform.addListener(cacheListener);
	}

	/**
	 * Returns the shared OS media playback provider.
	 *
	 * @return provider instance
	 */
	public static OsMediaPlaybackProvider get() {
		return INSTANCE;
	}

	/**
	 * Starts platform listeners when the Spotify manager begins polling.
	 */
	@Override
	public void onManagerStarted() {
		if (active) {
			return;
		}

		active = true;
		platform.start();
		refreshCache();
	}

	/**
	 * Stops platform listeners when the Spotify manager shuts down.
	 */
	@Override
	public void onManagerStopped() {
		if (!active) {
			return;
		}

		active = false;
		platform.stop();
		cache.set(OsMediaPlaybackSnapshot.empty());
	}

	@Override
	public SpotifyPlaybackUpdate fetch(SpotifySession session) {
		Objects.requireNonNull(session, "session");

		if (!active) {
			return SpotifyPlaybackUpdate.disconnected();
		}

		platform.refresh();
		refreshCache();
		return OsMediaPlaybackMapper.toPlaybackUpdate(cache.get());
	}

	private void refreshCache() {
		cache.set(platform.captureSnapshot());
	}
}
