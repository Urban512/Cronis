package dev.cronis.widget;



import dev.cronis.gui.layout.Padding;

import dev.cronis.gui.layout.Spacing;

import dev.cronis.gui.render.RenderUtil;

import dev.cronis.gui.render.RoundedRenderer;

import dev.cronis.gui.theme.GuiTheme;

import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.Font;

import net.minecraft.client.gui.GuiGraphicsExtractor;



import java.time.LocalTime;

import java.time.format.DateTimeFormatter;



/**

 * Framework validation widget that exercises the standard widget lifecycle,

 * theming, and rendering pipeline without introducing domain-specific behavior.

 */

public final class DemoWidget extends Widget {

	private static final String DISPLAY_NAME = "Cronis Demo";

	private static final String FPS_LABEL = "FPS";

	private static final String TIME_LABEL = "Time";

	private static final String SCALE_LABEL = "GUI Scale";

	private static final String FPS_PLACEHOLDER = "000";

	private static final String TIME_PLACEHOLDER = "00:00:00";

	private static final String SCALE_PLACEHOLDER = "Auto (0)";



	private static final int CORNER_RADIUS = 10;

	private static final int LINE_GAP = Spacing.XS;

	private static final int LABEL_VALUE_GAP = Spacing.MD;

	private static final int FALLBACK_LINE_HEIGHT = 9;

	private static final Padding PADDING = Padding.symmetric(Spacing.MD, Spacing.LG);

	private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");



	private String fpsValue = FPS_PLACEHOLDER;

	private String timeValue = TIME_PLACEHOLDER;

	private String scaleValue = SCALE_PLACEHOLDER;

	private int lastFps = -1;

	private WidgetSize cachedPreferredSize;

	private WidgetSize cachedMinimumSize;



	public DemoWidget() {

		super("demo", DISPLAY_NAME, WidgetCategory.DEVELOPER);

		setAnchor(WidgetAnchor.TOP_LEFT);

		setPosition(new WidgetPosition(Spacing.LG, Spacing.LG));

	}



	@Override

	public void update(WidgetContext context) {

		ensureContentSizes(context.font());



		int fps = Minecraft.getInstance().getFps();

		if (fps != lastFps) {

			lastFps = fps;

			fpsValue = Integer.toString(fps);

		}



		String currentTime = TIME_FORMAT.format(LocalTime.now());

		if (!currentTime.equals(timeValue)) {

			timeValue = currentTime;

		}



		String currentScale = formatGuiScale(context);

		if (!currentScale.equals(scaleValue)) {

			scaleValue = currentScale;

		}

	}



	@Override

	public void render(WidgetContext context) {

		ensureContentSizes(context.font());



		WidgetBounds bounds = resolveBounds(context);

		GuiGraphicsExtractor graphics = context.graphics();

		Font font = context.font();

		GuiTheme theme = context.theme();



		RoundedRenderer.fill(

				graphics,

				bounds.x(),

				bounds.y(),

				bounds.width(),

				bounds.height(),

				CORNER_RADIUS,

				theme.cardBackground()

		);

		RoundedRenderer.outline(

				graphics,

				bounds.x(),

				bounds.y(),

				bounds.width(),

				bounds.height(),

				CORNER_RADIUS,

				1,

				theme.cardBorder()

		);



		int contentX = bounds.x() + PADDING.left();

		int contentWidth = bounds.width() - PADDING.horizontal();

		int textY = bounds.y() + PADDING.top();



		textY = drawMetricRow(

				graphics,

				font,

				contentX,

				textY,

				contentWidth,

				FPS_LABEL,

				fpsValue,

				theme.textSecondary(),

				theme.textPrimary()

		);

		textY = drawMetricRow(

				graphics,

				font,

				contentX,

				textY,

				contentWidth,

				TIME_LABEL,

				timeValue,

				theme.textSecondary(),

				theme.textPrimary()

		);

		drawMetricRow(

				graphics,

				font,

				contentX,

				textY,

				contentWidth,

				SCALE_LABEL,

				scaleValue,

				theme.textSecondary(),

				theme.textMuted()

		);

	}



	@Override

	public WidgetSize getPreferredSize() {

		if (cachedPreferredSize != null) {

			return cachedPreferredSize;

		}



		return fallbackContentSize();

	}



	@Override

	public WidgetSize getMinimumSize() {

		if (cachedMinimumSize != null) {

			return cachedMinimumSize;

		}



		return fallbackContentSize();

	}



	private void ensureContentSizes(Font font) {

		if (cachedMinimumSize != null) {

			return;

		}



		cachedMinimumSize = measureContentSize(font);

		cachedPreferredSize = cachedMinimumSize;

		setSize(cachedMinimumSize.width(), cachedMinimumSize.height());

	}



	private static WidgetSize measureContentSize(Font font) {

		int fpsRowWidth = font.width(FPS_LABEL) + LABEL_VALUE_GAP + font.width(FPS_PLACEHOLDER);

		int timeRowWidth = font.width(TIME_LABEL) + LABEL_VALUE_GAP + font.width(TIME_PLACEHOLDER);

		int scaleRowWidth = font.width(SCALE_LABEL) + LABEL_VALUE_GAP + font.width(SCALE_PLACEHOLDER);

		int contentWidth = Math.max(fpsRowWidth, Math.max(timeRowWidth, scaleRowWidth));

		int width = PADDING.horizontal() + contentWidth;

		int height = PADDING.vertical() + (font.lineHeight + LINE_GAP) * 2 + font.lineHeight;

		return new WidgetSize(width, height);

	}



	private static WidgetSize fallbackContentSize() {

		int contentWidth = 56 + LABEL_VALUE_GAP + 56;

		int height = PADDING.vertical() + (FALLBACK_LINE_HEIGHT + LINE_GAP) * 2 + FALLBACK_LINE_HEIGHT;

		return new WidgetSize(PADDING.horizontal() + contentWidth, height);

	}



	private static int drawMetricRow(

			GuiGraphicsExtractor graphics,

			Font font,

			int x,

			int y,

			int rowWidth,

			String label,

			String value,

			int labelColor,

			int valueColor

	) {

		graphics.text(font, label, x, y, labelColor, false);

		RenderUtil.drawAlignedText(

				graphics,

				font,

				value,

				x,

				y,

				rowWidth,

				valueColor,

				RenderUtil.TextAlignment.RIGHT,

				false

		);

		return y + font.lineHeight + LINE_GAP;

	}



	private static String formatGuiScale(WidgetContext context) {

		int configuredScale = Minecraft.getInstance().options.guiScale().get();

		String effectiveScale = formatScaleValue(context.guiScale());



		if (configuredScale == 0) {

			return "Auto (" + effectiveScale + ")";

		}



		return formatScaleValue(configuredScale);

	}



	private static String formatNumber(double scale) {

		if (Math.rint(scale) == scale) {

			return Integer.toString((int) scale);

		}



		return String.format("%.1f", scale);

	}



	private static String formatScaleValue(double scale) {

		return formatNumber(scale);

	}

}


