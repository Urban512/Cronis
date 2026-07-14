package dev.cronis.gui.animation;

/**
 * Smoothly interpolates a normalized opacity value between {@code 0.0} and {@code 1.0}.
 */
public final class FadeAnimation extends Animation {
	private float value;
	private float target;

	/**
	 * Creates a fade animation with the given transition speed.
	 *
	 * @param speed units per second
	 */
	public FadeAnimation(float speed) {
		super(speed);
	}

	/**
	 * Creates a fade animation with default speed.
	 */
	public FadeAnimation() {
		this(6f);
	}

	@Override
	public void update(float delta) {
		value = step(value, target, delta);
	}

	/**
	 * Sets the target opacity.
	 *
	 * @param target opacity in the range {@code 0.0-1.0}
	 */
	public void setTarget(float target) {
		this.target = clamp(target);
	}

	/**
	 * Sets the current and target opacity immediately.
	 *
	 * @param value opacity in the range {@code 0.0-1.0}
	 */
	public void setImmediate(float value) {
		this.value = clamp(value);
		this.target = this.value;
	}

	/**
	 * Returns the current interpolated opacity.
	 *
	 * @return opacity in the range {@code 0.0-1.0}
	 */
	public float getValue() {
		return value;
	}

	private static float clamp(float value) {
		return Math.max(0f, Math.min(1f, value));
	}
}
