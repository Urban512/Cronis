package dev.cronis.gui.component;

import dev.cronis.gui.animation.FadeAnimation;
import dev.cronis.gui.layout.Padding;
import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.render.RoundedRenderer;
import dev.cronis.gui.render.ShadowRenderer;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Visual grouping surface for related content.
 */
public class GuiCard extends GuiComponent {
	private static final int CORNER_RADIUS = 12;
	private static final Padding PADDING = Padding.symmetric(Spacing.LG, Spacing.LG);
	private static final int DESCRIPTION_GAP = Spacing.SM;

	private final String title;
	private final String description;
	private final FadeAnimation hoverAnimation = new FadeAnimation(12f);
	private boolean hovered;

	public GuiCard(String title, String description) {
		this.title = title;
		this.description = description;
	}

	@Override
	public int getPreferredHeight(int availableWidth) {
		return PADDING.vertical() + 9 + DESCRIPTION_GAP + 9 + PADDING.vertical();
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		hovered = contains(mouseX, mouseY);
		hoverAnimation.setTarget(hovered ? 1f : 0f);
		hoverAnimation.update(delta);
		super.update(delta, mouseX, mouseY);
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		var theme = ThemeManager.get();
		float hover = hoverAnimation.getValue();

		if (hover > 0f) {
			ShadowRenderer.draw(
					context,
					x,
					y,
					width,
					height,
					CORNER_RADIUS,
					6,
					0.18f * hover,
					theme.cardShadow()
			);
		}

		RoundedRenderer.fill(context, x, y, width, height, CORNER_RADIUS, theme.cardBackground());
		int borderColor = ColorUtil.lerp(theme.cardBorder(), theme.cardHoverBorder(), hover);
		RoundedRenderer.outline(context, x, y, width, height, CORNER_RADIUS, 1, borderColor);

		int textX = x + PADDING.left();
		int titleY = y + PADDING.top();
		int descriptionY = titleY + font.lineHeight + DESCRIPTION_GAP;

		context.text(font, title, textX, titleY, theme.textPrimary(), false);
		context.text(font, description, textX, descriptionY, theme.textSecondary(), false);
	}
}
