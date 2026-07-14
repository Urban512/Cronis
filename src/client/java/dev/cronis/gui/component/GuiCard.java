package dev.cronis.gui.component;

import dev.cronis.gui.layout.Padding;
import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.layout.VerticalLayout;
import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.render.RoundedRenderer;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Visual grouping surface for related content.
 */
public class GuiCard extends GuiComponent {
	private static final int CORNER_RADIUS = 8;
	private static final Padding PADDING = Padding.all(Spacing.LG);

	private final String title;
	private final String description;

	public GuiCard(String title, String description) {
		this.title = title;
		this.description = description;
	}

	@Override
	public int getPreferredHeight(int availableWidth) {
		return 88;
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		var theme = ThemeManager.get();
		RoundedRenderer.fill(context, x, y, width, height, CORNER_RADIUS, theme.cardBackground());
		RoundedRenderer.outline(context, x, y, width, height, CORNER_RADIUS, 1, theme.cardBorder());

		int textX = x + PADDING.left();
		int textY = y + PADDING.top();
		int textWidth = width - PADDING.horizontal();

		context.text(font, title, textX, textY, theme.textPrimary(), false);
		context.text(font, description, textX, textY + font.lineHeight + Spacing.SM, theme.textSecondary(), false);
	}
}
