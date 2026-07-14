package dev.cronis.gui.component;

import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Scrollable main content panel with welcome text and placeholder cards.
 */
public class GuiContentPanel extends GuiComponent {
	private final GuiScrollPanel scrollPanel = new GuiScrollPanel(new dev.cronis.gui.layout.VerticalLayout(Spacing.LG));

	public GuiContentPanel() {
		addChild(scrollPanel);
		scrollPanel.addChild(GuiLabel.primary("Welcome to Cronis"));
		scrollPanel.addChild(new GuiCard("Welcome", "Your premium SkyBlock HUD framework."));
		scrollPanel.addChild(new GuiCard("Latest News", "Cronis is in early development."));
		scrollPanel.addChild(new GuiCard("Installed Modules", "No modules installed yet."));
		scrollPanel.addChild(new GuiCard("Performance", "Rendering optimized for smooth gameplay."));
	}

	public void layoutContent(int x, int y, int width, int height) {
		setBounds(x, y, width, height);
		int padding = Spacing.LG;
		scrollPanel.setBounds(x + padding, y + padding, width - padding * 2, height - padding * 2);
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		context.fill(x, y, x + width, y + height, ThemeManager.get().contentBackground());
	}

	@Override
	protected void renderChildren(GuiGraphicsExtractor context, Font font) {
		scrollPanel.render(context, font);
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		scrollPanel.update(delta, mouseX, mouseY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		return scrollPanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}
}
