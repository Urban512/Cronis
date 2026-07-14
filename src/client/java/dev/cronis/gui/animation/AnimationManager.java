package dev.cronis.gui.animation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Coordinates updates for active GUI animations.
 */
public final class AnimationManager {
	private final List<Animation> animations = new ArrayList<>();

	/**
	 * Registers an animation for automatic updates.
	 *
	 * @param animation animation to track
	 */
	public void register(Animation animation) {
		animations.add(animation);
	}

	/**
	 * Updates every registered animation.
	 *
	 * @param delta frame delta time in seconds
	 */
	public void update(float delta) {
		Iterator<Animation> iterator = animations.iterator();
		while (iterator.hasNext()) {
			Animation animation = iterator.next();
			animation.update(delta);
		}
	}
}
