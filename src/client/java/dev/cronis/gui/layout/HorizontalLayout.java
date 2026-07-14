package dev.cronis.gui.layout;

import dev.cronis.gui.component.GuiComponent;
import dev.cronis.gui.util.GuiBounds;

import java.util.List;

/**
 * Arranges child components in a single horizontal row.
 */
public class HorizontalLayout extends Layout {
	private final int spacing;

	/**
	 * Creates a horizontal layout with the given spacing between children.
	 *
	 * @param spacing gap between siblings in pixels
	 */
	public HorizontalLayout(int spacing) {
		this.spacing = spacing;
	}

	@Override
	public void layout(GuiBounds bounds, List<GuiComponent> children) {
		int currentX = bounds.x();
		for (GuiComponent child : children) {
			if (!child.isVisible()) {
				continue;
			}

			int childWidth = child.getPreferredWidth(bounds.height());
			child.setBounds(currentX, bounds.y(), childWidth, bounds.height());
			currentX += childWidth + spacing;
		}
	}

	@Override
	public int preferredHeight(int width, List<GuiComponent> children) {
		int maxHeight = 0;
		for (GuiComponent child : children) {
			if (!child.isVisible()) {
				continue;
			}

			maxHeight = Math.max(maxHeight, child.getPreferredHeight(width));
		}
		return maxHeight;
	}

	/**
	 * Returns the preferred width required to lay out children at the given height.
	 *
	 * @param height   available height
	 * @param children components to measure
	 * @return total width
	 */
	public int preferredWidth(int height, List<GuiComponent> children) {
		int totalWidth = 0;
		int visibleCount = 0;

		for (GuiComponent child : children) {
			if (!child.isVisible()) {
				continue;
			}

			totalWidth += child.getPreferredWidth(height);
			visibleCount++;
		}

		if (visibleCount > 1) {
			totalWidth += spacing * (visibleCount - 1);
		}

		return totalWidth;
	}
}
