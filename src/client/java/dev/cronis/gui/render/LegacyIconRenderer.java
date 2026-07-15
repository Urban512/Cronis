package dev.cronis.gui.render;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Vector fallback icons used when PNG assets are unavailable.
 */
final class LegacyIconRenderer {
	private LegacyIconRenderer() {
	}

	enum Icon {
		SETTINGS,
		DISCORD,
		GITHUB,
		SEARCH
	}

	static void draw(GuiGraphicsExtractor context, Icon icon, int x, int y, int size, int color) {
		if (size <= 0) {
			return;
		}

		switch (icon) {
			case SETTINGS -> drawSettings(context, x, y, size, color);
			case DISCORD -> drawDiscord(context, x, y, size, color);
			case GITHUB -> drawGithub(context, x, y, size, color);
			case SEARCH -> drawSearch(context, x, y, size, color);
		}
	}

	private static void drawSettings(GuiGraphicsExtractor context, int x, int y, int size, int color) {
		int centerX = x + size / 2;
		int centerY = y + size / 2;
		int outer = Math.max(2, size / 2 - 1);
		int inner = Math.max(1, size / 5);
		int tooth = Math.max(1, size / 7);

		for (int angle = 0; angle < 8; angle++) {
			int offsetX = gearOffset(outer, angle, true);
			int offsetY = gearOffset(outer, angle, false);
			context.fill(centerX + offsetX - tooth / 2, centerY + offsetY - tooth / 2, centerX + offsetX + tooth / 2 + 1, centerY + offsetY + tooth / 2 + 1, color);
		}

		context.fill(centerX - inner, centerY - inner, centerX + inner + 1, centerY + inner + 1, color);
	}

	private static void drawDiscord(GuiGraphicsExtractor context, int x, int y, int size, int color) {
		int bodyHeight = Math.max(4, size * 2 / 3);
		int bodyY = y + (size - bodyHeight) / 2;
		int radius = bodyHeight / 2;
		context.fill(x + radius, bodyY, x + size - radius, bodyY + bodyHeight, color);
		context.fill(x + 1, bodyY + radius / 2, x + radius, bodyY + bodyHeight - 1, color);
		context.fill(x + size - radius, bodyY + radius / 2, x + size - 1, bodyY + bodyHeight - 1, color);

		int tail = Math.max(2, size / 5);
		context.fill(x + radius, bodyY + bodyHeight - 1, x + radius + tail, bodyY + bodyHeight + tail - 1, color);
	}

	private static void drawGithub(GuiGraphicsExtractor context, int x, int y, int size, int color) {
		int head = Math.max(4, size * 2 / 5);
		int headX = x + (size - head) / 2;
		int headY = y + 1;
		context.fill(headX, headY, headX + head, headY + head, color);

		int bodyWidth = Math.max(6, size - 2);
		int bodyHeight = Math.max(4, size / 2);
		int bodyX = x + (size - bodyWidth) / 2;
		int bodyY = headY + head - 1;
		context.fill(bodyX, bodyY, bodyX + bodyWidth, bodyY + bodyHeight, color);

		int arm = Math.max(2, size / 6);
		context.fill(bodyX - arm, bodyY + 1, bodyX, bodyY + bodyHeight - 1, color);
		context.fill(bodyX + bodyWidth, bodyY + 1, bodyX + bodyWidth + arm, bodyY + bodyHeight - 1, color);
	}

	private static void drawSearch(GuiGraphicsExtractor context, int x, int y, int size, int color) {
		int lens = Math.max(4, size * 3 / 5);
		int lensX = x + 1;
		int lensY = y + 1;
		int thickness = Math.max(1, size / 7);
		drawRing(context, lensX + lens / 2, lensY + lens / 2, lens / 2, thickness, color);

		int handleLength = Math.max(2, size / 3);
		int handleX = lensX + lens - thickness;
		int handleY = lensY + lens - thickness;
		for (int index = 0; index < handleLength; index++) {
			context.fill(handleX + index, handleY + index, handleX + index + thickness, handleY + index + thickness, color);
		}
	}

	private static void drawRing(GuiGraphicsExtractor context, int centerX, int centerY, int radius, int thickness, int color) {
		float outerSquared = radius * radius;
		float innerSquared = Math.max(0f, radius - thickness) * Math.max(0f, radius - thickness);

		for (int row = -radius; row <= radius; row++) {
			for (int col = -radius; col <= radius; col++) {
				float distanceSquared = col * col + row * row;
				if (distanceSquared <= outerSquared && distanceSquared >= innerSquared) {
					context.fill(centerX + col, centerY + row, centerX + col + 1, centerY + row + 1, color);
				}
			}
		}
	}

	private static int gearOffset(int radius, int segment, boolean horizontal) {
		return switch (segment % 8) {
			case 0 -> horizontal ? 0 : -radius;
			case 1 -> horizontal ? radius * 3 / 4 : -radius * 3 / 4;
			case 2 -> horizontal ? radius : 0;
			case 3 -> horizontal ? radius * 3 / 4 : radius * 3 / 4;
			case 4 -> horizontal ? 0 : radius;
			case 5 -> horizontal ? -radius * 3 / 4 : radius * 3 / 4;
			case 6 -> horizontal ? -radius : 0;
			default -> horizontal ? -radius * 3 / 4 : -radius * 3 / 4;
		};
	}
}
