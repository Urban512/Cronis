package dev.cronis.gui.component;

import dev.cronis.gui.animation.FadeAnimation;
import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.render.RoundedRenderer;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Interactive component for triggering a single primary action.
 */
public class GuiButton extends GuiComponent {
	private static final int HEIGHT = 32;
	private static final int CORNER_RADIUS = 10;
	private static final int MIN_WIDTH = 88;

	private String label;
	private final FadeAnimation hoverAnimation = new FadeAnimation(10f);
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
		int background = ColorUtil.lerp(theme.buttonBackground(), theme.buttonBackgroundHover(), hoverAnimation.getValue());
		int border = ColorUtil.lerp(theme.buttonBorder(), theme.accent(), hoverAnimation.getValue() * 0.5f);
		int textColor = enabled ? theme.buttonText() : theme.controlDisabled();

		RoundedRenderer.fill(context, x, y, width, height, CORNER_RADIUS, background);
		RoundedRenderer.outline(context, x, y, width, height, CORNER_RADIUS, 1, border);
		int textX = x + (width - font.width(label)) / 2;
		context.text(font, label, textX, y + (height - font.lineHeight) / 2, textColor, false);
	}
}
