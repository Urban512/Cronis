package dev.cronis.widget;

import dev.cronis.gui.theme.GuiMetrics;
import dev.cronis.gui.theme.GuiTheme;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

/**
 * Shared vertical metric layout for text-only HUD widgets.
 * <p>
 * Does not draw a card background. Callers apply widget scale via pose transform
 * and sized bounds; measurement is always at 1.0x.
 */
public final class MetricWidgetLayout {
	private static final float LABEL_SCALE = GuiMetrics.TEXT_SCALE_CAPTION;
	private static final float VALUE_SCALE = GuiMetrics.TEXT_SCALE_BODY;
	private static final int LABEL_TO_VALUE_GAP = GuiMetrics.WIDGET_LINE_GAP;

	private MetricWidgetLayout() {
	}

	public static void render(
			GuiGraphicsExtractor graphics,
			Font font,
			GuiTheme theme,
			int x,
			int y,
			String label,
			String value,
			boolean showLabel
	) {
		int contentX = x + GuiMetrics.PADDING_WIDGET.left();
		int contentY = y + GuiMetrics.PADDING_WIDGET.top();

		if (showLabel && !label.isEmpty()) {
			drawScaledText(graphics, font, label, contentX, contentY, theme.textSecondary(), LABEL_SCALE);
			contentY += scaledLineHeight(font, LABEL_SCALE) + LABEL_TO_VALUE_GAP;
		}

		drawScaledText(graphics, font, value, contentX, contentY, theme.textPrimary(), VALUE_SCALE);
	}

	/**
	 * Renders metric content with a geometric widget scale transform.
	 */
	public static void renderScaled(
			GuiGraphicsExtractor graphics,
			Font font,
			GuiTheme theme,
			int screenX,
			int screenY,
			float widgetScale,
			String label,
			String value,
			boolean showLabel
	) {
		if (widgetScale == 1.0f) {
			render(graphics, font, theme, screenX, screenY, label, value, showLabel);
			return;
		}

		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(screenX, screenY);
		pose.scale(widgetScale, widgetScale);
		render(graphics, font, theme, 0, 0, label, value, showLabel);
		pose.popMatrix();
	}

	public static WidgetSize measure(Font font, String label, String valuePlaceholder, boolean showLabel) {
		int labelWidth = showLabel ? scaledTextWidth(font, label, LABEL_SCALE) : 0;
		int valueWidth = scaledTextWidth(font, valuePlaceholder, VALUE_SCALE);
		int contentWidth = Math.max(labelWidth, valueWidth);

		int contentHeight = 0;
		if (showLabel) {
			contentHeight += scaledLineHeight(font, LABEL_SCALE) + LABEL_TO_VALUE_GAP;
		}
		contentHeight += scaledLineHeight(font, VALUE_SCALE);

		return new WidgetSize(
				GuiMetrics.PADDING_WIDGET.horizontal() + contentWidth,
				GuiMetrics.PADDING_WIDGET.vertical() + contentHeight
		);
	}

	/**
	 * Approximate size used before Minecraft's {@link Font} is available.
	 */
	public static WidgetSize fallback(boolean showLabel) {
		int contentWidth = 48;
		int contentHeight = scaledFallbackLineHeight(VALUE_SCALE);
		if (showLabel) {
			contentHeight += scaledFallbackLineHeight(LABEL_SCALE) + LABEL_TO_VALUE_GAP;
		}
		return new WidgetSize(
				GuiMetrics.PADDING_WIDGET.horizontal() + contentWidth,
				GuiMetrics.PADDING_WIDGET.vertical() + contentHeight
		);
	}

	private static int scaledFallbackLineHeight(float scale) {
		return Math.max(1, Math.round(9 * scale));
	}

	private static void drawScaledText(
			GuiGraphicsExtractor graphics,
			Font font,
			String text,
			int x,
			int y,
			int color,
			float scale
	) {
		if (text.isEmpty()) {
			return;
		}

		if (scale == 1.0f) {
			graphics.text(font, text, x, y, color, false);
			return;
		}

		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(x, y);
		pose.scale(scale, scale);
		graphics.text(font, text, 0, 0, color, false);
		pose.popMatrix();
	}

	private static int scaledLineHeight(Font font, float scale) {
		return Math.max(1, Math.round(font.lineHeight * scale));
	}

	private static int scaledTextWidth(Font font, String text, float scale) {
		if (text.isEmpty()) {
			return 0;
		}
		return Math.round(font.width(text) * scale);
	}
}
