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

	/**
	 * Returns whether a point lies inside the bounds.
	 *
	 * @param pointX point X coordinate in GUI pixels
	 * @param pointY point Y coordinate in GUI pixels
	 * @return {@code true} when the point is inside the bounds
	 */
	public boolean contains(int pointX, int pointY) {
		return pointX >= x && pointX < right() && pointY >= y && pointY < bottom();
	}

	/**
	 * Returns a copy translated by the provided delta.
	 *
	 * @param deltaX horizontal delta in GUI pixels
	 * @param deltaY vertical delta in GUI pixels
	 * @return translated bounds
	 */
	public WidgetBounds translated(int deltaX, int deltaY) {
		return new WidgetBounds(x + deltaX, y + deltaY, width, height);
	}

	/**
	 * Returns a copy with updated dimensions while preserving the origin.
	 *
	 * @param newWidth  updated width in GUI pixels
	 * @param newHeight updated height in GUI pixels
	 * @return resized bounds
	 */
	public WidgetBounds withSize(int newWidth, int newHeight) {
		return new WidgetBounds(x, y, newWidth, newHeight);
	}

	/**
	 * Clamps the bounds so they remain fully inside the viewport.
	 *
	 * @param screenWidth  viewport width
	 * @param screenHeight viewport height
	 * @return clamped bounds
	 */
	public WidgetBounds clampedToViewport(int screenWidth, int screenHeight) {
		if (width <= 0 || height <= 0) {
			return this;
		}

		int maxX = Math.max(0, screenWidth - width);
		int maxY = Math.max(0, screenHeight - height);
		return new WidgetBounds(Math.clamp(x, 0, maxX), Math.clamp(y, 0, maxY), width, height);
	}
}
