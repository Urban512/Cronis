package dev.cronis.editor.inspector;

import dev.cronis.gui.component.GuiComponent;
import dev.cronis.gui.theme.GuiMetrics;

import java.util.List;

/**
 * Simple vertical container used to group dynamically generated inspector rows.
 */
final class InspectorSection extends GuiComponent {
	@Override
	public void update(float delta, int mouseX, int mouseY) {
		layoutChildren(x, y, width);
		super.update(delta, mouseX, mouseY);
	}

	@Override
	public int getPreferredHeight(int availableWidth) {
		int totalHeight = 0;
		boolean first = true;
		for (GuiComponent child : getChildren()) {
			if (!child.isVisible()) {
				continue;
			}

			if (!first) {
				totalHeight += GuiMetrics.ROW_GAP;
			}

			totalHeight += child.getPreferredHeight(availableWidth);
			first = false;
		}
		return totalHeight;
	}

	void layoutChildren(int originX, int originY, int availableWidth) {
		int currentY = originY;
		boolean first = true;
		for (GuiComponent child : getChildren()) {
			if (!child.isVisible()) {
				continue;
			}

			if (!first) {
				currentY += GuiMetrics.ROW_GAP;
			}

			int childHeight = child.getPreferredHeight(availableWidth);
			child.setBounds(originX, currentY, availableWidth, childHeight);
			currentY += childHeight;
			first = false;
		}

		height = Math.max(0, currentY - originY);
	}
}
