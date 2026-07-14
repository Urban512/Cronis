package dev.cronis.gui.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Draws rounded rectangles using the GUI draw context.
 * <p>
 * Shapes are composed from axis-aligned fills so no custom OpenGL code is required.
 */
public final class RoundedRenderer {
	private RoundedRenderer() {
	}

	/**
	 * Draws a filled rounded rectangle.
	 *
	 * @param context the draw context
	 * @param x       rectangle X position
	 * @param y       rectangle Y position
	 * @param width   rectangle width
	 * @param height  rectangle height
	 * @param radius  corner radius in pixels
	 * @param color   fill color in ARGB format
	 */
	public static void fill(GuiGraphicsExtractor context, int x, int y, int width, int height, int radius, int color) {
		if (width <= 0 || height <= 0) {
			return;
		}

		int clampedRadius = clampRadius(width, height, radius);
		if (clampedRadius <= 0) {
			context.fill(x, y, x + width, y + height, color);
			return;
		}

		context.fill(x + clampedRadius, y, x + width - clampedRadius, y + height, color);
		context.fill(x, y + clampedRadius, x + clampedRadius, y + height - clampedRadius, color);
		context.fill(x + width - clampedRadius, y + clampedRadius, x + width, y + height - clampedRadius, color);

		fillCorner(context, x, y, clampedRadius, color, Corner.TOP_LEFT);
		fillCorner(context, x + width - clampedRadius, y, clampedRadius, color, Corner.TOP_RIGHT);
		fillCorner(context, x, y + height - clampedRadius, clampedRadius, color, Corner.BOTTOM_LEFT);
		fillCorner(context, x + width - clampedRadius, y + height - clampedRadius, clampedRadius, color, Corner.BOTTOM_RIGHT);
	}

	/**
	 * Draws a rounded rectangle outline.
	 *
	 * @param context   the draw context
	 * @param x         rectangle X position
	 * @param y         rectangle Y position
	 * @param width     rectangle width
	 * @param height    rectangle height
	 * @param radius    corner radius in pixels
	 * @param thickness outline thickness in pixels
	 * @param color     outline color in ARGB format
	 */
	public static void outline(
			GuiGraphicsExtractor context,
			int x,
			int y,
			int width,
			int height,
			int radius,
			int thickness,
			int color
	) {
		if (width <= 0 || height <= 0 || thickness <= 0) {
			return;
		}

		int clampedRadius = clampRadius(width, height, radius);
		int clampedThickness = Math.min(thickness, Math.min(width, height) / 2);
		if (clampedRadius <= 0) {
			context.fill(x, y, x + width, y + thickness, color);
			context.fill(x, y + height - thickness, x + width, y + height, color);
			context.fill(x, y, x + thickness, y + height, color);
			context.fill(x + width - thickness, y, x + width, y + height, color);
			return;
		}

		context.fill(x + clampedRadius, y, x + width - clampedRadius, y + clampedThickness, color);
		context.fill(x + clampedRadius, y + height - clampedThickness, x + width - clampedRadius, y + height, color);
		context.fill(x, y + clampedRadius, x + clampedThickness, y + height - clampedRadius, color);
		context.fill(x + width - clampedThickness, y + clampedRadius, x + width, y + height - clampedRadius, color);

		outlineCorner(context, x, y, clampedRadius, clampedThickness, color, Corner.TOP_LEFT);
		outlineCorner(context, x + width - clampedRadius, y, clampedRadius, clampedThickness, color, Corner.TOP_RIGHT);
		outlineCorner(context, x, y + height - clampedRadius, clampedRadius, clampedThickness, color, Corner.BOTTOM_LEFT);
		outlineCorner(context, x + width - clampedRadius, y + height - clampedRadius, clampedRadius, clampedThickness, color, Corner.BOTTOM_RIGHT);
	}

	private static void fillCorner(
			GuiGraphicsExtractor context,
			int originX,
			int originY,
			int radius,
			int color,
			Corner corner
	) {
		for (int row = 0; row < radius; row++) {
			int span = horizontalCornerSpan(radius, row);
			if (span <= 0) {
				continue;
			}

			int y = corner.top() ? originY + row : originY + radius - row - 1;
			int x1 = corner.left() ? originX : originX + radius - span;
			int x2 = corner.left() ? originX + span : originX + radius;
			context.fill(x1, y, x2, y + 1, color);
		}
	}

	private static void outlineCorner(
			GuiGraphicsExtractor context,
			int originX,
			int originY,
			int radius,
			int thickness,
			int color,
			Corner corner
	) {
		float innerRadius = Math.max(0f, radius - thickness);
		float outerRadiusSquared = radius * radius;
		float innerRadiusSquared = innerRadius * innerRadius;

		for (int row = 0; row < radius; row++) {
			int y = corner.top() ? originY + row : originY + radius - row - 1;
			float cy = radius - row - 0.5f;

			for (int col = 0; col < radius; col++) {
				int x = corner.left() ? originX + col : originX + radius - col - 1;
				float cx = radius - col - 0.5f;
				float distanceSquared = cx * cx + cy * cy;

				if (distanceSquared <= outerRadiusSquared && distanceSquared > innerRadiusSquared) {
					context.fill(x, y, x + 1, y + 1, color);
				}
			}
		}
	}

	private static int horizontalCornerSpan(int radius, int row) {
		float cy = radius - row - 0.5f;
		float span = (float) Math.sqrt(Math.max(0f, radius * radius - cy * cy));
		return (int) Math.ceil(span);
	}

	private static int clampRadius(int width, int height, int radius) {
		return Math.max(0, Math.min(radius, Math.min(width, height) / 2));
	}

	private enum Corner {
		TOP_LEFT(true, true),
		TOP_RIGHT(false, true),
		BOTTOM_LEFT(true, false),
		BOTTOM_RIGHT(false, false);

		private final boolean left;
		private final boolean top;

		Corner(boolean left, boolean top) {
			this.left = left;
			this.top = top;
		}

		boolean left() {
			return left;
		}

		boolean top() {
			return top;
		}
	}
}
