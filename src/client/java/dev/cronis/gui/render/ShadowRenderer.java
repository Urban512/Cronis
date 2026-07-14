package dev.cronis.gui.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Draws soft drop shadows for elevated Cronis interface elements.
 * <p>
 * Shadows are built from layered rounded shapes with decreasing opacity to create
 * a smooth falloff without custom shader code.
 */
public final class ShadowRenderer {
	private ShadowRenderer() {
	}

	/**
	 * Draws a soft shadow behind a rounded rectangle.
	 *
	 * @param context      the draw context
	 * @param x            element X position
	 * @param y            element Y position
	 * @param width        element width
	 * @param height       element height
	 * @param cornerRadius element corner radius
	 * @param shadowRadius shadow spread in pixels
	 * @param opacity      maximum shadow opacity in the range {@code 0.0-1.0}
	 */
	public static void draw(
			GuiGraphicsExtractor context,
			int x,
			int y,
			int width,
			int height,
			int cornerRadius,
			int shadowRadius,
			float opacity
	) {
		if (width <= 0 || height <= 0 || shadowRadius <= 0 || opacity <= 0f) {
			return;
		}

		float clampedOpacity = Math.min(1f, Math.max(0f, opacity));
		int layers = Math.max(1, shadowRadius);

		for (int spread = layers; spread >= 1; spread--) {
			float layerWeight = (float) spread / layers;
			float layerOpacity = clampedOpacity * layerWeight * layerWeight * 0.35f;
			int shadowColor = ColorUtil.withAlpha(0xFF000000, layerOpacity);
			int offsetY = spread / 2;

			RoundedRenderer.fill(
					context,
					x - spread,
					y - spread + offsetY,
					width + spread * 2,
					height + spread * 2,
					cornerRadius + spread / 2,
					shadowColor
			);
		}
	}
}
