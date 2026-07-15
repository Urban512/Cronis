package dev.cronis.gui.component;

import dev.cronis.gui.animation.FadeAnimation;
import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.render.RoundedRenderer;
import dev.cronis.gui.theme.GuiMetrics;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.function.Consumer;

/**
 * Sidebar navigation item with hover, selection, and disabled states.
 */
public class GuiSidebarItem extends GuiComponent {
	private static final int ACCENT_WIDTH = 3;
	private static final int ICON_SIZE = 5;

	private final String label;
	private final FadeAnimation hoverAnimation = new FadeAnimation(12f);
	private final FadeAnimation selectionAnimation = new FadeAnimation(10f);
	private boolean selected;
	private boolean hovered;
	private Consumer<GuiSidebarItem> onSelect;

	public GuiSidebarItem(String label) {
		this.label = label;
		this.height = GuiMetrics.HEIGHT_NAV_ITEM;
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
		return GuiMetrics.HEIGHT_NAV_ITEM;
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
		float selection = selectionAnimation.getValue();
		float hover = hoverAnimation.getValue();
		int radius = GuiMetrics.RADIUS_COMPACT;

		if (selection > 0f) {
			int selectedColor = ColorUtil.withAlpha(theme.sidebarItemSelected(), selection * 0.95f);
			RoundedRenderer.fill(context, x, y, width, height, radius, selectedColor);
		} else if (hover > 0f) {
			int hoverColor = ColorUtil.withAlpha(theme.sidebarItemHover(), hover * 0.75f);
			RoundedRenderer.fill(context, x, y, width, height, radius, hoverColor);
		}

		if (selection > 0f) {
			int accentHeight = Math.max(14, height - Spacing.MD * 2);
			int accentY = y + (height - accentHeight) / 2;
			int accentColor = ColorUtil.withAlpha(theme.sidebarAccentIndicator(), selection);
			RoundedRenderer.fill(context, x + Spacing.SM, accentY, ACCENT_WIDTH, accentHeight, ACCENT_WIDTH, accentColor);
		}

		int iconX = x + Spacing.MD;
		int iconY = y + (height - ICON_SIZE) / 2;
		int iconColor = enabled
				? ColorUtil.lerp(theme.sidebarItemTextMuted(), theme.sidebarAccentIndicator(), selection)
				: theme.sidebarItemTextMuted();
		RoundedRenderer.fill(context, iconX, iconY, ICON_SIZE, ICON_SIZE, ICON_SIZE / 2, iconColor);

		int textColor = enabled
				? ColorUtil.lerp(theme.sidebarItemText(), theme.textPrimary(), selection * 0.65f)
				: theme.sidebarItemTextMuted();
		context.text(font, label, iconX + ICON_SIZE + Spacing.SM, y + (height - font.lineHeight) / 2, textColor, false);
	}
}
