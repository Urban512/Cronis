package dev.cronis.gui.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Rounded borders that share geometry with {@link SurfaceRenderer}.
 */
public final class BorderRenderer {
	private BorderRenderer() {
	}

	public static void draw(
			GuiGraphicsExtractor context,
			int x,
			int y,
			int width,
			int height,
			int radius,
			int thickness,
			int color
	) {
		RoundedRectGeometry.ring(context, x, y, width, height, radius, thickness, color);
	}
}
