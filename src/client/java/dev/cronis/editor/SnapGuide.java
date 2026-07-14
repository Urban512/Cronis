package dev.cronis.editor;

/**
 * A single snap guide line rendered during drag or resize operations.
 *
 * @param axis      guide orientation
 * @param position  line position in GUI pixels
 * @param start     line start coordinate on the orthogonal axis
 * @param end       line end coordinate on the orthogonal axis
 * @param guideType snap target category
 */
public record SnapGuide(
		SnapGuideAxis axis,
		int position,
		int start,
		int end,
		SnapGuideType guideType
) {
	/**
	 * Guide orientation.
	 */
	public enum SnapGuideAxis {
		VERTICAL,
		HORIZONTAL
	}

	/**
	 * Snap target category.
	 */
	public enum SnapGuideType {
		SCREEN_EDGE,
		SCREEN_CENTER,
		WIDGET_EDGE,
		GRID
	}
}
