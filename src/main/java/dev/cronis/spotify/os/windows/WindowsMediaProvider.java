package dev.cronis.spotify.os.windows;



import dev.cronis.Cronis;

import dev.cronis.spotify.SpotifyPlaybackState;

import dev.cronis.spotify.SpotifyTrack;

import dev.cronis.spotify.os.MediaMetadata;

import dev.cronis.spotify.os.MediaSession;

import dev.cronis.spotify.os.OsMediaPlaybackSnapshot;

import dev.cronis.spotify.os.OsMediaPlatformProvider;

import dev.cronis.spotify.os.OsMediaSessionListener;

import dev.cronis.spotify.os.PlaybackPositionTracker;



import java.util.List;

import java.util.Objects;

import java.util.Optional;

import java.util.concurrent.CopyOnWriteArrayList;

import java.util.concurrent.atomic.AtomicReference;



/**

 * Windows implementation of {@link OsMediaPlatformProvider} backed by SMTC.

 * <p>

 * A native WinRT bridge reads {@code GlobalSystemMediaTransportControlsSession}

 * data and exposes it to Java through {@link WindowsSmtcBridge}.

 */

public final class WindowsMediaProvider implements OsMediaPlatformProvider {

	private final List<OsMediaSessionListener> listeners = new CopyOnWriteArrayList<>();

	private final PlaybackPositionTracker positionTracker = new PlaybackPositionTracker();

	private final WindowsSmtcBridge smtcBridge = new WindowsSmtcBridge();

	private final AtomicReference<MediaSession> activeSession = new AtomicReference<>();

	private final AtomicReference<MediaMetadata> activeMetadata = new AtomicReference<>();

	private final AtomicReference<SpotifyPlaybackState> playbackState = new AtomicReference<>(SpotifyPlaybackState.STOPPED);

	private final AtomicReference<SpotifyTrack.SpotifyRepeatMode> repeatMode =

			new AtomicReference<>(SpotifyTrack.SpotifyRepeatMode.OFF);



	private volatile boolean shuffleEnabled;

	private volatile boolean running;

	private volatile boolean nativeAvailable;



	@Override

	public void start() {

		if (running) {

			return;

		}



		running = true;
		WindowsSmtcLog.info("Starting Windows media provider");

		nativeAvailable = smtcBridge.initialize();
		if (!nativeAvailable) {
			WindowsSmtcLog.failure(
					"provider_initialize",
					"Backend '" + smtcBridge.getBackendName() + "' failed to initialize: " + smtcBridge.getLastError()
			);
			Cronis.LOGGER.warn("Windows SMTC backend is unavailable. Spotify playback will remain disconnected.");
			return;
		}

		WindowsSmtcLog.stage("provider_initialized", "Backend: " + smtcBridge.getBackendName());
		discoverSessions();
		synchronizeSnapshotFromPlatform();

	}



	@Override

	public void stop() {

		if (!running) {

			return;

		}



		running = false;

		clearActiveState(false);

		smtcBridge.shutdown();

		nativeAvailable = false;

	}



	@Override

	public void refresh() {

		if (!running || !nativeAvailable) {

			return;

		}



		synchronizeSnapshotFromPlatform();

	}



	@Override

	public OsMediaPlaybackSnapshot captureSnapshot() {

		long nowMs = System.currentTimeMillis();

		MediaSession session = activeSession.get();

		MediaMetadata metadata = activeMetadata.get();



		if (session == null || metadata == null || !session.isSpotify() || !metadata.hasTrack()) {

			return OsMediaPlaybackSnapshot.empty();

		}



		return new OsMediaPlaybackSnapshot(

				Optional.of(session),

				Optional.of(metadata),

				playbackState.get(),

				shuffleEnabled,

				repeatMode.get(),

				positionTracker.positionAt(nowMs),

				nowMs

		);

	}



	@Override

	public void addListener(OsMediaSessionListener listener) {

		listeners.add(Objects.requireNonNull(listener, "listener"));

	}



	@Override

	public boolean removeListener(OsMediaSessionListener listener) {

		return listeners.remove(listener);

	}



	/**

	 * Applies a platform snapshot produced by the native SMTC bridge.

	 * <p>

	 * Native code must marshal callbacks onto the Cronis Spotify worker thread

	 * before invoking this method.

	 *

	 * @param session       active Spotify session

	 * @param metadata      current metadata

	 * @param state         transport state

	 * @param shuffle       shuffle flag

	 * @param repeat        repeat mode

	 * @param positionMs    reported timeline position

	 * @param durationMs    reported track duration

	 * @param eventTimeMs   platform event timestamp

	 */

	public void applyPlatformSnapshot(

			MediaSession session,

			MediaMetadata metadata,

			SpotifyPlaybackState state,

			boolean shuffle,

			SpotifyTrack.SpotifyRepeatMode repeat,

			long positionMs,

			long durationMs,

			long eventTimeMs

	) {

		Objects.requireNonNull(session, "session");

		Objects.requireNonNull(metadata, "metadata");

		Objects.requireNonNull(state, "state");

		Objects.requireNonNull(repeat, "repeat");



		if (!SpotifySessionMatcher.matches(session)) {

			return;

		}



		activeSession.set(session);

		activeMetadata.set(metadata);

		playbackState.set(state);

		shuffleEnabled = shuffle;

		repeatMode.set(repeat);



		float playbackRate = state == SpotifyPlaybackState.PLAYING ? 1.0f : 0.0f;

		positionTracker.update(positionMs, durationMs, playbackRate, eventTimeMs);

		notifyListeners();

	}



	private void discoverSessions() {

		// Session discovery is handled by the native SMTC bridge during synchronization.

	}



	private void synchronizeSnapshotFromPlatform() {
		if (!smtcBridge.synchronize()) {
			WindowsSmtcLog.stage("provider_synchronize", smtcBridge.getLastError());
			clearActiveState(true);
			return;
		}

		Optional<WindowsSmtcBridge.PlatformSnapshot> snapshot = smtcBridge.capturePlatformSnapshot();
		if (snapshot.isEmpty()) {
			WindowsSmtcLog.failure("provider_snapshot", "Spotify session snapshot was unavailable after synchronization");
			clearActiveState(true);
			return;
		}

		WindowsSmtcBridge.PlatformSnapshot platformSnapshot = snapshot.get();
		WindowsSmtcLog.stage(
				"provider_snapshot",
				"title='" + platformSnapshot.metadata().title() + "', artist='" + platformSnapshot.metadata().artist() + "'"
		);

		applyPlatformSnapshot(

				platformSnapshot.session(),

				platformSnapshot.metadata(),

				platformSnapshot.playbackState(),

				false,

				SpotifyTrack.SpotifyRepeatMode.OFF,

				platformSnapshot.positionMs(),

				platformSnapshot.durationMs(),

				platformSnapshot.eventTimeMs()

		);

	}



	private void clearActiveState(boolean notify) {

		if (activeSession.get() == null && activeMetadata.get() == null) {

			return;

		}



		activeSession.set(null);

		activeMetadata.set(null);

		playbackState.set(SpotifyPlaybackState.STOPPED);

		shuffleEnabled = false;

		repeatMode.set(SpotifyTrack.SpotifyRepeatMode.OFF);

		positionTracker.reset();



		if (notify) {

			notifyListeners();

		}

	}



	private void notifyListeners() {

		for (OsMediaSessionListener listener : listeners) {

			listener.onSessionChanged();

		}

	}

}


