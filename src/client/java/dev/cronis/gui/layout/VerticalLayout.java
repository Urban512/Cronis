package dev.cronis.gui.layout;

import dev.cronis.gui.component.GuiComponent;
import dev.cronis.gui.util.GuiBounds;

import java.util.List;

/**
 * Arranges child components in a single vertical column.
 */
public class VerticalLayout extends Layout {
	private final int spacing;

	/**
	 * Creates a vertical layout with the given spacing between children.
	 *
	 * @param spacing gap between siblings in pixels
	 */
	public VerticalLayout(int spacing) {
		this.spacing = spacing;
	}

	@Override
	public void layout(GuiBounds bounds, List<GuiComponent> children) {
		int currentY = bounds.y();
		for (GuiComponent child : children) {
			if (!child.isVisible()) {
				continue;
			}

			int childHeight = child.getPreferredHeight(bounds.width());
			child.setBounds(bounds.x(), currentY, bounds.width(), childHeight);
			currentY += childHeight + spacing;
		}
	}

	@Override
	public int preferredHeight(int width, List<GuiComponent> children) {
		int totalHeight = 0;
		int visibleCount = 0;

		for (GuiComponent child : children) {
			if (!child.isVisible()) {
				continue;
			}

			totalHeight += child.getPreferredHeight(width);
			visibleCount++;
		}

		if (visibleCount > 1) {
			totalHeight += spacing * (visibleCount - 1);
		}

		return totalHeight;
	}
}
