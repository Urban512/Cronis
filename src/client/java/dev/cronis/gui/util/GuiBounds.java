package dev.cronis.gui.util;

/**
 * Immutable rectangular bounds of a GUI element in screen space.
 *
 * @param x      origin X position
 * @param y      origin Y position
 * @param width  width in pixels
 * @param height height in pixels
 */
public record GuiBounds(int x, int y, int width, int height) {
	/**
	 * Returns the right edge of the bounds.
	 *
	 * @return x coordinate of the right edge
	 */
	public int right() {
		return x + width;
	}

	/**
	 * Returns the bottom edge of the bounds.
	 *
	 * @return y coordinate of the bottom edge
	 */
	public int bottom() {
		return y + height;
	}
}
