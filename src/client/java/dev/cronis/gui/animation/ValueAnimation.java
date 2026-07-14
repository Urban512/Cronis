package dev.cronis.gui.animation;

/**
 * Smoothly interpolates a floating-point value toward a target.
 */
public final class ValueAnimation extends Animation {
	private final float min;
	private final float max;
	private float value;
	private float target;

	/**
	 * Creates a value animation within the given bounds.
	 *
	 * @param speed units per second
	 * @param min   minimum value
	 * @param max   maximum value
	 * @param value initial value
	 */
	public ValueAnimation(float speed, float min, float max, float value) {
		super(speed);
		this.min = min;
		this.max = max;
		this.value = clamp(value);
		this.target = this.value;
	}

	@Override
	public void update(float delta) {
		value = step(value, target, delta);
	}

	/**
	 * Sets the target value.
	 *
	 * @param target destination value
	 */
	public void setTarget(float target) {
		this.target = clamp(target);
	}

	/**
	 * Sets the current and target value immediately.
	 *
	 * @param value immediate value
	 */
	public void setImmediate(float value) {
		this.value = clamp(value);
		this.target = this.value;
	}

	/**
	 * Returns the current interpolated value.
	 *
	 * @return current value
	 */
	public float getValue() {
		return value;
	}

	private float clamp(float amount) {
		return Math.max(min, Math.min(max, amount));
	}
}
