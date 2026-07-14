package dev.cronis.gui.component;

import dev.cronis.gui.animation.FadeAnimation;
import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.render.RoundedRenderer;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Compact icon button placeholder for header actions.
 */
public class GuiIconButton extends GuiComponent {
	private static final int SIZE = 28;
	private static final int CORNER_RADIUS = 6;

	private final String iconLabel;
	private final FadeAnimation hoverAnimation = new FadeAnimation(8f);
	private boolean hovered;

	public GuiIconButton(String iconLabel) {
		this.iconLabel = iconLabel;
		this.width = SIZE;
		this.height = SIZE;
	}

	@Override
	public int getPreferredWidth(int availableHeight) {
		return SIZE;
	}

	@Override
	public int getPreferredHeight(int availableWidth) {
		return SIZE;
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
		int background = ColorUtil.lerp(0x00000000, theme.sidebarItemHover(), hoverAnimation.getValue());
		int iconColor = ColorUtil.lerp(theme.iconDefault(), theme.iconHover(), hoverAnimation.getValue());

		if (hoverAnimation.getValue() > 0f) {
			RoundedRenderer.fill(context, x, y, width, height, CORNER_RADIUS, background);
		}

		int textWidth = font.width(iconLabel);
		context.text(
				font,
				iconLabel,
				x + (width - textWidth) / 2,
				y + (height - font.lineHeight) / 2,
				iconColor,
				false
		);
	}
}
