package dev.cronis.gui.component;

import dev.cronis.gui.animation.FadeAnimation;
import dev.cronis.gui.layout.Padding;
import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.layout.VerticalLayout;
import dev.cronis.gui.render.RoundedRenderer;
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
	public static final int WIDTH = 220;

	private static final int LOGO_SIZE = 30;
	private static final int LOGO_RADIUS = 9;
	private static final String VERSION_LABEL = "Cronis v1.0.0";

	private static final String[] CATEGORIES = {
			"General", "HUD", "Modules", "SkyBlock", "Dungeon",
			"Mining", "Fishing", "Garden", "Combat", "Profiles", "Developer"
	};

	private final List<GuiSidebarItem> items = new ArrayList<>();
	private final GuiScrollPanel navScrollPanel = new GuiScrollPanel(new VerticalLayout(Spacing.SM));
	private GuiSidebarItem selectedItem;
	private Consumer<GuiSidebarItem> selectionListener;

	public GuiSidebar() {
		this.width = WIDTH;
		navScrollPanel.setContentPadding(Padding.symmetric(Spacing.SM, 0));

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

		int padding = Spacing.LG;
		int headerHeight = headerSectionHeight(font, padding);
		int footerHeight = footerSectionHeight(font, padding);
		int navY = y + headerHeight;
		int navHeight = Math.max(0, height - headerHeight - footerHeight);
		int navInset = Spacing.MD;

		navScrollPanel.setBounds(x + navInset, navY, WIDTH - navInset * 2, navHeight);
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		var theme = ThemeManager.get();
		context.fill(x, y, x + width, y + height, theme.sidebarBackground());
		context.fill(x + width - 1, y, x + width, y + height, theme.headerDivider());

		int padding = Spacing.LG;
		int logoX = x + padding;
		int logoY = y + padding;
		RoundedRenderer.fill(context, logoX, logoY, LOGO_SIZE, LOGO_SIZE, LOGO_RADIUS, theme.logoAccent());
		context.text(font, "Cronis", logoX + LOGO_SIZE + Spacing.MD, logoY + 2, theme.textPrimary(), false);
		context.text(font, "SkyBlock HUD", logoX + LOGO_SIZE + Spacing.MD, logoY + font.lineHeight + 3, theme.textMuted(), false);

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
		int titleBlockHeight = font.lineHeight * 2 + Spacing.XS + 2;
		return padding + Math.max(LOGO_SIZE, titleBlockHeight) + Spacing.MD;
	}

	private int footerSectionHeight(Font font, int padding) {
		return padding + font.lineHeight + Spacing.MD;
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
