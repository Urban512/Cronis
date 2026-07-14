package dev.cronis.editor;

import dev.cronis.widget.Widget;
import dev.cronis.widget.WidgetBounds;
import dev.cronis.widget.WidgetPosition;

/**
 * Handles widget drag interactions in screen space while preserving anchor semantics.
 */
public final class WidgetDragController {
	private boolean active;
	private Widget widget;
	private int startMouseX;
	private int startMouseY;
	private WidgetBounds startBounds;

	public boolean isActive() {
		return active;
	}

	public Widget widget() {
		return widget;
	}

	/**
	 * Begins dragging the provided widget from the current cursor position.
	 *
	 * @param widget      widget to drag
	 * @param mouseX      cursor X coordinate in GUI pixels
	 * @param mouseY      cursor Y coordinate in GUI pixels
	 * @param startBounds widget bounds at drag start
	 */
	public void begin(Widget widget, int mouseX, int mouseY, WidgetBounds startBounds) {
		this.active = true;
		this.widget = widget;
		this.startMouseX = mouseX;
		this.startMouseY = mouseY;
		this.startBounds = startBounds;
	}

	/**
	 * Updates the dragged widget using the current cursor position.
	 *
	 * @param editorContext editor frame context
	 * @param snapEngine    snap calculator
	 */
	public void update(WidgetEditorContext editorContext, WidgetSnapEngine snapEngine) {
		if (!active || widget == null) {
			return;
		}

		int deltaX = editorContext.mouseX() - startMouseX;
		int deltaY = editorContext.mouseY() - startMouseY;
		WidgetBounds proposed = startBounds.translated(deltaX, deltaY);
		WidgetBounds snapped = snapEngine.snapDrag(
				proposed,
				widget,
				editorContext.widgetContext(),
				editorContext.grid()
		);
		applyBounds(widget, snapped, editorContext);
	}

	/**
	 * Ends the active drag operation.
	 */
	public void end() {
		active = false;
		widget = null;
	}

	private static void applyBounds(Widget widget, WidgetBounds bounds, WidgetEditorContext editorContext) {
		WidgetPosition position = widget.getAnchor().positionFromBounds(
				editorContext.screenWidth(),
				editorContext.screenHeight(),
				bounds,
				widget.getWidth(),
				widget.getHeight()
		);
		widget.setPosition(position);
	}
}
