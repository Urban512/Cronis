package dev.cronis.gui.component;

import dev.cronis.gui.animation.FadeAnimation;
import dev.cronis.gui.render.CardRenderer;
import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.theme.DesignTokens;
import dev.cronis.gui.theme.GuiMetrics;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

/**
 * Interactive component for triggering a single primary action.
 */
public class GuiButton extends GuiComponent {
	private static final int HEIGHT = GuiMetrics.HEIGHT_BUTTON;
	private static final int CORNER_RADIUS = GuiMetrics.RADIUS_CONTROL;
	private static final int MIN_WIDTH = 88;

	private String label;
	private final FadeAnimation hoverAnimation = new FadeAnimation(DesignTokens.ANIM_HOVER);
	private Runnable onClick;
	private boolean hovered;

	public GuiButton(String label) {
		this.label = label;
		this.height = HEIGHT;
		this.width = MIN_WIDTH;
	}

	public GuiButton setOnClick(Runnable onClick) {
		this.onClick = onClick;
		return this;
	}

	public GuiButton setLabel(String label) {
		this.label = label;
		return this;
	}

	@Override
	public int getPreferredHeight(int availableWidth) {
		return HEIGHT;
	}

	@Override
	public int getPreferredWidth(int availableHeight) {
		return MIN_WIDTH;
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		hovered = enabled && contains(mouseX, mouseY);
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
		int background = ColorUtil.lerp(theme.buttonBackground(), theme.buttonBackgroundHover(), hover);
		int border = ColorUtil.lerp(theme.buttonBorder(), theme.accent(), hover * 0.5f);
		int textColor = enabled ? theme.buttonText() : theme.controlDisabled();
		if (enabled && hover > 0f) {
			background = ColorUtil.multiplyAlpha(
					background,
					1f - (1f - DesignTokens.HOVER_OPACITY) * hover
			);
		}

		boolean emphasized = hover > 0.35f;
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
		CardRenderer.draw(context, x, y, width, height, CardRenderer.Style.control(), background, border, emphasized);
		int textX = x + (width - font.width(label)) / 2;
		context.text(font, label, textX, y + (height - font.lineHeight) / 2, textColor, false);
		pose.popMatrix();
	}
}
