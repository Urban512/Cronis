package dev.cronis.gui.component;

import dev.cronis.gui.animation.FadeAnimation;
import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.render.CardRenderer;
import dev.cronis.gui.render.IconManager;
import dev.cronis.gui.theme.DesignTokens;
import dev.cronis.gui.theme.GuiMetrics;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

/**
 * Compact icon button for header actions.
 */
public class GuiIconButton extends GuiComponent {
	private static final int SIZE = GuiMetrics.HEIGHT_CONTROL;
	private static final int CORNER_RADIUS = GuiMetrics.RADIUS_CONTROL;
	private static final int ICON_SIZE = GuiMetrics.ICON_MD;

	private final IconManager.Icon icon;
	private final FadeAnimation hoverAnimation = new FadeAnimation(DesignTokens.ANIM_HOVER);
	private Runnable onClick;
	private boolean hovered;

	public GuiIconButton(IconManager.Icon icon) {
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
		float hover = hoverAnimation.getValue();
		int background = ColorUtil.withAlpha(theme.sidebarItemHover(), hover * DesignTokens.HOVER_OPACITY);
		int iconColor = ColorUtil.lerp(theme.iconDefault(), theme.iconHover(), hover);
		int border = ColorUtil.lerp(theme.cardBorder(), theme.accent(), hover * 0.45f);

		float scale = 1f + (DesignTokens.HOVER_SCALE - 1f) * hover;
		Matrix3x2fStack pose = context.pose();
		pose.pushMatrix();
		if (scale != 1f) {
			float cx = x + width * 0.5f;
			float cy = y + height * 0.5f;
			pose.translate(cx, cy);
			pose.scale(scale, scale);
			pose.translate(-cx, -cy);
		}

		if (hover > 0f) {
			CardRenderer.draw(
					context,
					x,
					y,
					width,
					height,
					CardRenderer.Style.control(),
					background,
					border,
					hover > 0.35f
			);
		}

		int iconX = x + (width - ICON_SIZE) / 2;
		int iconY = y + (height - ICON_SIZE) / 2;
		IconManager.draw(context, icon, iconX, iconY, ICON_SIZE, iconColor);
		pose.popMatrix();
	}
}
