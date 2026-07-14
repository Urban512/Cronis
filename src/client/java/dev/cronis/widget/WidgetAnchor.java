package dev.cronis.widget;

import dev.cronis.gui.render.RenderUtil;

/**
 * Anchor points used to position widgets relative to the viewport.
 */
public enum WidgetAnchor {
	TOP_LEFT,
	TOP_CENTER,
	TOP_RIGHT,
	CENTER,
	BOTTOM_LEFT,
	BOTTOM_CENTER,
	BOTTOM_RIGHT;

	/**
	 * Resolves persistent widget placement into screen-space bounds.
	 *
	 * @param screenWidth  scaled viewport width
	 * @param screenHeight scaled viewport height
	 * @param position     persistent widget offsets
	 * @param width        widget width
	 * @param height       widget height
	 * @return resolved render bounds
	 */
	public WidgetBounds resolve(int screenWidth, int screenHeight, WidgetPosition position, int width, int height) {
		int offsetX = Math.round(position.offsetX());
		int offsetY = Math.round(position.offsetY());

		return switch (this) {
			case TOP_LEFT -> new WidgetBounds(offsetX, offsetY, width, height);
			case TOP_CENTER -> new WidgetBounds(
					RenderUtil.centerX(screenWidth, width) + offsetX,
					offsetY,
					width,
					height
			);
			case TOP_RIGHT -> new WidgetBounds(
					screenWidth - width - offsetX,
					offsetY,
					width,
					height
			);
			case CENTER -> new WidgetBounds(
					RenderUtil.centerX(screenWidth, width) + offsetX,
					RenderUtil.centerY(screenHeight, height) + offsetY,
					width,
					height
			);
			case BOTTOM_LEFT -> new WidgetBounds(
					offsetX,
					screenHeight - height - offsetY,
					width,
					height
			);
			case BOTTOM_CENTER -> new WidgetBounds(
					RenderUtil.centerX(screenWidth, width) + offsetX,
					screenHeight - height - offsetY,
					width,
					height
			);
			case BOTTOM_RIGHT -> new WidgetBounds(
					screenWidth - width - offsetX,
					screenHeight - height - offsetY,
					width,
					height
			);
		};
	}

	/**
	 * Converts resolved screen-space bounds back into persistent anchor offsets.
	 *
	 * @param screenWidth  scaled viewport width
	 * @param screenHeight scaled viewport height
	 * @param bounds       resolved widget bounds
	 * @param width        widget width
	 * @param height       widget height
	 * @return persistent position offsets for this anchor
	 */
	public WidgetPosition positionFromBounds(
			int screenWidth,
			int screenHeight,
			WidgetBounds bounds,
			int width,
			int height
	) {
		return switch (this) {
			case TOP_LEFT -> new WidgetPosition(bounds.x(), bounds.y());
			case TOP_CENTER -> new WidgetPosition(
					bounds.x() - RenderUtil.centerX(screenWidth, width),
					bounds.y()
			);
			case TOP_RIGHT -> new WidgetPosition(
					screenWidth - width - bounds.x(),
					bounds.y()
			);
			case CENTER -> new WidgetPosition(
					bounds.x() - RenderUtil.centerX(screenWidth, width),
					bounds.y() - RenderUtil.centerY(screenHeight, height)
			);
			case BOTTOM_LEFT -> new WidgetPosition(
					bounds.x(),
					screenHeight - height - bounds.y()
			);
			case BOTTOM_CENTER -> new WidgetPosition(
					bounds.x() - RenderUtil.centerX(screenWidth, width),
					screenHeight - height - bounds.y()
			);
			case BOTTOM_RIGHT -> new WidgetPosition(
					screenWidth - width - bounds.x(),
					screenHeight - height - bounds.y()
			);
		};
	}
}
