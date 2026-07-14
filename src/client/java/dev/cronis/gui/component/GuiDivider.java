package dev.cronis.gui.component;

import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Horizontal or vertical separator line.
 */
public class GuiDivider extends GuiComponent {
	/**
	 * Divider orientation.
	 */
	public enum Orientation {
		HORIZONTAL,
		VERTICAL
	}

	private final Orientation orientation;

	public GuiDivider() {
		this(Orientation.HORIZONTAL);
	}

	public GuiDivider(Orientation orientation) {
		this.orientation = orientation;
	}

	@Override
	public int getPreferredHeight(int availableWidth) {
		return orientation == Orientation.HORIZONTAL ? Spacing.SM : availableWidth;
	}

	@Override
	public int getPreferredWidth(int availableHeight) {
		return orientation == Orientation.VERTICAL ? Spacing.SM : availableHeight;
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		var theme = ThemeManager.get();
		if (orientation == Orientation.HORIZONTAL) {
			int lineY = y + Math.max(0, (height - 1) / 2);
			context.fill(x, lineY, x + width, lineY + 1, theme.divider());
			return;
		}

		int lineX = x + Math.max(0, (width - 1) / 2);
		context.fill(lineX, y, lineX + 1, y + height, theme.divider());
	}
}
