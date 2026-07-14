package dev.cronis.editor.contextmenu;

import dev.cronis.widget.Widget;
import dev.cronis.widget.WidgetContext;

/**
 * Editor callbacks used by widget context menu actions.
 */
public interface WidgetContextMenuHost {
	/**
	 * Returns the active viewport context.
	 *
	 * @return viewport context
	 */
	WidgetContext viewportContext();

	/**
	 * Returns the scaled viewport width in GUI pixels.
	 *
	 * @return viewport width
	 */
	int screenWidth();

	/**
	 * Returns the scaled viewport height in GUI pixels.
	 *
	 * @return viewport height
	 */
	int screenHeight();

	/**
	 * Opens the widget inspector for the provided widget.
	 *
	 * @param widget widget to inspect
	 */
	void openInspector(Widget widget);

	/**
	 * Re-renders widgets and editor overlays after a layout mutation.
	 */
	void refreshEditorLayout();

	/**
	 * Called when the context menu closes.
	 */
	void onContextMenuClosed();
}
