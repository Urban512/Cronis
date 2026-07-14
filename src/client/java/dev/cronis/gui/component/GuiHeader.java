package dev.cronis.gui.component;

import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Top application header containing search and action icons.
 */
public class GuiHeader extends GuiComponent {
	public static final int HEIGHT = 52;

	private final GuiSearchBar searchBar = new GuiSearchBar("Search Cronis...");
	private final GuiIconButton settingsButton = new GuiIconButton("S");
	private final GuiIconButton discordButton = new GuiIconButton("D");
	private final GuiIconButton githubButton = new GuiIconButton("G");

	public GuiHeader() {
		this.height = HEIGHT;
		addChild(searchBar);
		addChild(settingsButton);
		addChild(discordButton);
		addChild(githubButton);
	}

	@Override
	public int getPreferredHeight(int availableWidth) {
		return HEIGHT;
	}

	public void layoutHeader(int x, int y, int width) {
		setBounds(x, y, width, HEIGHT);
		int padding = Spacing.LG;
		searchBar.setBounds(x + padding, y + (HEIGHT - searchBar.getPreferredHeight(width)) / 2, 240, searchBar.getPreferredHeight(width));

		int iconY = y + (HEIGHT - settingsButton.getPreferredHeight(width)) / 2;
		githubButton.setBounds(x + width - padding - githubButton.getPreferredWidth(HEIGHT), iconY, githubButton.getPreferredWidth(HEIGHT), githubButton.getPreferredHeight(width));
		discordButton.setBounds(githubButton.getBounds().x() - Spacing.SM - discordButton.getPreferredWidth(HEIGHT), iconY, discordButton.getPreferredWidth(HEIGHT), discordButton.getPreferredHeight(width));
		settingsButton.setBounds(discordButton.getBounds().x() - Spacing.SM - settingsButton.getPreferredWidth(HEIGHT), iconY, settingsButton.getPreferredWidth(HEIGHT), settingsButton.getPreferredHeight(width));
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		var theme = ThemeManager.get();
		context.fill(x, y, x + width, y + height, theme.headerBackground());
		context.fill(x, y + height - 1, x + width, y + height, theme.headerDivider());
	}

	@Override
	protected void renderChildren(GuiGraphicsExtractor context, Font font) {
		searchBar.render(context, font);
		settingsButton.render(context, font);
		discordButton.render(context, font);
		githubButton.render(context, font);
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		searchBar.update(delta, mouseX, mouseY);
		settingsButton.update(delta, mouseX, mouseY);
		discordButton.update(delta, mouseX, mouseY);
		githubButton.update(delta, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return searchBar.mouseClicked(mouseX, mouseY, button)
				|| settingsButton.mouseClicked(mouseX, mouseY, button)
				|| discordButton.mouseClicked(mouseX, mouseY, button)
				|| githubButton.mouseClicked(mouseX, mouseY, button);
	}
}
