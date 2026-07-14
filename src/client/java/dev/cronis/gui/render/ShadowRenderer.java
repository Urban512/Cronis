package dev.cronis.gui.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Draws soft drop shadows for elevated Cronis interface elements.
 */
public final class ShadowRenderer {
	private ShadowRenderer() {
	}

	/**
	 * Draws a soft shadow behind a rounded rectangle.
	 */
	public static void draw(
			GuiGraphicsExtractor context,
			int x,
			int y,
			int width,
			int height,
			int cornerRadius,
			int shadowRadius,
			float opacity,
			int shadowColor
	) {
		if (width <= 0 || height <= 0 || shadowRadius <= 0 || opacity <= 0f) {
			return;
		}

		float clampedOpacity = Math.min(1f, Math.max(0f, opacity));
		int layers = Math.max(1, shadowRadius);

		for (int spread = layers; spread >= 1; spread--) {
			float layerWeight = (float) spread / layers;
			float layerOpacity = clampedOpacity * layerWeight * layerWeight * 0.35f;
			int layerColor = ColorUtil.withAlpha(shadowColor, layerOpacity);
			int offsetY = spread / 2;

			RoundedRenderer.fill(
					context,
					x - spread,
					y - spread + offsetY,
					width + spread * 2,
					height + spread * 2,
					cornerRadius + spread / 2,
					layerColor
			);
		}
	}
}
