package dev.cronis.spotify.os;

/**
 * Interpolates playback position between OS media timeline events.
 * <p>
 * SMTC and MPRIS typically emit position updates on state changes rather than
 * every frame. This tracker keeps progress smooth for HUD rendering while the
 * provider continues to receive sparse platform events.
 */
public final class PlaybackPositionTracker {
	private long positionMs;
	private long durationMs;
	private long anchorTimeMs;
	private float playbackRate;

	/**
	 * Creates an empty tracker.
	 */
	public PlaybackPositionTracker() {
		reset();
	}

	/**
	 * Replaces the tracker state with a platform timeline sample.
	 *
	 * @param positionMs    reported position in milliseconds
	 * @param durationMs    reported duration in milliseconds
	 * @param playbackRate  {@code 1.0} while playing, {@code 0.0} while paused
	 * @param anchorTimeMs  sample timestamp in epoch milliseconds
	 */
	public void update(long positionMs, long durationMs, float playbackRate, long anchorTimeMs) {
		this.positionMs = Math.max(0L, positionMs);
		this.durationMs = Math.max(0L, durationMs);
		this.playbackRate = Math.max(0f, playbackRate);
		this.anchorTimeMs = anchorTimeMs;
	}

	/**
	 * Clears timeline state.
	 */
	public void reset() {
		positionMs = 0L;
		durationMs = 0L;
		anchorTimeMs = 0L;
		playbackRate = 0f;
	}

	/**
	 * Returns the interpolated position for the given timestamp.
	 *
	 * @param nowMs current epoch milliseconds
	 * @return interpolated position in milliseconds
	 */
	public long positionAt(long nowMs) {
		if (playbackRate <= 0f || nowMs <= anchorTimeMs) {
			return positionMs;
		}

		long elapsedMs = nowMs - anchorTimeMs;
		long interpolated = positionMs + (long) (elapsedMs * playbackRate);
		if (durationMs <= 0L) {
			return Math.max(0L, interpolated);
		}
		return Math.min(durationMs, Math.max(0L, interpolated));
	}

	/**
	 * Returns the last reported duration.
	 *
	 * @return duration in milliseconds
	 */
	public long durationMs() {
		return durationMs;
	}

	/**
	 * Returns the current playback rate.
	 *
	 * @return playback rate multiplier
	 */
	public float playbackRate() {
		return playbackRate;
	}

	/**
	 * Returns whether playback is actively progressing.
	 *
	 * @return {@code true} when the playback rate is greater than zero
	 */
	public boolean isPlaying() {
		return playbackRate > 0f;
	}
}
