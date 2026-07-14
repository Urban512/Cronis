package dev.cronis.gui.component;

import dev.cronis.gui.animation.FadeAnimation;
import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.render.RoundedRenderer;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.function.Consumer;

/**
 * Sidebar navigation item with hover, selection, and disabled states.
 */
public class GuiSidebarItem extends GuiComponent {
	private static final int HEIGHT = 34;
	private static final int CORNER_RADIUS = 6;
	private static final int ICON_SIZE = 8;

	private final String label;
	private final FadeAnimation hoverAnimation = new FadeAnimation(8f);
	private final FadeAnimation selectionAnimation = new FadeAnimation(6f);
	private boolean selected;
	private boolean hovered;
	private Consumer<GuiSidebarItem> onSelect;

	public GuiSidebarItem(String label) {
		this.label = label;
		this.height = HEIGHT;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
		selectionAnimation.setTarget(selected ? 1f : 0f);
	}

	public void setOnSelect(Consumer<GuiSidebarItem> onSelect) {
		this.onSelect = onSelect;
	}

	public String getLabel() {
		return label;
	}

	@Override
	public int getPreferredHeight(int availableWidth) {
		return HEIGHT;
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		hovered = enabled && contains(mouseX, mouseY);
		hoverAnimation.setTarget(hovered && !selected ? 1f : 0f);
		selectionAnimation.setTarget(selected ? 1f : 0f);
		hoverAnimation.update(delta);
		selectionAnimation.update(delta);
		super.update(delta, mouseX, mouseY);
	}

	@Override
	protected boolean handleMouseClicked(double mouseX, double mouseY, int button) {
		if (enabled && contains((int) mouseX, (int) mouseY) && onSelect != null) {
			onSelect.accept(this);
			return true;
		}
		return false;
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		var theme = ThemeManager.get();
		int hoverColor = ColorUtil.withAlpha(theme.sidebarItemHover(), hoverAnimation.getValue() * 0.85f);
		int selectedColor = ColorUtil.withAlpha(theme.sidebarItemSelected(), selectionAnimation.getValue() * 0.95f);

		if (hoverAnimation.getValue() > 0f) {
			RoundedRenderer.fill(context, x, y, width, height, CORNER_RADIUS, hoverColor);
		}
		if (selectionAnimation.getValue() > 0f) {
			RoundedRenderer.fill(context, x, y, width, height, CORNER_RADIUS, selectedColor);
		}

		int iconX = x + Spacing.MD;
		int iconY = y + (height - ICON_SIZE) / 2;
		int iconColor = enabled ? theme.logoAccent() : theme.sidebarItemTextMuted();
		RoundedRenderer.fill(context, iconX, iconY, ICON_SIZE, ICON_SIZE, ICON_SIZE / 2, iconColor);

		int textColor = enabled
				? (selected ? theme.textPrimary() : theme.sidebarItemText())
				: theme.sidebarItemTextMuted();
		context.text(font, label, iconX + ICON_SIZE + Spacing.SM, y + (height - font.lineHeight) / 2, textColor, false);
	}
}
