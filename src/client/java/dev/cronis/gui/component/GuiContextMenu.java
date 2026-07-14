package dev.cronis.gui.component;

import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.render.RoundedRenderer;
import dev.cronis.gui.render.ShadowRenderer;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

/**
 * Compact themed context menu anchored near the cursor.
 */
public final class GuiContextMenu extends GuiComponent {
	public static final int MIN_WIDTH = 128;
	public static final int CORNER_RADIUS = 8;
	private static final int VERTICAL_PADDING = Spacing.XS;

	private final List<GuiContextMenuItem> items = new ArrayList<>();

	public void clearItems() {
		clearChildren();
		items.clear();
	}

	/**
	 * Adds a selectable menu row.
	 *
	 * @param label    row label
	 * @param onSelect selection handler
	 */
	public void addItem(String label, Runnable onSelect) {
		GuiContextMenuItem item = new GuiContextMenuItem(label);
		item.setOnSelect(onSelect);
		items.add(item);
		addChild(item);
	}

	/**
	 * Lays out the menu near the provided anchor and clamps it to the viewport.
	 *
	 * @param anchorX      anchor X coordinate in GUI pixels
	 * @param anchorY      anchor Y coordinate in GUI pixels
	 * @param screenWidth  viewport width
	 * @param screenHeight viewport height
	 * @param font         active font
	 */
	public void layoutAt(int anchorX, int anchorY, int screenWidth, int screenHeight, Font font) {
		int menuWidth = Math.max(MIN_WIDTH, measureWidth(font));
		int menuHeight = measureHeight();
		int x = Math.clamp(anchorX, 0, Math.max(0, screenWidth - menuWidth));
		int y = Math.clamp(anchorY, 0, Math.max(0, screenHeight - menuHeight));
		setBounds(x, y, menuWidth, menuHeight);

		int itemY = y + VERTICAL_PADDING;
		for (GuiContextMenuItem item : items) {
			item.setBounds(x, itemY, menuWidth, GuiContextMenuItem.HEIGHT);
			itemY += GuiContextMenuItem.HEIGHT;
		}
	}

	public boolean containsPoint(int mouseX, int mouseY) {
		return contains(mouseX, mouseY);
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		var theme = ThemeManager.get();
		ShadowRenderer.draw(context, x, y, width, height, CORNER_RADIUS, 6, 0.24f, theme.cardShadow());
		RoundedRenderer.fill(context, x, y, width, height, CORNER_RADIUS, theme.cardBackground());
		RoundedRenderer.outline(context, x, y, width, height, CORNER_RADIUS, 1, theme.cardBorder());
	}

	@Override
	protected void renderChildren(GuiGraphicsExtractor context, Font font) {
		for (GuiComponent child : getChildren()) {
			child.render(context, font);
		}
	}

	private int measureWidth(Font font) {
		int maxWidth = MIN_WIDTH;
		for (GuiContextMenuItem item : items) {
			maxWidth = Math.max(maxWidth, item.measureWidth(font));
		}
		return maxWidth;
	}

	private int measureHeight() {
		if (items.isEmpty()) {
			return VERTICAL_PADDING * 2;
		}
		return VERTICAL_PADDING * 2 + items.size() * GuiContextMenuItem.HEIGHT;
	}
}
