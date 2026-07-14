package dev.cronis.gui.component;

import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.settings.SettingGroupPanel;
import dev.cronis.gui.theme.ThemeManager;
import dev.cronis.widget.Widget;
import dev.cronis.widget.WidgetManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Scrollable main content panel for the Cronis settings GUI.
 */
public class GuiContentPanel extends GuiComponent {
	public static final String SPOTIFY_TAB = "Spotify";

	private final GuiScrollPanel scrollPanel = new GuiScrollPanel(new dev.cronis.gui.layout.VerticalLayout(Spacing.LG));
	private final SettingGroupPanel spotifySettingsPanel = new SettingGroupPanel();
	private String activeCategory = "General";

	public GuiContentPanel() {
		addChild(scrollPanel);
		showCategory(activeCategory);
	}

	/**
	 * Switches the visible content to the selected sidebar category.
	 *
	 * @param category sidebar category label
	 */
	public void showCategory(String category) {
		activeCategory = category == null || category.isBlank() ? "General" : category;
		scrollPanel.clearChildren();

		if (SPOTIFY_TAB.equals(activeCategory)) {
			showSpotifyContent();
			return;
		}

		if ("General".equals(activeCategory)) {
			showGeneralContent();
			return;
		}

		scrollPanel.addChild(GuiLabel.heading(activeCategory));
		scrollPanel.addChild(new GuiCard(activeCategory, "Settings for this section are coming soon."));
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

	private void showGeneralContent() {
		scrollPanel.addChild(GuiLabel.heading("Welcome to Cronis"));
		scrollPanel.addChild(new GuiCard("Welcome", "Your premium SkyBlock HUD framework."));
		scrollPanel.addChild(new GuiCard("Latest News", "Cronis is in early development."));
		scrollPanel.addChild(new GuiCard("Installed Modules", "No modules installed yet."));
		scrollPanel.addChild(new GuiCard("Performance", "Rendering optimized for smooth gameplay."));
	}

	private void showSpotifyContent() {
		scrollPanel.addChild(GuiLabel.heading("Spotify"));

		WidgetManager.get()
				.getWidget("spotify")
				.map(Widget::getSettings)
				.ifPresentOrElse(
						spotifySettingsPanel::bind,
						() -> spotifySettingsPanel.bind(null)
				);

		if (spotifySettingsPanel.getChildren().isEmpty()) {
			scrollPanel.addChild(new GuiCard("Spotify", "The Spotify widget is not available."));
			return;
		}

		scrollPanel.addChild(spotifySettingsPanel);
	}
}
