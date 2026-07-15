package dev.cronis.gui.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Backward-compatible entry point that delegates to {@link IconManager}.
 */
public final class IconRenderer {
	private IconRenderer() {
	}

	public enum Icon {
		SETTINGS,
		DISCORD,
		GITHUB,
		SEARCH
	}

	public static void draw(GuiGraphicsExtractor context, Icon icon, int x, int y, int size, int color) {
		IconManager.draw(context, toManagedIcon(icon), x, y, size, color);
	}

	private static IconManager.Icon toManagedIcon(Icon icon) {
		return switch (icon) {
			case SETTINGS -> IconManager.Icon.SETTINGS;
			case DISCORD -> IconManager.Icon.DISCORD;
			case GITHUB -> IconManager.Icon.GITHUB;
			case SEARCH -> IconManager.Icon.SEARCH;
		};
	}
}
