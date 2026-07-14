package dev.cronis.gui.focus;

import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

/**
 * Tracks and dispatches keyboard focus across the Cronis component tree.
 */
public final class FocusManager {
	private Focusable focused;

	/**
	 * Returns the currently focused component, if any.
	 *
	 * @return focused component
	 */
	public Focusable getFocused() {
		return focused;
	}

	/**
	 * Requests focus for the given component.
	 *
	 * @param component component to focus
	 */
	public void requestFocus(Focusable component) {
		if (focused == component) {
			return;
		}

		if (focused != null) {
			focused.onFocusLost();
		}

		focused = component;

		if (focused != null) {
			focused.onFocusGained();
		}
	}

	/**
	 * Clears the active keyboard focus.
	 */
	public void clearFocus() {
		requestFocus(null);
	}

	/**
	 * Dispatches a key press to the focused component.
	 *
	 * @param event key event
	 * @return {@code true} if the event was consumed
	 */
	public boolean dispatchKeyPressed(KeyEvent event) {
		return focused != null && focused.keyPressed(event);
	}

	/**
	 * Dispatches a typed character to the focused component.
	 *
	 * @param event character event
	 * @return {@code true} if the event was consumed
	 */
	public boolean dispatchCharTyped(CharacterEvent event) {
		return focused != null && focused.charTyped(event);
	}
}
