package dev.cronis.gui.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Abstraction for backdrop blur rendering in the Cronis GUI.
 * <p>
 * Background blur is owned by Minecraft's screen pipeline and may only be requested
 * once per frame through {@link net.minecraft.client.gui.screens.Screen#extractBlurredBackground}.
 * This class exposes a stable Cronis API for future localized blur work without issuing
 * additional blur passes.
 */
public final class BlurRenderer {
	/**
	 * Creates a blur renderer.
	 */
	public BlurRenderer() {
	}

	/**
	 * Begins a blur capture pass.
	 *
	 * @param context the draw context
	 */
	public void beginBlur(GuiGraphicsExtractor context) {
	}

	/**
	 * Ends the active blur capture pass.
	 *
	 * @param context the draw context
	 */
	public void endBlur(GuiGraphicsExtractor context) {
	}

	/**
	 * Draws a blurred area at the requested bounds.
	 *
	 * @param context the draw context
	 * @param x       area X position
	 * @param y       area Y position
	 * @param width   area width
	 * @param height  area height
	 * @param radius  corner radius of the blurred area
	 */
	public void drawBlurArea(GuiGraphicsExtractor context, int x, int y, int width, int height, int radius) {
	}
}
