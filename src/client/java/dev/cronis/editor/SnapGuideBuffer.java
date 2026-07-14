package dev.cronis.editor;

/**
 * Reusable fixed-capacity buffer for snap guides to avoid allocations during drag.
 */
public final class SnapGuideBuffer {
	public static final int MAX_GUIDES = 16;

	private final SnapGuide[] guides = new SnapGuide[MAX_GUIDES];
	private int count;

	public void clear() {
		count = 0;
	}

	public int count() {
		return count;
	}

	public SnapGuide get(int index) {
		return guides[index];
	}

	public void add(SnapGuide guide) {
		if (count >= MAX_GUIDES) {
			return;
		}

		guides[count++] = guide;
	}

	public void addVertical(int position, int start, int end, SnapGuide.SnapGuideType type) {
		add(new SnapGuide(SnapGuide.SnapGuideAxis.VERTICAL, position, start, end, type));
	}

	public void addHorizontal(int position, int start, int end, SnapGuide.SnapGuideType type) {
		add(new SnapGuide(SnapGuide.SnapGuideAxis.HORIZONTAL, position, start, end, type));
	}
}
