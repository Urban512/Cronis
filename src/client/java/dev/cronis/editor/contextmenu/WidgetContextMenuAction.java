package dev.cronis.editor.contextmenu;

import dev.cronis.widget.Widget;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Declarative action entry for the widget context menu.
 *
 * @param label   menu label
 * @param enabled whether the action is available for the target widget
 * @param handler action handler
 */
public record WidgetContextMenuAction(
		String label,
		Predicate<Widget> enabled,
		Consumer<WidgetContextMenuContext> handler
) {
	public WidgetContextMenuAction {
		Objects.requireNonNull(label, "label");
		Objects.requireNonNull(enabled, "enabled");
		Objects.requireNonNull(handler, "handler");
		if (label.isBlank()) {
			throw new IllegalArgumentException("label must not be blank");
		}
	}
}
