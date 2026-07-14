package dev.cronis.spotify;

import java.util.Objects;
import java.util.Optional;

/**
 * Public entry point for Spotify playback information.
 * <p>
 * Future widgets and modules must depend on this class only. Rendering, GUI,
 * authentication and HTTP transport remain outside this API.
 */
public final class SpotifyService {
	private static final SpotifyService INSTANCE = new SpotifyService();

	private final SpotifyStateBroadcaster broadcaster = new SpotifyStateBroadcaster();
	private final SpotifyManager manager = new SpotifyManager(broadcaster);

	private SpotifyService() {
	}

	/**
	 * Returns the shared Spotify service instance.
	 *
	 * @return Spotify service
	 */
	public static SpotifyService get() {
		return INSTANCE;
	}

	/**
	 * Starts background polling when a provider is configured.
	 * <p>
	 * The default provider reads playback state from operating-system media
	 * transport APIs without requiring Spotify authentication.
	 */
	public void start() {
		manager.start();
	}

	/**
	 * Stops polling and clears cached playback state.
	 */
	public void stop() {
		manager.stop();
	}

	/**
	 * Shuts down background workers. Intended for client shutdown.
	 */
	public void shutdown() {
		manager.shutdown();
	}

	/**
	 * Returns whether the service is actively polling.
	 *
	 * @return {@code true} when running
	 */
	public boolean isRunning() {
		return manager.isRunning();
	}

	/**
	 * Returns the active Spotify session.
	 *
	 * @return session state
	 */
	public SpotifySession getSession() {
		return manager.session();
	}

	/**
	 * Returns the cached current track, if any.
	 * <p>
	 * This method is safe to call from the render thread. It never performs
	 * network I/O and never blocks.
	 *
	 * @return optional current track
	 */
	public Optional<SpotifyTrack> getCurrentTrack() {
		return manager.currentTrack();
	}

	/**
	 * Returns the cached player transport state.
	 *
	 * @return playback state
	 */
	public SpotifyPlaybackState getPlaybackState() {
		return manager.playbackState();
	}

	/**
	 * Registers a playback listener.
	 *
	 * @param listener state listener
	 */
	public void addListener(SpotifyStateListener listener) {
		broadcaster.addListener(listener);
	}

	/**
	 * Removes a previously registered playback listener.
	 *
	 * @param listener state listener
	 * @return {@code true} when the listener was removed
	 */
	public boolean removeListener(SpotifyStateListener listener) {
		return broadcaster.removeListener(listener);
	}

	/**
	 * Installs the playback provider used for future Spotify API integration.
	 * <p>
	 * Network requests must be executed on the manager's background thread via
	 * {@link SpotifyPlaybackProvider#fetch(SpotifySession)}.
	 *
	 * @param provider playback provider
	 */
	public void setPlaybackProvider(SpotifyPlaybackProvider provider) {
		manager.setProvider(provider);
	}
}
