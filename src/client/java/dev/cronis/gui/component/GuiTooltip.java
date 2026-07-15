package dev.cronis.gui.component;

import dev.cronis.gui.animation.FadeAnimation;
import dev.cronis.gui.layout.Padding;
import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.render.CardRenderer;
import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.theme.DesignTokens;
import dev.cronis.gui.theme.GuiMetrics;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Floating tooltip bubble rendered near a pointer or anchor point.
 */
public class GuiTooltip extends GuiComponent {
	private static final int CORNER_RADIUS = GuiMetrics.RADIUS_COMPACT;
	private static final Padding PADDING = Padding.symmetric(Spacing.SM, Spacing.MD);
	private static final int MAX_WIDTH = 220;

	private final FadeAnimation visibilityAnimation = new FadeAnimation(DesignTokens.ANIM_PANEL);
	private String text = "";
	private boolean active;

	public GuiTooltip() {
		setVisible(false);
	}

	public GuiTooltip(String text) {
		this();
		this.text = text;
	}

	public void setText(String text) {
		this.text = text == null ? "" : text;
	}

	/**
	 * Shows the tooltip near the provided anchor position.
	 *
	 * @param text    tooltip content
	 * @param anchorX anchor X position
	 * @param anchorY anchor Y position
	 */
	public void show(String text, int anchorX, int anchorY) {
		this.text = text == null ? "" : text;
		this.active = !this.text.isBlank();
		setVisible(active);
		if (!active) {
			return;
		}

		setBounds(anchorX + Spacing.SM, anchorY - getPreferredHeight(MAX_WIDTH) - Spacing.SM, MAX_WIDTH, getPreferredHeight(MAX_WIDTH));
		visibilityAnimation.setTarget(1f);
	}

	/**
	 * Hides the tooltip.
	 */
	public void hide() {
		active = false;
		visibilityAnimation.setTarget(0f);
		if (visibilityAnimation.getValue() <= 0f) {
			setVisible(false);
		}
	}

	@Override
	public int getPreferredHeight(int availableWidth) {
		return PADDING.vertical() + 10;
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		visibilityAnimation.update(delta);
		if (!active && visibilityAnimation.getValue() <= 0f) {
			setVisible(false);
		}
		super.update(delta, mouseX, mouseY);
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		if (text.isBlank()) {
			return;
		}

		var theme = ThemeManager.get();
		float alpha = visibilityAnimation.getValue();
		if (alpha <= 0f) {
			return;
		}

		int background = ColorUtil.withAlpha(theme.tooltipBackground(), alpha);
		int border = ColorUtil.withAlpha(theme.tooltipBorder(), alpha);
		CardRenderer.draw(context, x, y, width, height, CardRenderer.Style.control(), background, border);
		context.text(font, text, x + PADDING.left(), y + PADDING.top(), ColorUtil.withAlpha(theme.textPrimary(), alpha), false);
	}
}
