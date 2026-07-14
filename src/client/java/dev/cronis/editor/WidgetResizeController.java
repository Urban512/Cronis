package dev.cronis.editor;

import dev.cronis.widget.Widget;
import dev.cronis.widget.WidgetBounds;
import dev.cronis.widget.WidgetPosition;
import dev.cronis.widget.WidgetSize;

/**
 * Handles corner resize interactions for HUD widgets.
 */
public final class WidgetResizeController {
	public static final int HANDLE_SIZE = 8;
	public static final int HANDLE_HIT_PADDING = 4;

	/**
	 * Resize handle corners.
	 */
	public enum Handle {
		TOP_LEFT,
		TOP_RIGHT,
		BOTTOM_LEFT,
		BOTTOM_RIGHT
	}

	private boolean active;
	private Widget widget;
	private Handle handle;
	private int startMouseX;
	private int startMouseY;
	private WidgetBounds startBounds;
	private boolean aspectRatioLocked;
	private float lockedAspectRatio = 1f;

	public boolean isActive() {
		return active;
	}

	public Handle handle() {
		return handle;
	}

	public Widget widget() {
		return widget;
	}

	public void setAspectRatioLocked(boolean aspectRatioLocked) {
		this.aspectRatioLocked = aspectRatioLocked;
	}

	public void setLockedAspectRatio(float lockedAspectRatio) {
		this.lockedAspectRatio = Math.max(0.01f, lockedAspectRatio);
	}

	/**
	 * Returns the handle under the cursor for the provided bounds.
	 *
	 * @param bounds widget bounds
	 * @param mouseX cursor X coordinate in GUI pixels
	 * @param mouseY cursor Y coordinate in GUI pixels
	 * @return handle hit, or {@code null} when no handle is under the cursor
	 */
	public Handle hitTest(WidgetBounds bounds, int mouseX, int mouseY) {
		if (containsHandle(bounds.x(), bounds.y(), mouseX, mouseY)) {
			return Handle.TOP_LEFT;
		}
		if (containsHandle(bounds.right(), bounds.y(), mouseX, mouseY)) {
			return Handle.TOP_RIGHT;
		}
		if (containsHandle(bounds.x(), bounds.bottom(), mouseX, mouseY)) {
			return Handle.BOTTOM_LEFT;
		}
		if (containsHandle(bounds.right(), bounds.bottom(), mouseX, mouseY)) {
			return Handle.BOTTOM_RIGHT;
		}

		return null;
	}

	/**
	 * Begins resizing the provided widget from the active handle.
	 *
	 * @param widget      widget to resize
	 * @param handle      active resize handle
	 * @param mouseX      cursor X coordinate in GUI pixels
	 * @param mouseY      cursor Y coordinate in GUI pixels
	 * @param startBounds widget bounds at resize start
	 */
	public void begin(Widget widget, Handle handle, int mouseX, int mouseY, WidgetBounds startBounds) {
		this.active = true;
		this.widget = widget;
		this.handle = handle;
		this.startMouseX = mouseX;
		this.startMouseY = mouseY;
		this.startBounds = startBounds;
		this.lockedAspectRatio = startBounds.width() / (float) Math.max(1, startBounds.height());
	}

	/**
	 * Updates the resized widget using the current cursor position.
	 *
	 * @param editorContext editor frame context
	 * @param snapEngine    snap calculator
	 */
	public void update(WidgetEditorContext editorContext, WidgetSnapEngine snapEngine) {
		if (!active || widget == null || handle == null) {
			return;
		}

		WidgetBounds proposed = resizeToMouse(editorContext.mouseX(), editorContext.mouseY());
		WidgetBounds snapped = snapEngine.snapResize(
				proposed,
				widget,
				editorContext.widgetContext(),
				editorContext.grid()
		);
		applyBounds(widget, enforceMinimum(snapped), editorContext);
	}

	/**
	 * Ends the active resize operation.
	 */
	public void end() {
		active = false;
		widget = null;
		handle = null;
	}

	private WidgetBounds resizeToMouse(int mouseX, int mouseY) {
		WidgetSize minimum = widget.getMinimumSize();
		return switch (handle) {
			case TOP_LEFT -> fromFixedBottomRight(startBounds.right(), startBounds.bottom(), mouseX, mouseY, minimum);
			case TOP_RIGHT -> fromFixedBottomLeft(startBounds.x(), startBounds.bottom(), mouseX, mouseY, minimum);
			case BOTTOM_LEFT -> fromFixedTopRight(startBounds.right(), startBounds.y(), mouseX, mouseY, minimum);
			case BOTTOM_RIGHT -> fromFixedTopLeft(startBounds.x(), startBounds.y(), mouseX, mouseY, minimum);
		};
	}

	private WidgetBounds fromFixedTopLeft(int left, int top, int mouseX, int mouseY, WidgetSize minimum) {
		int width = Math.max(minimum.width(), mouseX - left);
		int height = Math.max(minimum.height(), mouseY - top);
		return applyAspectRatio(new WidgetBounds(left, top, width, height), left, top, true, true, minimum);
	}

	private WidgetBounds fromFixedBottomRight(int right, int bottom, int mouseX, int mouseY, WidgetSize minimum) {
		int width = Math.max(minimum.width(), right - mouseX);
		int height = Math.max(minimum.height(), bottom - mouseY);
		int x = right - width;
		int y = bottom - height;
		return applyAspectRatio(new WidgetBounds(x, y, width, height), right, bottom, false, false, minimum);
	}

	private WidgetBounds fromFixedBottomLeft(int left, int bottom, int mouseX, int mouseY, WidgetSize minimum) {
		int width = Math.max(minimum.width(), mouseX - left);
		int height = Math.max(minimum.height(), bottom - mouseY);
		int y = bottom - height;
		return applyAspectRatio(new WidgetBounds(left, y, width, height), left, bottom, true, false, minimum);
	}

	private WidgetBounds fromFixedTopRight(int right, int top, int mouseX, int mouseY, WidgetSize minimum) {
		int width = Math.max(minimum.width(), right - mouseX);
		int height = Math.max(minimum.height(), mouseY - top);
		int x = right - width;
		return applyAspectRatio(new WidgetBounds(x, top, width, height), right, top, false, true, minimum);
	}

	private WidgetBounds applyAspectRatio(
			WidgetBounds bounds,
			int anchorX,
			int anchorY,
			boolean anchorLeft,
			boolean anchorTop,
			WidgetSize minimum
	) {
		if (!aspectRatioLocked) {
			return bounds;
		}

		int width = bounds.width();
		int height = bounds.height();
		float currentRatio = width / (float) height;

		if (currentRatio > lockedAspectRatio) {
			width = Math.round(height * lockedAspectRatio);
		} else {
			height = Math.round(width / lockedAspectRatio);
		}

		width = Math.max(minimum.width(), width);
		height = Math.max(minimum.height(), height);

		int x = anchorLeft ? bounds.x() : anchorX - width;
		int y = anchorTop ? bounds.y() : anchorY - height;
		return new WidgetBounds(x, y, width, height);
	}

	private WidgetBounds enforceMinimum(WidgetBounds bounds) {
		WidgetSize minimum = widget.getMinimumSize();
		int width = Math.max(minimum.width(), bounds.width());
		int height = Math.max(minimum.height(), bounds.height());
		if (width == bounds.width() && height == bounds.height()) {
			return bounds;
		}

		return switch (handle) {
			case BOTTOM_RIGHT -> bounds.withSize(width, height);
			case TOP_LEFT -> new WidgetBounds(bounds.right() - width, bounds.bottom() - height, width, height);
			case TOP_RIGHT -> new WidgetBounds(bounds.x(), bounds.bottom() - height, width, height);
			case BOTTOM_LEFT -> new WidgetBounds(bounds.right() - width, bounds.y(), width, height);
		};
	}

	private static boolean containsHandle(int handleCenterX, int handleCenterY, int mouseX, int mouseY) {
		int half = HANDLE_SIZE / 2 + HANDLE_HIT_PADDING;
		return mouseX >= handleCenterX - half
				&& mouseX <= handleCenterX + half
				&& mouseY >= handleCenterY - half
				&& mouseY <= handleCenterY + half;
	}

	private static void applyBounds(Widget widget, WidgetBounds bounds, WidgetEditorContext editorContext) {
		widget.setSize(bounds.width(), bounds.height());
		WidgetPosition position = widget.getAnchor().positionFromBounds(
				editorContext.screenWidth(),
				editorContext.screenHeight(),
				bounds,
				bounds.width(),
				bounds.height()
		);
		widget.setPosition(position);
	}
}
