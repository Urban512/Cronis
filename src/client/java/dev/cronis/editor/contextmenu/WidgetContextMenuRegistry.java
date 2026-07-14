package dev.cronis.editor.contextmenu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registry of widget context menu actions.
 * <p>
 * Additional actions can be registered during client startup without modifying
 * the HUD editor screen.
 */
public final class WidgetContextMenuRegistry {
	private static final List<WidgetContextMenuAction> ACTIONS = new ArrayList<>();

	private WidgetContextMenuRegistry() {
	}

	/**
	 * Registers a context menu action.
	 *
	 * @param action action to register
	 */
	public static void register(WidgetContextMenuAction action) {
		ACTIONS.add(action);
	}

	/**
	 * Returns the registered context menu actions in registration order.
	 *
	 * @return immutable action list
	 */
	public static List<WidgetContextMenuAction> actions() {
		return Collections.unmodifiableList(ACTIONS);
	}
}
