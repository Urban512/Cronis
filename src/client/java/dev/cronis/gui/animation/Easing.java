package dev.cronis.gui.animation;

/**
 * Easing functions for smooth Cronis interface transitions.
 */
public final class Easing {
	private Easing() {
	}

	/**
	 * Exponential ease-out interpolation toward a target value.
	 *
	 * @param current current value
	 * @param target  destination value
	 * @param delta   frame delta time in seconds
	 * @param speed   convergence rate; higher values snap faster
	 * @return eased value between current and target
	 */
	public static float easeOut(float current, float target, float delta, float speed) {
		if (current == target) {
			return target;
		}

		float factor = 1f - (float) Math.exp(-speed * delta);
		return current + (target - current) * factor;
	}

	/**
	 * Cubic ease-out curve for normalized progress values.
	 *
	 * @param t progress in the range {@code 0.0-1.0}
	 * @return eased progress
	 */
	public static float easeOutCubic(float t) {
		float clamped = Math.max(0f, Math.min(1f, t));
		float inverse = 1f - clamped;
		return 1f - inverse * inverse * inverse;
	}
}
