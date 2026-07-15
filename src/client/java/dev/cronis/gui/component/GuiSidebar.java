package dev.cronis.gui.component;

import dev.cronis.gui.layout.Padding;
import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.layout.VerticalLayout;
import dev.cronis.gui.render.RoundedRenderer;
import dev.cronis.gui.theme.GuiMetrics;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Left navigation sidebar with branding, scrollable categories, and footer.
 */
public class GuiSidebar extends GuiComponent {
	public static final int WIDTH = GuiMetrics.SIDEBAR_WIDTH;

	private static final String VERSION_LABEL = "Cronis v1.0.0";

	private static final String[] CATEGORIES = {
			"General", "HUD", "Spotify", "Modules", "SkyBlock", "Dungeon",
			"Mining", "Fishing", "Garden", "Combat", "Profiles", "Developer"
	};

	private final List<GuiSidebarItem> items = new ArrayList<>();
	private final GuiScrollPanel navScrollPanel = new GuiScrollPanel(new VerticalLayout(Spacing.XS));
	private GuiSidebarItem selectedItem;
	private Consumer<GuiSidebarItem> selectionListener;

	public GuiSidebar() {
		this.width = WIDTH;
		navScrollPanel.setContentPadding(Padding.symmetric(Spacing.XS, 0));

		for (String category : CATEGORIES) {
			GuiSidebarItem item = new GuiSidebarItem(category);
			item.setOnSelect(this::selectItem);
			items.add(item);
			navScrollPanel.addChild(item);
		}

		addChild(navScrollPanel);

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
		Font font = Minecraft.getInstance().font;

		int padding = GuiMetrics.PADDING_PANEL.left();
		int headerHeight = headerSectionHeight(font, padding);
		int footerHeight = footerSectionHeight(font, padding);
		int navY = y + headerHeight;
		int navHeight = Math.max(0, height - headerHeight - footerHeight);
		int navInset = Spacing.SM;

		navScrollPanel.setBounds(x + navInset, navY, WIDTH - navInset * 2, navHeight);
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		var theme = ThemeManager.get();
		context.fill(x, y, x + width, y + height, theme.sidebarBackground());
		context.fill(x + width - 1, y, x + width, y + height, theme.headerDivider());

		int padding = GuiMetrics.PADDING_PANEL.left();
		int logoX = x + padding;
		int logoY = y + padding;
		RoundedRenderer.fill(
				context,
				logoX,
				logoY,
				GuiMetrics.LOGO_SIZE,
				GuiMetrics.LOGO_SIZE,
				GuiMetrics.LOGO_RADIUS,
				theme.logoAccent()
		);
		int brandX = logoX + GuiMetrics.LOGO_SIZE + Spacing.MD;
		context.text(font, "Cronis", brandX, logoY + Spacing.XS, theme.textPrimary(), false);
		context.text(font, "SkyBlock HUD", brandX, logoY + font.lineHeight + Spacing.XS, theme.textMuted(), false);

		int footerY = y + height - padding - font.lineHeight;
		context.text(font, VERSION_LABEL, x + padding, footerY, theme.textMuted(), false);
	}

	@Override
	protected void renderChildren(GuiGraphicsExtractor context, Font font) {
		navScrollPanel.render(context, font);
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		navScrollPanel.update(delta, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return navScrollPanel.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		return navScrollPanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	private int headerSectionHeight(Font font, int padding) {
		int titleBlockHeight = font.lineHeight * 2 + Spacing.SM;
		return padding + Math.max(GuiMetrics.LOGO_SIZE, titleBlockHeight) + Spacing.LG;
	}

	private int footerSectionHeight(Font font, int padding) {
		return padding + font.lineHeight + Spacing.LG;
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
