package dev.cronis.editor;

import dev.cronis.widget.WidgetContext;

/**
 * Immutable per-frame context shared by editor interaction and overlay systems.
 */
public final class WidgetEditorContext {
	private final WidgetContext widgetContext;
	private final int mouseX;
	private final int mouseY;
	private final WidgetGrid grid;
	private final boolean showGrid;
	private final boolean showSafeArea;
	private final int safeAreaPadding;

	public WidgetEditorContext(
			WidgetContext widgetContext,
			int mouseX,
			int mouseY,
			WidgetGrid grid,
			boolean showGrid,
			boolean showSafeArea,
			int safeAreaPadding
	) {
		this.widgetContext = widgetContext;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.grid = grid;
		this.showGrid = showGrid;
		this.showSafeArea = showSafeArea;
		this.safeAreaPadding = safeAreaPadding;
	}

	public WidgetContext widgetContext() {
		return widgetContext;
	}

	public int mouseX() {
		return mouseX;
	}

	public int mouseY() {
		return mouseY;
	}

	public WidgetGrid grid() {
		return grid;
	}

	public boolean showGrid() {
		return showGrid;
	}

	public boolean showSafeArea() {
		return showSafeArea;
	}

	public int safeAreaPadding() {
		return safeAreaPadding;
	}

	public int screenWidth() {
		return widgetContext.screenWidth();
	}

	public int screenHeight() {
		return widgetContext.screenHeight();
	}
}
