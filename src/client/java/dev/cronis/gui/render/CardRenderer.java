package dev.cronis.gui.render;

import dev.cronis.gui.theme.DesignTokens;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Flat card surfaces combining fill and border (no shadows).
 * <p>
 * Border colors from {@link dev.cronis.gui.theme.GuiTheme} may already carry soft
 * alpha. Fully opaque outlines are softened via {@link DesignTokens#BORDER_OPACITY}
 * (or {@link DesignTokens#BORDER_OPACITY_STRONG} for emphasis).
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

		public static Style checkbox() {
			return new Style(DesignTokens.CORNER_RADIUS_CHECKBOX, DesignTokens.BORDER_THICKNESS);
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
		draw(context, x, y, width, height, style, fillColor, borderColor, false);
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
		draw(context, x, y, width, height, style, fillColor, borderColor, false);
	}

	/**
	 * Draws a card surface.
	 *
	 * @param emphasized when true, uses stronger outline opacity (focus / hover accent)
	 */
	public static void draw(
			GuiGraphicsExtractor context,
			int x,
			int y,
			int width,
			int height,
			Style style,
			int fillColor,
			int borderColor,
			boolean emphasized
	) {
		if (width <= 0 || height <= 0) {
			return;
		}

		SurfaceRenderer.fill(context, x, y, width, height, style.radius(), fillColor);
		BorderRenderer.draw(
				context,
				x,
				y,
				width,
				height,
				style.radius(),
				style.borderThickness(),
				resolveBorder(borderColor, emphasized)
		);
	}

	private static int resolveBorder(int borderColor, boolean emphasized) {
		int alpha = ColorUtil.alpha(borderColor);
		if (alpha < 0xE0) {
			return borderColor;
		}
		float opacity = emphasized ? DesignTokens.BORDER_OPACITY_STRONG : DesignTokens.BORDER_OPACITY;
		return ColorUtil.multiplyAlpha(borderColor, opacity);
	}
}
