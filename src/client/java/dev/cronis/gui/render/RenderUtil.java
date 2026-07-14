package dev.cronis.gui.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * General-purpose rendering helpers for Cronis GUI code.
 */
public final class RenderUtil {
	private RenderUtil() {
	}

	/**
	 * Horizontal text alignment options for bounded text layout.
	 */
	public enum TextAlignment {
		LEFT,
		CENTER,
		RIGHT
	}

	/**
	 * Returns the horizontal offset needed to center an element inside a container.
	 *
	 * @param containerWidth available container width
	 * @param elementWidth   element width
	 * @return centered X offset relative to the container origin
	 */
	public static int centerX(int containerWidth, int elementWidth) {
		return Math.max(0, (containerWidth - elementWidth) / 2);
	}

	/**
	 * Returns the vertical offset needed to center an element inside a container.
	 *
	 * @param containerHeight available container height
	 * @param elementHeight   element height
	 * @return centered Y offset relative to the container origin
	 */
	public static int centerY(int containerHeight, int elementHeight) {
		return Math.max(0, (containerHeight - elementHeight) / 2);
	}

	/**
	 * Returns the scaled GUI width of the current window.
	 *
	 * @return scaled width in pixels
	 */
	public static int scaledWidth() {
		return Minecraft.getInstance().getWindow().getGuiScaledWidth();
	}

	/**
	 * Returns the scaled GUI height of the current window.
	 *
	 * @return scaled height in pixels
	 */
	public static int scaledHeight() {
		return Minecraft.getInstance().getWindow().getGuiScaledHeight();
	}

	/**
	 * Returns the ratio between the physical window width and the scaled GUI width.
	 *
	 * @return current GUI scale factor
	 */
	public static double scaleFactor() {
		return (double) Minecraft.getInstance().getWindow().getWidth() / scaledWidth();
	}

	/**
	 * Executes rendering within a temporary scissor region.
	 *
	 * @param context the draw context
	 * @param x       scissor region X position
	 * @param y       scissor region Y position
	 * @param width   scissor region width
	 * @param height  scissor region height
	 * @param action  rendering action to execute inside the scissor region
	 */
	public static void scissor(GuiGraphicsExtractor context, int x, int y, int width, int height, Runnable action) {
		context.enableScissor(x, y, x + width, y + height);
		try {
			action.run();
		} finally {
			context.disableScissor();
		}
	}

	/**
	 * Draws text within a horizontal boundary using the requested alignment.
	 *
	 * @param context   the draw context
	 * @param font      font used to measure and draw the text
	 * @param text      text content
	 * @param x         boundary X position
	 * @param y         text Y position
	 * @param width     boundary width used for alignment
	 * @param color     text color in ARGB format
	 * @param alignment horizontal alignment within the boundary
	 * @param shadow    whether to draw a drop shadow
	 */
	public static void drawAlignedText(
			GuiGraphicsExtractor context,
			Font font,
			String text,
			int x,
			int y,
			int width,
			int color,
			TextAlignment alignment,
			boolean shadow
	) {
		int drawX = switch (alignment) {
			case LEFT -> x;
			case CENTER -> x + centerX(width, font.width(text));
			case RIGHT -> x + Math.max(0, width - font.width(text));
		};
		context.text(font, text, drawX, y, color, shadow);
	}
}
