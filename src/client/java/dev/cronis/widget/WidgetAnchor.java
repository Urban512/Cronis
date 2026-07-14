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
}
