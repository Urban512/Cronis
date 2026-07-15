package dev.cronis.gui.render;

import dev.cronis.gui.theme.DesignTokens;
import dev.cronis.gui.theme.GuiMetrics;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Former elevation helper. Shadows are disabled — methods are intentionally empty.
 */
public final class ElevationRenderer {
	private ElevationRenderer() {
	}

	public static void draw(
			GuiGraphicsExtractor context,
			int x,
			int y,
			int width,
			int height,
			int cornerRadius,
			DesignTokens.Elevation elevation,
			int shadowColor
	) {
		// Flat UI: no elevation shadows.
	}

	public static void draw(
			GuiGraphicsExtractor context,
			int x,
			int y,
			int width,
			int height,
			int cornerRadius,
			GuiMetrics.Elevation elevation,
			int shadowColor
	) {
		// Flat UI: no elevation shadows.
	}
}
