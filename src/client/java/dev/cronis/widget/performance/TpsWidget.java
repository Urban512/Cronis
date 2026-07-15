package dev.cronis.widget.performance;

import dev.cronis.gui.theme.GuiMetrics;
import dev.cronis.metrics.ClientMetricsService;
import dev.cronis.settings.IntSetting;
import dev.cronis.settings.SettingGroup;
import dev.cronis.widget.MetricWidgetLayout;
import dev.cronis.widget.Widget;
import dev.cronis.widget.WidgetAnchor;
import dev.cronis.widget.WidgetCategory;
import dev.cronis.widget.WidgetContext;
import dev.cronis.widget.WidgetPosition;
import dev.cronis.widget.WidgetSize;
import dev.cronis.widget.WidgetSurfaceStyle;

/**
 * Displays a smoothed client-side TPS estimate.
 */
public final class TpsWidget extends Widget {
	private static final String WIDGET_ID = "tps";
	private static final String LABEL = "TPS";
	private static final String VALUE_PLACEHOLDER = "20.0";

	private final ClientMetricsService metrics = ClientMetricsService.get();

	private String valueText = VALUE_PLACEHOLDER;
	private float lastTps = Float.NaN;
	private int lastPrecision = -1;
	private WidgetSize cachedUnscaledSize;
	private float lastScale = Float.NaN;

	public TpsWidget() {
		super(WIDGET_ID, "TPS", WidgetCategory.PERFORMANCE, WidgetSurfaceStyle.TEXT_ONLY);
		setAnchor(WidgetAnchor.TOP_LEFT);
		setPosition(new WidgetPosition(GuiMetrics.SCREEN_MARGIN + 176, GuiMetrics.SCREEN_MARGIN));
	}

	@Override
	public void update(WidgetContext context) {
		int precision = decimalPrecision();
		if (precision != lastPrecision) {
			lastPrecision = precision;
			cachedUnscaledSize = null;
			lastTps = Float.NaN;
		}

		ensureSize(context.font());

		float tps = metrics.tps();
		if (Float.isNaN(lastTps) || Math.abs(tps - lastTps) >= precisionThreshold(precision)) {
			lastTps = tps;
			valueText = formatTps(tps, precision);
		}
	}

	@Override
	public void render(WidgetContext context) {
		ensureSize(context.font());

		var bounds = resolveBounds(context);
		MetricWidgetLayout.renderScaled(
				context.graphics(),
				context.font(),
				context.theme(),
				bounds.x(),
				bounds.y(),
				getScale(),
				LABEL,
				valueText,
				true
		);
	}

	@Override
	public WidgetSize getPreferredSize() {
		return cachedUnscaledSize != null ? cachedUnscaledSize : MetricWidgetLayout.fallback(true);
	}

	@Override
	protected SettingGroup buildSettingsGroup() {
		SettingGroup group = new SettingGroup(WIDGET_ID + ".settings", "TPS", "Tick rate display options.");
		group.add(new IntSetting(
				"decimalPrecision",
				"Decimal Precision",
				"Number of decimal places shown for the TPS value.",
				1,
				0,
				2
		));
		return group;
	}

	private int decimalPrecision() {
		return getSettings()
				.get("decimalPrecision", IntSetting.class)
				.map(IntSetting::getValue)
				.orElse(1);
	}

	private static float precisionThreshold(int precision) {
		return switch (precision) {
			case 0 -> 0.5f;
			case 2 -> 0.01f;
			default -> 0.05f;
		};
	}

	private static String formatTps(float tps, int precision) {
		return String.format("%." + precision + "f", tps);
	}

	private void ensureSize(net.minecraft.client.gui.Font font) {
		if (cachedUnscaledSize == null) {
			cachedUnscaledSize = MetricWidgetLayout.measure(font, LABEL, VALUE_PLACEHOLDER, true);
			lastScale = Float.NaN;
		}

		float scale = getScale();
		if (scale != lastScale) {
			lastScale = scale;
			WidgetSize scaled = scaleSize(cachedUnscaledSize);
			setSize(scaled.width(), scaled.height());
		}
	}
}
