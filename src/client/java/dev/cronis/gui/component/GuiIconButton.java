package dev.cronis.gui.component;

import dev.cronis.gui.animation.FadeAnimation;
import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.render.IconRenderer;
import dev.cronis.gui.render.RoundedRenderer;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Compact icon button for header actions.
 */
public class GuiIconButton extends GuiComponent {
	private static final int SIZE = 30;
	private static final int CORNER_RADIUS = 8;
	private static final int ICON_SIZE = 14;

	private final IconRenderer.Icon icon;
	private final FadeAnimation hoverAnimation = new FadeAnimation(10f);
	private Runnable onClick;
	private boolean hovered;

	public GuiIconButton(IconRenderer.Icon icon) {
		this.icon = icon;
		this.width = SIZE;
		this.height = SIZE;
	}

	/**
	 * Sets the action executed when the button is clicked.
	 *
	 * @param onClick click handler
	 * @return this button for chaining
	 */
	public GuiIconButton setOnClick(Runnable onClick) {
		this.onClick = onClick;
		return this;
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
	protected boolean handleMouseClicked(double mouseX, double mouseY, int button) {
		if (!enabled || !contains((int) mouseX, (int) mouseY)) {
			return false;
		}

		if (onClick != null) {
			onClick.run();
		}

		return true;
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		var theme = ThemeManager.get();
		int background = ColorUtil.withAlpha(theme.sidebarItemHover(), hoverAnimation.getValue() * 0.9f);
		int iconColor = ColorUtil.lerp(theme.iconDefault(), theme.iconHover(), hoverAnimation.getValue());

		if (hoverAnimation.getValue() > 0f) {
			RoundedRenderer.fill(context, x, y, width, height, CORNER_RADIUS, background);
		}

		int iconX = x + (width - ICON_SIZE) / 2;
		int iconY = y + (height - ICON_SIZE) / 2;
		IconRenderer.draw(context, icon, iconX, iconY, ICON_SIZE, iconColor);
	}
}
