package dev.cronis.gui.component;

import dev.cronis.gui.animation.FadeAnimation;
import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.render.RoundedRenderer;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Single row in a {@link GuiContextMenu}.
 */
public final class GuiContextMenuItem extends GuiComponent {
	public static final int HEIGHT = 28;
	private static final int HORIZONTAL_PADDING = Spacing.MD;

	private final String label;
	private final FadeAnimation hoverAnimation = new FadeAnimation(12f);
	private Runnable onSelect;
	private boolean hovered;

	public GuiContextMenuItem(String label) {
		this.label = label;
		this.height = HEIGHT;
	}

	public void setOnSelect(Runnable onSelect) {
		this.onSelect = onSelect;
	}

	@Override
	public int getPreferredHeight(int availableWidth) {
		return HEIGHT;
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
		if (enabled && contains((int) mouseX, (int) mouseY) && onSelect != null) {
			onSelect.run();
			return true;
		}
		return false;
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		var theme = ThemeManager.get();
		float hover = hoverAnimation.getValue();
		if (hover > 0f) {
			int hoverColor = ColorUtil.withAlpha(theme.dropdownItemHover(), hover * 0.95f);
			RoundedRenderer.fill(context, x, y, width, height, 6, hoverColor);
		}

		int textColor = enabled ? theme.textPrimary() : theme.controlDisabled();
		context.text(font, label, x + HORIZONTAL_PADDING, y + (height - font.lineHeight) / 2, textColor, false);
	}

	int measureWidth(Font font) {
		return HORIZONTAL_PADDING * 2 + font.width(label);
	}
}
