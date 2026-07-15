package dev.cronis.gui.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Shared rounded-rectangle geometry with smooth corner coverage.
 */
final class RoundedRectGeometry {
	private RoundedRectGeometry() {
	}

	static int clampRadius(int width, int height, int radius) {
		return Math.max(0, Math.min(radius, Math.min(width, height) / 2));
	}

	static void fill(GuiGraphicsExtractor context, int x, int y, int width, int height, int radius, int color) {
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

	static void ring(
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
			context.fill(x, y, x + width, y + clampedThickness, color);
			context.fill(x, y + height - clampedThickness, x + width, y + height, color);
			context.fill(x, y, x + clampedThickness, y + height, color);
			context.fill(x + width - clampedThickness, y, x + width, y + height, color);
			return;
		}

		context.fill(x + clampedRadius, y, x + width - clampedRadius, y + clampedThickness, color);
		context.fill(x + clampedRadius, y + height - clampedThickness, x + width - clampedRadius, y + height, color);
		context.fill(x, y + clampedRadius, x + clampedThickness, y + height - clampedRadius, color);
		context.fill(x + width - clampedThickness, y + clampedRadius, x + width, y + height - clampedRadius, color);

		ringCorner(context, x, y, clampedRadius, clampedThickness, color, Corner.TOP_LEFT);
		ringCorner(context, x + width - clampedRadius, y, clampedRadius, clampedThickness, color, Corner.TOP_RIGHT);
		ringCorner(context, x, y + height - clampedRadius, clampedRadius, clampedThickness, color, Corner.BOTTOM_LEFT);
		ringCorner(context, x + width - clampedRadius, y + height - clampedRadius, clampedRadius, clampedThickness, color, Corner.BOTTOM_RIGHT);
	}

	private static void fillCorner(
			GuiGraphicsExtractor context,
			int originX,
			int originY,
			int radius,
			int color,
			Corner corner
	) {
		float outerRadius = radius - 0.5f;
		float outerRadiusSquared = outerRadius * outerRadius;

		for (int row = 0; row < radius; row++) {
			for (int col = 0; col < radius; col++) {
				float cx = radius - col - 0.5f;
				float cy = radius - row - 0.5f;
				float distanceSquared = cx * cx + cy * cy;
				if (distanceSquared > outerRadiusSquared) {
					continue;
				}

				float distance = (float) Math.sqrt(distanceSquared);
				float coverage = Math.min(1f, Math.max(0f, outerRadius + 0.5f - distance));
				int pixelColor = coverage >= 0.999f ? color : ColorUtil.withAlpha(color, coverage);

				int pixelX = corner.left() ? originX + col : originX + radius - col - 1;
				int pixelY = corner.top() ? originY + row : originY + radius - row - 1;
				context.fill(pixelX, pixelY, pixelX + 1, pixelY + 1, pixelColor);
			}
		}
	}

	private static void ringCorner(
			GuiGraphicsExtractor context,
			int originX,
			int originY,
			int radius,
			int thickness,
			int color,
			Corner corner
	) {
		float outerRadius = radius - 0.5f;
		float innerRadius = Math.max(0f, outerRadius - thickness);
		float outerRadiusSquared = outerRadius * outerRadius;
		float innerRadiusSquared = innerRadius * innerRadius;

		for (int row = 0; row < radius; row++) {
			for (int col = 0; col < radius; col++) {
				float cx = radius - col - 0.5f;
				float cy = radius - row - 0.5f;
				float distanceSquared = cx * cx + cy * cy;
				if (distanceSquared > outerRadiusSquared || distanceSquared <= innerRadiusSquared) {
					continue;
				}

				float distance = (float) Math.sqrt(distanceSquared);
				float outerCoverage = Math.min(1f, Math.max(0f, outerRadius + 0.5f - distance));
				float innerCoverage = Math.min(1f, Math.max(0f, distance - innerRadius + 0.5f));
				float ringCoverage = Math.min(outerCoverage, innerCoverage);
				if (ringCoverage <= 0f) {
					continue;
				}

				int pixelColor = ringCoverage >= 0.999f ? color : ColorUtil.withAlpha(color, ringCoverage);
				int pixelX = corner.left() ? originX + col : originX + radius - col - 1;
				int pixelY = corner.top() ? originY + row : originY + radius - row - 1;
				context.fill(pixelX, pixelY, pixelX + 1, pixelY + 1, pixelColor);
			}
		}
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
