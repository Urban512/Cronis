package dev.cronis.gui.render;

import dev.cronis.gui.theme.DesignTokens;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Flat card surfaces combining fill and border (no shadows).
 */
public final class CardRenderer {
	private CardRenderer() {
	}

	public record Style(int radius, int borderThickness) {
		public static Style widget() {
			return new Style(DesignTokens.CORNER_RADIUS_WIDGET, DesignTokens.BORDER_THICKNESS);
		}

		public static Style card() {
			return new Style(DesignTokens.CORNER_RADIUS_CARD, DesignTokens.BORDER_THICKNESS);
		}

		public static Style flatCard() {
			return card();
		}

		public static Style flatWidget() {
			return widget();
		}

		public static Style panel() {
			return new Style(DesignTokens.CORNER_RADIUS_PANEL, DesignTokens.BORDER_THICKNESS);
		}

		public static Style control() {
			return new Style(DesignTokens.CORNER_RADIUS_CONTROL, DesignTokens.BORDER_THICKNESS);
		}
	}

	public static void draw(
			GuiGraphicsExtractor context,
			int x,
			int y,
			int width,
			int height,
			Style style,
			int fillColor,
			int borderColor,
			int shadowColor
	) {
		draw(context, x, y, width, height, style, fillColor, borderColor);
	}

	public static void draw(
			GuiGraphicsExtractor context,
			int x,
			int y,
			int width,
			int height,
			Style style,
			int fillColor,
			int borderColor
	) {
		if (width <= 0 || height <= 0) {
			return;
		}

		SurfaceRenderer.fill(context, x, y, width, height, style.radius(), fillColor);
		BorderRenderer.draw(context, x, y, width, height, style.radius(), style.borderThickness(), borderColor);
	}
}
