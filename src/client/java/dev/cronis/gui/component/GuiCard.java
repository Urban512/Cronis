package dev.cronis.gui.component;

import dev.cronis.gui.animation.FadeAnimation;
import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.render.CardRenderer;
import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.theme.DesignTokens;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Visual grouping surface for related content.
 */
public class GuiCard extends GuiComponent {
	private static final int DESCRIPTION_GAP = Spacing.XS;

	private final String title;
	private final String description;
	private final FadeAnimation hoverAnimation = new FadeAnimation(DesignTokens.ANIMATION_NORMAL);
	private boolean hovered;

	public GuiCard(String title, String description) {
		this.title = title;
		this.description = description;
	}

	@Override
	public int getPreferredHeight(int availableWidth) {
		int lineHeight = 9;
		return DesignTokens.CARD_PADDING.vertical()
				+ lineHeight
				+ DESCRIPTION_GAP
				+ lineHeight;
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

		CardRenderer.draw(
				context,
				x,
				y,
				width,
				height,
				CardRenderer.Style.card(),
				theme.cardBackground(),
				ColorUtil.lerp(theme.cardBorder(), theme.cardHoverBorder(), hover)
		);

		int textX = x + DesignTokens.CARD_PADDING.left();
		int titleY = y + DesignTokens.CARD_PADDING.top();
		int descriptionY = titleY + font.lineHeight + DESCRIPTION_GAP;

		context.text(font, title, textX, titleY, theme.textPrimary(), false);
		context.text(font, description, textX, descriptionY, theme.textSecondary(), false);
	}
}
