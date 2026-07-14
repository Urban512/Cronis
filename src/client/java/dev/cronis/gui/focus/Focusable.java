package dev.cronis.gui.focus;

import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

/**
 * Components that accept keyboard focus and typed input.
 */
public interface Focusable {
	/**
	 * Called when this component becomes the focused element.
	 */
	void onFocusGained();

	/**
	 * Called when this component loses keyboard focus.
	 */
	void onFocusLost();

	/**
	 * Handles a key press while focused.
	 *
	 * @param event key event
	 * @return {@code true} if the event was consumed
	 */
	boolean keyPressed(KeyEvent event);

	/**
	 * Handles a typed character while focused.
	 *
	 * @param event character event
	 * @return {@code true} if the event was consumed
	 */
	boolean charTyped(CharacterEvent event);
}
