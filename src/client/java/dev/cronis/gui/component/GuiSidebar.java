package dev.cronis.gui.component;

import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.layout.VerticalLayout;
import dev.cronis.gui.render.RoundedRenderer;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Left navigation sidebar with branding, categories, and footer.
 */
public class GuiSidebar extends GuiComponent {
	public static final int WIDTH = 220;

	private static final String[] CATEGORIES = {
			"General", "HUD", "Modules", "SkyBlock", "Dungeon",
			"Mining", "Fishing", "Garden", "Combat", "Profiles", "Developer"
	};

	private final List<GuiSidebarItem> items = new ArrayList<>();
	private final VerticalLayout layout = new VerticalLayout(Spacing.XS);
	private GuiSidebarItem selectedItem;
	private Consumer<GuiSidebarItem> selectionListener;

	public GuiSidebar() {
		this.width = WIDTH;
		for (String category : CATEGORIES) {
			GuiSidebarItem item = new GuiSidebarItem(category);
			item.setOnSelect(this::selectItem);
			items.add(item);
			addChild(item);
		}

		if (!items.isEmpty()) {
			selectItem(items.getFirst());
		}
	}

	public void setSelectionListener(Consumer<GuiSidebarItem> selectionListener) {
		this.selectionListener = selectionListener;
	}

	public GuiSidebarItem getSelectedItem() {
		return selectedItem;
	}

	public void layoutSidebar(int x, int y, int height) {
		setBounds(x, y, WIDTH, height);
		int padding = Spacing.LG;
		int logoHeight = 48;
		int footerHeight = 28;
		int listY = y + padding + logoHeight + Spacing.MD;
		int listHeight = height - logoHeight - footerHeight - padding * 2 - Spacing.MD;
		layout.layout(new dev.cronis.gui.util.GuiBounds(x + Spacing.SM, listY, WIDTH - Spacing.SM * 2, listHeight), getChildren());
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		var theme = ThemeManager.get();
		context.fill(x, y, x + width, y + height, theme.sidebarBackground());
		context.fill(x + width - 1, y, x + width, y + height, theme.headerDivider());

		int padding = Spacing.LG;
		int logoSize = 28;
		int logoX = x + padding;
		int logoY = y + padding;
		RoundedRenderer.fill(context, logoX, logoY, logoSize, logoSize, 8, theme.logoAccent());
		context.text(font, "Cronis", logoX + logoSize + Spacing.SM, logoY + 4, theme.textPrimary(), false);
		context.text(font, "SkyBlock HUD", logoX + logoSize + Spacing.SM, logoY + 14, theme.textMuted(), false);

		int footerY = y + height - padding - font.lineHeight;
		context.text(font, "Cronis v1.0.0", x + padding, footerY, theme.textMuted(), false);
	}

	@Override
	protected void renderChildren(GuiGraphicsExtractor context, Font font) {
		for (GuiSidebarItem item : items) {
			item.render(context, font);
		}
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		for (GuiSidebarItem item : items) {
			item.update(delta, mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		for (GuiSidebarItem item : items) {
			if (item.mouseClicked(mouseX, mouseY, button)) {
				return true;
			}
		}
		return false;
	}

	private void selectItem(GuiSidebarItem item) {
		if (selectedItem != null) {
			selectedItem.setSelected(false);
		}

		selectedItem = item;
		selectedItem.setSelected(true);

		if (selectionListener != null) {
			selectionListener.accept(item);
		}
	}
}
