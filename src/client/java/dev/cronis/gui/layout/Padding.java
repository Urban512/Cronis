package dev.cronis.gui.layout;

/**
 * Immutable internal spacing applied inside a component boundary.
 *
 * @param top    inset from the top edge in pixels
 * @param right  inset from the right edge in pixels
 * @param bottom inset from the bottom edge in pixels
 * @param left   inset from the left edge in pixels
 */
public record Padding(int top, int right, int bottom, int left) {
	/**
	 * Creates padding with the same inset on every edge.
	 *
	 * @param amount inset applied to all edges
	 * @return uniform padding
	 */
	public static Padding all(int amount) {
		return new Padding(amount, amount, amount, amount);
	}

	/**
	 * Creates padding with independent vertical and horizontal insets.
	 *
	 * @param vertical   combined top and bottom inset
	 * @param horizontal combined left and right inset
	 * @return symmetric padding
	 */
	public static Padding symmetric(int vertical, int horizontal) {
		return new Padding(vertical, horizontal, vertical, horizontal);
	}

	/**
	 * Returns the total horizontal inset.
	 *
	 * @return sum of the left and right insets
	 */
	public int horizontal() {
		return left + right;
	}

	/**
	 * Returns the total vertical inset.
	 *
	 * @return sum of the top and bottom insets
	 */
	public int vertical() {
		return top + bottom;
	}
}
