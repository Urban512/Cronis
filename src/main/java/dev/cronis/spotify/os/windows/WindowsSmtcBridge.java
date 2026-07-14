package dev.cronis.spotify.os.windows;

import dev.cronis.spotify.SpotifyPlaybackState;
import dev.cronis.spotify.SpotifyTrack;
import dev.cronis.spotify.os.MediaMetadata;
import dev.cronis.spotify.os.MediaSession;

import java.util.Objects;
import java.util.Optional;

/**
 * Converts Windows SMTC backend state into Cronis OS media models.
 */
final class WindowsSmtcBridge {
	private final WindowsSmtcBackend backend = WindowsSmtcNative.backend();
	private boolean initialized;

	boolean initialize() {
		WindowsSmtcLog.stage("bridge_initialize", "Using backend: " + backend.backendName());

		if (initialized) {
			return true;
		}

		initialized = backend.initialize();
		if (initialized) {
			WindowsSmtcLog.stage("initialization_complete", backend.backendName() + " backend ready");
		} else {
			WindowsSmtcLog.failure("bridge_initialize", backend.getLastError());
		}
		return initialized;
	}

	void shutdown() {
		if (!initialized) {
			return;
		}

		backend.shutdown();
		initialized = false;
		WindowsSmtcLog.stage("bridge_shutdown", backend.backendName() + " backend stopped");
	}

	boolean isInitialized() {
		return initialized;
	}

	String getLastError() {
		return backend.getLastError();
	}

	String getBackendName() {
		return backend.backendName();
	}

	/**
	 * Synchronizes the backend cache with the current Windows media session.
	 *
	 * @return {@code true} when the active session is Spotify with track metadata
	 */
	boolean synchronize() {
		if (!initialized) {
			return false;
		}

		return backend.synchronize();
	}

	Optional<PlatformSnapshot> capturePlatformSnapshot() {
		if (!initialized || !backend.hasActiveSession()) {
			return Optional.empty();
		}

		MediaSession session = new MediaSession(
				backend.getSessionId(),
				backend.getApplicationId(),
				backend.getApplicationName()
		);

		if (!SpotifySessionMatcher.matches(session)) {
			WindowsSmtcLog.failure("spotify_session_match", "Session rejected by matcher: " + session.applicationId());
			return Optional.empty();
		}

		MediaMetadata metadata = new MediaMetadata(
				"",
				backend.getTitle(),
				backend.getArtist(),
				backend.getAlbumTitle(),
				backend.getAlbumArtist(),
				backend.getDurationMs(),
				"",
				false
		);

		if (!metadata.hasTrack()) {
			WindowsSmtcLog.failure("metadata_retrieval", "Spotify session did not provide a track title");
			return Optional.empty();
		}

		long eventTimeMs = System.currentTimeMillis();
		return Optional.of(new PlatformSnapshot(
				session,
				metadata,
				mapPlaybackState(backend.getPlaybackStatus()),
				backend.getPositionMs(),
				Math.max(metadata.durationMs(), backend.getDurationMs()),
				eventTimeMs
		));
	}

	private static SpotifyPlaybackState mapPlaybackState(int playbackStatus) {
		return switch (playbackStatus) {
			case 4 -> SpotifyPlaybackState.PLAYING;
			case 5 -> SpotifyPlaybackState.PAUSED;
			case 2 -> SpotifyPlaybackState.BUFFERING;
			default -> SpotifyPlaybackState.STOPPED;
		};
	}

	record PlatformSnapshot(
			MediaSession session,
			MediaMetadata metadata,
			SpotifyPlaybackState playbackState,
			long positionMs,
			long durationMs,
			long eventTimeMs
	) {
		PlatformSnapshot {
			Objects.requireNonNull(session, "session");
			Objects.requireNonNull(metadata, "metadata");
			Objects.requireNonNull(playbackState, "playbackState");
		}
	}
}
