package dev.cronis.gui.component;

import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.render.IconRenderer;
import dev.cronis.gui.theme.ThemeManager;
import dev.cronis.util.CronisLinks;
import dev.cronis.util.LinkOpener;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Top application header containing search and action icons.
 */
public class GuiHeader extends GuiComponent {
	public static final int HEIGHT = 56;

	private static final int ICON_GAP = Spacing.MD;

	private final GuiSearchBar searchBar = new GuiSearchBar("Search Cronis...");
	private final GuiIconButton settingsButton = new GuiIconButton(IconRenderer.Icon.SETTINGS);
	private final GuiIconButton discordButton = new GuiIconButton(IconRenderer.Icon.DISCORD)
			.setOnClick(() -> LinkOpener.open(CronisLinks.DISCORD));
	private final GuiIconButton githubButton = new GuiIconButton(IconRenderer.Icon.GITHUB);

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
		int iconWidth = settingsButton.getPreferredWidth(HEIGHT);
		int iconGroupWidth = iconWidth * 3 + ICON_GAP * 2;
		int searchWidth = searchBar.resolveWidth(width - padding * 2 - iconGroupWidth - Spacing.LG);
		int searchY = y + (HEIGHT - searchBar.getPreferredHeight(width)) / 2;
		searchBar.setBounds(x + padding, searchY, searchWidth, searchBar.getPreferredHeight(width));

		int iconY = y + (HEIGHT - settingsButton.getPreferredHeight(width)) / 2;
		int iconX = x + width - padding - iconWidth;
		githubButton.setBounds(iconX, iconY, iconWidth, settingsButton.getPreferredHeight(width));
		discordButton.setBounds(iconX - ICON_GAP - iconWidth, iconY, iconWidth, settingsButton.getPreferredHeight(width));
		settingsButton.setBounds(discordButton.getBounds().x() - ICON_GAP - iconWidth, iconY, iconWidth, settingsButton.getPreferredHeight(width));
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
