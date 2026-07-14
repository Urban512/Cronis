package dev.cronis.widget;

/**
 * Persistent widget placement relative to a {@link WidgetAnchor}.
 *
 * @param offsetX horizontal offset from the anchor origin in GUI pixels
 * @param offsetY vertical offset from the anchor origin in GUI pixels
 */
public record WidgetPosition(float offsetX, float offsetY) {
	/**
	 * Returns a position with zero offsets.
	 *
	 * @return origin position
	 */
	public static WidgetPosition zero() {
		return new WidgetPosition(0f, 0f);
	}

	/**
	 * Returns a copy with updated offsets.
	 *
	 * @param offsetX horizontal offset
	 * @param offsetY vertical offset
	 * @return updated position
	 */
	public WidgetPosition withOffsets(float offsetX, float offsetY) {
		return new WidgetPosition(offsetX, offsetY);
	}
}
