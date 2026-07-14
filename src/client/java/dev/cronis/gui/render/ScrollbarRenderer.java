package dev.cronis.gui.render;

import dev.cronis.gui.animation.Easing;
import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.render.RoundedRenderer;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Draws a thin custom scrollbar for scrollable Cronis panels.
 */
public final class ScrollbarRenderer {
	private ScrollbarRenderer() {
	}

	/**
	 * Draws a vertical scrollbar when content overflows the viewport.
	 *
	 * @param context       the draw context
	 * @param x             scrollbar track X position
	 * @param y             viewport Y position
	 * @param height        viewport height
	 * @param scrollOffset  current scroll offset in pixels
	 * @param contentHeight total content height in pixels
	 * @param thumbHover    normalized hover amount in the range {@code 0.0-1.0}
	 */
	public static void drawVertical(
			GuiGraphicsExtractor context,
			int x,
			int y,
			int height,
			float scrollOffset,
			int contentHeight,
			float thumbHover
	) {
		if (contentHeight <= height) {
			return;
		}

		var theme = ThemeManager.get();
		int trackWidth = 4;
		int trackX = x - trackWidth - 2;
		RoundedRenderer.fill(context, trackX, y, trackWidth, height, 2, theme.scrollbarTrack());

		float visibleRatio = (float) height / contentHeight;
		int thumbHeight = Math.max(18, Math.round(height * visibleRatio));
		float maxOffset = contentHeight - height;
		float scrollRatio = maxOffset <= 0f ? 0f : scrollOffset / maxOffset;
		int thumbY = y + Math.round((height - thumbHeight) * scrollRatio);
		int thumbColor = ColorUtil.lerp(theme.scrollbarThumb(), theme.scrollbarThumbHover(), thumbHover);
		RoundedRenderer.fill(context, trackX, thumbY, trackWidth, thumbHeight, 2, thumbColor);
	}

	/**
	 * Draws soft edge fades when scrollable content overflows.
	 *
	 * @param context       the draw context
	 * @param x             viewport X position
	 * @param y             viewport Y position
	 * @param width         viewport width
	 * @param height        viewport height
	 * @param scrollOffset  current scroll offset in pixels
	 * @param contentHeight total content height in pixels
	 */
	public static void drawEdgeFades(
			GuiGraphicsExtractor context,
			int x,
			int y,
			int width,
			int height,
			float scrollOffset,
			int contentHeight
	) {
		if (contentHeight <= height) {
			return;
		}

		var theme = ThemeManager.get();
		int fadeHeight = 16;
		float topFade = Math.min(1f, scrollOffset / fadeHeight);
		float bottomFade = Math.min(1f, (contentHeight - height - scrollOffset) / fadeHeight);

		if (topFade > 0f) {
			drawFadeStrip(context, x, y, width, fadeHeight, true, theme.overlayFade(), topFade);
		}
		if (bottomFade > 0f) {
			drawFadeStrip(context, x, y + height - fadeHeight, width, fadeHeight, false, theme.overlayFade(), bottomFade);
		}
	}

	private static void drawFadeStrip(
			GuiGraphicsExtractor context,
			int x,
			int y,
			int width,
			int height,
			boolean top,
			int surfaceColor,
			float strength
	) {
		int layers = 6;
		for (int layer = 0; layer < layers; layer++) {
			float progress = (float) (layer + 1) / layers;
			float alpha = Easing.easeOutCubic(progress) * strength;
			int layerHeight = Math.max(1, height / layers);
			int layerY = top ? y + layer * layerHeight : y + height - (layer + 1) * layerHeight;
			context.fill(x, layerY, x + width, layerY + layerHeight, ColorUtil.withAlpha(surfaceColor, alpha));
		}
	}
}
