package dev.cronis.gui.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Filled rounded surfaces used by cards, controls, and panels.
 */
public final class SurfaceRenderer {
	private SurfaceRenderer() {
	}

	public static void fill(GuiGraphicsExtractor context, int x, int y, int width, int height, int radius, int color) {
		RoundedRectGeometry.fill(context, x, y, width, height, radius, color);
	}
}
