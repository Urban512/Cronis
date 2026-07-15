package dev.cronis.gui.render;

import dev.cronis.gui.theme.DesignTokens;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Smooth pill-shaped progress indicators.
 */
public final class ProgressBarRenderer {
	private ProgressBarRenderer() {
	}

	public static void draw(
			GuiGraphicsExtractor context,
			int x,
			int y,
			int width,
			int height,
			float progress,
			int trackColor,
			int fillColor
	) {
		if (width <= 0 || height <= 0) {
			return;
		}

		int radius = Math.max(1, height / 2);
		SurfaceRenderer.fill(context, x, y, width, height, radius, trackColor);

		float clampedProgress = Math.max(0f, Math.min(1f, progress));
		if (clampedProgress <= 0f) {
			return;
		}

		int fillWidth = Math.max(height, Math.round(width * clampedProgress));
		if (fillWidth >= width) {
			SurfaceRenderer.fill(context, x, y, width, height, radius, fillColor);
			return;
		}

		RenderUtil.scissor(context, x, y, fillWidth, height, () ->
				SurfaceRenderer.fill(context, x, y, width, height, radius, fillColor)
		);
	}

	public static void draw(
			GuiGraphicsExtractor context,
			int x,
			int y,
			int width,
			float progress,
			int trackColor,
			int fillColor
	) {
		draw(context, x, y, width, DesignTokens.PROGRESS_HEIGHT, progress, trackColor, fillColor);
	}
}
