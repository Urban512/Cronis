package dev.cronis.gui.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Draws rounded rectangles using shared Cronis geometry.
 */
public final class RoundedRenderer {
	private RoundedRenderer() {
	}

	public static void fill(GuiGraphicsExtractor context, int x, int y, int width, int height, int radius, int color) {
		SurfaceRenderer.fill(context, x, y, width, height, radius, color);
	}

	public static void outline(
			GuiGraphicsExtractor context,
			int x,
			int y,
			int width,
			int height,
			int radius,
			int thickness,
			int color
	) {
		BorderRenderer.draw(context, x, y, width, height, radius, thickness, color);
	}
}
