package dev.cronis.widget.performance;

import dev.cronis.gui.theme.GuiMetrics;
import dev.cronis.metrics.ClientMetricsService;
import dev.cronis.settings.EnumSetting;
import dev.cronis.settings.SettingGroup;
import dev.cronis.widget.MetricWidgetLayout;
import dev.cronis.widget.Widget;
import dev.cronis.widget.WidgetAnchor;
import dev.cronis.widget.WidgetCategory;
import dev.cronis.widget.WidgetContext;
import dev.cronis.widget.WidgetPosition;
import dev.cronis.widget.WidgetSize;
import dev.cronis.widget.WidgetSurfaceStyle;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Displays the user's local system time.
 */
public final class ClockWidget extends Widget {
	private static final String WIDGET_ID = "clock";
	private static final String LABEL = "Time";
	private static final String VALUE_PLACEHOLDER_24 = "23:59";
	private static final String VALUE_PLACEHOLDER_12 = "11:59 PM";

	private static final DateTimeFormatter FORMAT_24 = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter FORMAT_12 = DateTimeFormatter.ofPattern("h:mm a");

	private final ClientMetricsService metrics = ClientMetricsService.get();

	private String valueText = VALUE_PLACEHOLDER_24;
	private ClockFormat lastFormat = ClockFormat.HOUR_24;
	private WidgetSize cachedUnscaledSize;
	private float lastScale = Float.NaN;

	public ClockWidget() {
		super(WIDGET_ID, "Clock", WidgetCategory.PERFORMANCE, WidgetSurfaceStyle.TEXT_ONLY);
		setAnchor(WidgetAnchor.TOP_LEFT);
		setPosition(new WidgetPosition(GuiMetrics.SCREEN_MARGIN + 264, GuiMetrics.SCREEN_MARGIN));
	}

	@Override
	public void update(WidgetContext context) {
		ClockFormat format = clockFormat();
		if (format != lastFormat) {
			lastFormat = format;
			cachedUnscaledSize = null;
		}

		ensureSize(context.font(), format);

		LocalTime time = metrics.localTime();
		String formatted = formatTime(time, format);
		if (!formatted.equals(valueText)) {
			valueText = formatted;
		}
	}

	@Override
	public void render(WidgetContext context) {
		ClockFormat format = clockFormat();
		ensureSize(context.font(), format);

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
		SettingGroup group = new SettingGroup(WIDGET_ID + ".settings", "Clock", "Local time display options.");
		group.add(new EnumSetting<>("timeFormat", "Time Format", "Choose between 24-hour and 12-hour display.", ClockFormat.HOUR_24));
		return group;
	}

	@SuppressWarnings("unchecked")
	private ClockFormat clockFormat() {
		return getSettings()
				.get("timeFormat", EnumSetting.class)
				.map(setting -> (ClockFormat) setting.getValue())
				.orElse(ClockFormat.HOUR_24);
	}

	private static String formatTime(LocalTime time, ClockFormat format) {
		return switch (format) {
			case HOUR_12 -> FORMAT_12.format(time);
			case HOUR_24 -> FORMAT_24.format(time);
		};
	}

	private void ensureSize(net.minecraft.client.gui.Font font, ClockFormat format) {
		if (cachedUnscaledSize == null) {
			String placeholder = format == ClockFormat.HOUR_12 ? VALUE_PLACEHOLDER_12 : VALUE_PLACEHOLDER_24;
			cachedUnscaledSize = MetricWidgetLayout.measure(font, LABEL, placeholder, true);
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
