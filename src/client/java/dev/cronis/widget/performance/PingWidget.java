package dev.cronis.widget.performance;

import dev.cronis.gui.theme.GuiMetrics;
import dev.cronis.metrics.ClientMetricsService;
import dev.cronis.settings.BooleanSetting;
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
 * Displays the current server latency.
 */
public final class PingWidget extends Widget {
	private static final String WIDGET_ID = "ping";
	private static final String LABEL = "Ping";
	private static final String VALUE_PLACEHOLDER = "999 ms";
	private static final String UNAVAILABLE_VALUE = "--";

	private final ClientMetricsService metrics = ClientMetricsService.get();

	private String valueText = UNAVAILABLE_VALUE;
	private int lastPing = Integer.MIN_VALUE;
	private WidgetSize cachedUnscaledSize;
	private boolean lastShowLabel = true;
	private float lastScale = Float.NaN;

	public PingWidget() {
		super(WIDGET_ID, "Ping", WidgetCategory.PERFORMANCE, WidgetSurfaceStyle.TEXT_ONLY);
		setAnchor(WidgetAnchor.TOP_LEFT);
		setPosition(new WidgetPosition(GuiMetrics.SCREEN_MARGIN + 88, GuiMetrics.SCREEN_MARGIN));
	}

	@Override
	public void update(WidgetContext context) {
		boolean showLabel = shouldShowLabel();
		if (showLabel != lastShowLabel) {
			lastShowLabel = showLabel;
			cachedUnscaledSize = null;
		}

		ensureSize(context.font(), showLabel);

		int ping = metrics.ping();
		if (ping != lastPing) {
			lastPing = ping;
			valueText = formatPing(ping);
		}
	}

	@Override
	public void render(WidgetContext context) {
		boolean showLabel = shouldShowLabel();
		ensureSize(context.font(), showLabel);

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
				showLabel
		);
	}

	@Override
	public WidgetSize getPreferredSize() {
		return cachedUnscaledSize != null ? cachedUnscaledSize : MetricWidgetLayout.fallback(true);
	}

	@Override
	protected SettingGroup buildSettingsGroup() {
		SettingGroup group = new SettingGroup(WIDGET_ID + ".settings", "Ping", "Latency display options.");
		group.add(new BooleanSetting("showLabel", "Show Label", "Display the Ping caption above the value.", true));
		return group;
	}

	private boolean shouldShowLabel() {
		return getSettings()
				.get("showLabel", BooleanSetting.class)
				.map(BooleanSetting::getValue)
				.orElse(true);
	}

	private static String formatPing(int ping) {
		if (ping < 0) {
			return UNAVAILABLE_VALUE;
		}
		return ping + " ms";
	}

	private void ensureSize(net.minecraft.client.gui.Font font, boolean showLabel) {
		if (cachedUnscaledSize == null) {
			cachedUnscaledSize = MetricWidgetLayout.measure(font, LABEL, VALUE_PLACEHOLDER, showLabel);
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
