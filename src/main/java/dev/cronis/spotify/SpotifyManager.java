package dev.cronis.spotify;

import dev.cronis.Cronis;
import dev.cronis.spotify.os.OsMediaPlaybackProvider;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Internal coordinator for Spotify polling, caching and refresh scheduling.
 * <p>
 * Widgets never access this class directly. All consumer-facing access flows
 * through {@link SpotifyService}.
 */
final class SpotifyManager {
	private static final long POLL_INTERVAL_PLAYING_MS = 1_000L;
	private static final long POLL_INTERVAL_ACTIVE_MS = 2_000L;
	private static final long POLL_INTERVAL_IDLE_MS = 5_000L;
	private static final long PROGRESS_NOTIFY_THRESHOLD_MS = 750L;

	private final SpotifySession session = new SpotifySession();
	private final AtomicReference<SpotifySnapshot> cache = new AtomicReference<>(SpotifySnapshot.empty());
	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(new SpotifyThreadFactory());
	private final SpotifyStateBroadcaster broadcaster;

	private volatile SpotifyPlaybackProvider provider = OsMediaPlaybackProvider.get();
	private volatile ScheduledFuture<?> scheduledPoll;
	private volatile boolean running;

	SpotifyManager(SpotifyStateBroadcaster broadcaster) {
		this.broadcaster = Objects.requireNonNull(broadcaster, "broadcaster");
	}

	SpotifySession session() {
		return session;
	}

	Optional<SpotifyTrack> currentTrack() {
		return cache.get().track();
	}

	SpotifyPlaybackState playbackState() {
		return cache.get().playbackState();
	}

	boolean isRunning() {
		return running;
	}

	void setProvider(SpotifyPlaybackProvider provider) {
		this.provider = Objects.requireNonNull(provider, "provider");
	}

	void start() {
		if (running) {
			return;
		}

		running = true;
		provider.onManagerStarted();
		schedulePoll(0L);
		Cronis.LOGGER.debug("Spotify manager started.");
	}

	void stop() {
		if (!running) {
			return;
		}

		running = false;
		cancelScheduledPoll();
		provider.onManagerStopped();
		session.reset();
		applySnapshot(SpotifySnapshot.empty(), true);
		Cronis.LOGGER.debug("Spotify manager stopped.");
	}

	void shutdown() {
		stop();
		executor.shutdownNow();
	}

	private void schedulePoll(long delayMs) {
		cancelScheduledPoll();
		if (!running) {
			return;
		}

		scheduledPoll = executor.schedule(this::pollSafely, delayMs, TimeUnit.MILLISECONDS);
	}

	private void cancelScheduledPoll() {
		ScheduledFuture<?> current = scheduledPoll;
		if (current != null) {
			current.cancel(false);
			scheduledPoll = null;
		}
	}

	private void pollSafely() {
		try {
			poll();
		} catch (Exception exception) {
			Cronis.LOGGER.warn("Spotify poll failed", exception);
			session.update(SpotifySession.State.ERROR, session.getAccountId(), session.getDisplayName());
			applySnapshot(cache.get().withSessionState(SpotifySession.State.ERROR), true);
		} finally {
			if (running) {
				schedulePoll(resolvePollIntervalMs());
			}
		}
	}

	private void poll() {
		SpotifyPlaybackUpdate update = provider.fetch(session);
		session.update(update.sessionState(), update.accountId(), update.displayName());

		SpotifySnapshot snapshot = new SpotifySnapshot(
				update.track(),
				update.playbackState(),
				update.sessionState(),
				System.currentTimeMillis()
		);
		applySnapshot(snapshot, false);
	}

	private void applySnapshot(SpotifySnapshot snapshot, boolean forceNotify) {
		SpotifySnapshot previous = cache.get();
		cache.set(snapshot);

		if (forceNotify || hasMeaningfulChange(previous, snapshot)) {
			broadcaster.broadcast(snapshot);
		}
	}

	private long resolvePollIntervalMs() {
		SpotifySnapshot snapshot = cache.get();
		return switch (snapshot.playbackState()) {
			case PLAYING, BUFFERING -> POLL_INTERVAL_PLAYING_MS;
			case PAUSED -> POLL_INTERVAL_ACTIVE_MS;
			case STOPPED -> POLL_INTERVAL_IDLE_MS;
		};
	}

	private static boolean hasMeaningfulChange(SpotifySnapshot previous, SpotifySnapshot current) {
		if (previous.sessionState() != current.sessionState()) {
			return true;
		}
		if (previous.playbackState() != current.playbackState()) {
			return true;
		}

		Optional<SpotifyTrack> previousTrack = previous.track();
		Optional<SpotifyTrack> currentTrack = current.track();
		if (previousTrack.isEmpty() != currentTrack.isEmpty()) {
			return true;
		}
		if (previousTrack.isEmpty()) {
			return false;
		}

		SpotifyTrack previousValue = previousTrack.get();
		SpotifyTrack currentValue = currentTrack.get();
		if (!previousValue.id().equals(currentValue.id())) {
			return true;
		}
		if (previousValue.shuffle() != currentValue.shuffle()) {
			return true;
		}
		if (previousValue.repeat() != currentValue.repeat()) {
			return true;
		}
		if (previousValue.playing() != currentValue.playing()) {
			return true;
		}
		if (previousValue.explicit() != currentValue.explicit()) {
			return true;
		}
		if (!previousValue.title().equals(currentValue.title())) {
			return true;
		}
		if (Math.abs(previousValue.progressMs() - currentValue.progressMs()) >= PROGRESS_NOTIFY_THRESHOLD_MS) {
			return true;
		}

		return false;
	}

	/**
	 * Immutable cached playback snapshot.
	 *
	 * @param track          currently playing track
	 * @param playbackState  player transport state
	 * @param sessionState   session state at fetch time
	 * @param fetchedAtMs    fetch timestamp in epoch milliseconds
	 */
	record SpotifySnapshot(
			Optional<SpotifyTrack> track,
			SpotifyPlaybackState playbackState,
			SpotifySession.State sessionState,
			long fetchedAtMs
	) {
		static SpotifySnapshot empty() {
			return new SpotifySnapshot(
					Optional.empty(),
					SpotifyPlaybackState.STOPPED,
					SpotifySession.State.DISCONNECTED,
					0L
			);
		}

		SpotifySnapshot {
			track = Objects.requireNonNullElse(track, Optional.empty());
			playbackState = Objects.requireNonNull(playbackState, "playbackState");
			sessionState = Objects.requireNonNull(sessionState, "sessionState");
		}

		SpotifySnapshot withSessionState(SpotifySession.State state) {
			return new SpotifySnapshot(track, playbackState, state, fetchedAtMs);
		}
	}

	private static final class SpotifyThreadFactory implements ThreadFactory {
		private static final String THREAD_NAME = "Cronis-Spotify";

		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(runnable, THREAD_NAME);
			thread.setDaemon(true);
			thread.setPriority(Thread.NORM_PRIORITY - 1);
			return thread;
		}
	}
}
