package dev.cronis.widget;

/**
 * Preferred dimensions reported by a widget.
 *
 * @param width  preferred width in GUI pixels
 * @param height preferred height in GUI pixels
 */
public record WidgetSize(int width, int height) {
	/**
	 * Returns a zero-sized dimension.
	 *
	 * @return zero size
	 */
	public static WidgetSize zero() {
		return new WidgetSize(0, 0);
	}
}
