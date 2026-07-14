package dev.cronis.editor.contextmenu;

/**
 * Registers default HUD editor context menu actions.
 */
public final class WidgetEditorActions {
	private static boolean registered;

	private WidgetEditorActions() {
	}

	/**
	 * Registers built-in editor actions once.
	 */
	public static void registerDefaults() {
		if (registered) {
			return;
		}

		registered = true;
		WidgetContextMenuRegistry.register(new WidgetContextMenuAction(
				"Edit",
				widget -> true,
				context -> context.host().openInspector(context.widget())
		));
	}
}
