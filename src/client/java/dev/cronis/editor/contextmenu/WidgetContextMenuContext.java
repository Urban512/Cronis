package dev.cronis.editor.contextmenu;

import dev.cronis.widget.Widget;

import java.util.Objects;

/**
 * Execution context for a widget context menu action.
 *
 * @param widget target widget
 * @param host   editor host callbacks
 */
public record WidgetContextMenuContext(Widget widget, WidgetContextMenuHost host) {
	public WidgetContextMenuContext {
		Objects.requireNonNull(widget, "widget");
		Objects.requireNonNull(host, "host");
	}
}
