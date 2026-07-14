package dev.cronis.gui.animation;

/**
 * Represents a single animated value transition within the Cronis GUI framework.
 */
public abstract class Animation {
	private final float speed;

	protected Animation(float speed) {
		this.speed = speed;
	}

	/**
	 * Advances the animation toward its target.
	 *
	 * @param delta frame delta time in seconds
	 */
	public abstract void update(float delta);

	protected float step(float current, float target, float delta) {
		return Easing.easeOut(current, target, delta, speed);
	}
}
