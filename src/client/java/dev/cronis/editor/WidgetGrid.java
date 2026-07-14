package dev.cronis.editor;

/**
 * Optional alignment grid used by the HUD editor for snapping and overlay rendering.
 */
public final class WidgetGrid {
	public static final int DEFAULT_CELL_SIZE = 8;

	private int cellSize = DEFAULT_CELL_SIZE;
	private boolean enabled;

	public int cellSize() {
		return cellSize;
	}

	public void setCellSize(int cellSize) {
		this.cellSize = Math.max(1, cellSize);
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Snaps a coordinate to the nearest grid line when enabled.
	 *
	 * @param coordinate coordinate in GUI pixels
	 * @return snapped coordinate
	 */
	public int snapCoordinate(int coordinate) {
		if (!enabled) {
			return coordinate;
		}

		return Math.round((float) coordinate / cellSize) * cellSize;
	}

	/**
	 * Snaps a dimension to the nearest grid increment when enabled.
	 *
	 * @param size dimension in GUI pixels
	 * @return snapped dimension
	 */
	public int snapSize(int size) {
		if (!enabled) {
			return size;
		}

		return Math.max(cellSize, snapCoordinate(size));
	}
}
