package dev.cronis.gui.layout;

import dev.cronis.gui.component.GuiComponent;
import dev.cronis.gui.util.GuiBounds;

import java.util.List;

/**
 * Defines how child components are positioned and sized within a parent container.
 */
public abstract class Layout {
	protected Layout() {
	}

	/**
	 * Positions children within the provided bounds.
	 *
	 * @param bounds   available layout area
	 * @param children components to position
	 */
	public abstract void layout(GuiBounds bounds, List<GuiComponent> children);

	/**
	 * Returns the preferred height required to lay out children at the given width.
	 *
	 * @param width    available width
	 * @param children components to measure
	 * @return total height
	 */
	public abstract int preferredHeight(int width, List<GuiComponent> children);
}
