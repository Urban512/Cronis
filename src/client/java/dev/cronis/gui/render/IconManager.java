package dev.cronis.gui.render;

import dev.cronis.assets.AssetManager;
import dev.cronis.assets.AssetRegistry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

/**
 * Draws Cronis PNG icons from the asset pipeline with vector fallbacks.
 */
public final class IconManager {
	private IconManager() {
	}

	public enum Icon {
		SETTINGS("settings"),
		DISCORD("discord"),
		GITHUB("github"),
		SEARCH("search"),
		CLOSE("close"),
		MUSIC("music"),
		SPOTIFY("spotify"),
		WARNING("warning"),
		SUCCESS("success"),
		ARROW_LEFT("arrow_left"),
		ARROW_RIGHT("arrow_right");

		private final String assetName;

		Icon(String assetName) {
			this.assetName = assetName;
		}

		public String assetName() {
			return assetName;
		}

		public Identifier textureId() {
			return AssetManager.icon(assetName + ".png");
		}
	}

	public static Identifier get(Icon icon) {
		return icon.textureId();
	}

	public static void draw(GuiGraphicsExtractor context, Icon icon, int x, int y, int size, int color) {
		if (size <= 0) {
			return;
		}

		Identifier texture = icon.textureId();
		if (AssetRegistry.isAvailable(texture)) {
			drawTexture(context, texture, x, y, size, color);
			return;
		}

		LegacyIconRenderer.draw(context, toLegacyIcon(icon), x, y, size, color);
	}

	public static void draw(GuiGraphicsExtractor context, String iconName, int x, int y, int size, int color) {
		Identifier texture = AssetManager.icon(iconName.endsWith(".png") ? iconName : iconName + ".png");
		if (AssetRegistry.isAvailable(texture)) {
			drawTexture(context, texture, x, y, size, color);
			return;
		}

		Icon resolved = resolveIcon(iconName);
		if (resolved != null) {
			draw(context, resolved, x, y, size, color);
		}
	}

	private static void drawTexture(
			GuiGraphicsExtractor context,
			Identifier texture,
			int x,
			int y,
			int size,
			int color
	) {
		if (ColorUtil.alpha(color) <= 0) {
			return;
		}

		context.blit(texture, x, y, x + size, y + size, 0f, 1f, 0f, 1f);
	}

	private static Icon resolveIcon(String iconName) {
		String normalized = iconName.toLowerCase().replace(".png", "");
		for (Icon icon : Icon.values()) {
			if (icon.assetName().equals(normalized)) {
				return icon;
			}
		}
		return null;
	}

	private static LegacyIconRenderer.Icon toLegacyIcon(Icon icon) {
		return switch (icon) {
			case SETTINGS -> LegacyIconRenderer.Icon.SETTINGS;
			case DISCORD -> LegacyIconRenderer.Icon.DISCORD;
			case GITHUB -> LegacyIconRenderer.Icon.GITHUB;
			case SEARCH -> LegacyIconRenderer.Icon.SEARCH;
			default -> LegacyIconRenderer.Icon.SETTINGS;
		};
	}
}
