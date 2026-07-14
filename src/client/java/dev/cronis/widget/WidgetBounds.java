package dev.cronis.widget;

/**
 * Resolved screen-space bounds of a widget.
 *
 * @param x      origin X position in GUI coordinates
 * @param y      origin Y position in GUI coordinates
 * @param width  width in GUI pixels
 * @param height height in GUI pixels
 */
public record WidgetBounds(int x, int y, int width, int height) {
	/**
	 * Returns zero bounds.
	 *
	 * @return zero bounds
	 */
	public static WidgetBounds zero() {
		return new WidgetBounds(0, 0, 0, 0);
	}

	/**
	 * Returns the right edge of the bounds.
	 *
	 * @return right edge X coordinate
	 */
	public int right() {
		return x + width;
	}

	/**
	 * Returns the bottom edge of the bounds.
	 *
	 * @return bottom edge Y coordinate
	 */
	public int bottom() {
		return y + height;
	}

	/**
	 * Returns whether the bounds are visible inside the viewport.
	 *
	 * @param screenWidth  viewport width
	 * @param screenHeight viewport height
	 * @return {@code true} when any part of the bounds intersects the viewport
	 */
	public boolean intersectsViewport(int screenWidth, int screenHeight) {
		return x < screenWidth && right() > 0 && y < screenHeight && bottom() > 0;
	}
}
