package dev.cronis.editor;

import dev.cronis.widget.Widget;
import dev.cronis.widget.WidgetBounds;
import dev.cronis.widget.WidgetContext;
import dev.cronis.widget.WidgetManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Owns HUD editor selection state and hit testing.
 */
public final class WidgetSelectionManager {
	private Widget selected;

	public Optional<Widget> selected() {
		return Optional.ofNullable(selected);
	}

	public Widget getSelectedOrNull() {
		return selected;
	}

	public boolean isSelected(Widget widget) {
		return selected == widget;
	}

	public void select(Widget widget) {
		selected = widget;
	}

	public void clear() {
		selected = null;
	}

	/**
	 * Returns the topmost widget under the cursor in reverse registration order.
	 *
	 * @param context viewport context
	 * @param mouseX  cursor X coordinate in GUI pixels
	 * @param mouseY  cursor Y coordinate in GUI pixels
	 * @return optional hit widget
	 */
	public Optional<Widget> hitTest(WidgetContext context, int mouseX, int mouseY) {
		List<Widget> widgets = new ArrayList<>(WidgetManager.get().getWidgets());
		for (int index = widgets.size() - 1; index >= 0; index--) {
			Widget widget = widgets.get(index);
			if (!widget.isVisible() || !widget.isEnabled()) {
				continue;
			}

			WidgetBounds bounds = widget.getInteractionBounds(context);
			if (bounds.contains(mouseX, mouseY)) {
				return Optional.of(widget);
			}
		}

		return Optional.empty();
	}

	/**
	 * Resolves the selected widget's current interaction bounds.
	 *
	 * @param context viewport context
	 * @return optional interaction bounds
	 */
	public Optional<WidgetBounds> selectedBounds(WidgetContext context) {
		if (selected == null) {
			return Optional.empty();
		}

		return Optional.of(selected.getInteractionBounds(context));
	}
}
