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
 * Displays the current client frame rate.
 */
public final class FpsWidget extends Widget {
	private static final String WIDGET_ID = "fps";
	private static final String LABEL = "FPS";
	private static final String VALUE_PLACEHOLDER = "000";

	private final ClientMetricsService metrics = ClientMetricsService.get();

	private String valueText = VALUE_PLACEHOLDER;
	private int lastFps = -1;
	private WidgetSize cachedUnscaledSize;
	private boolean lastShowLabel = true;
	private boolean lastValueOnly;
	private float lastScale = Float.NaN;

	public FpsWidget() {
		super(WIDGET_ID, "FPS", WidgetCategory.PERFORMANCE, WidgetSurfaceStyle.TEXT_ONLY);
		setAnchor(WidgetAnchor.TOP_LEFT);
		setPosition(new WidgetPosition(GuiMetrics.SCREEN_MARGIN, GuiMetrics.SCREEN_MARGIN));
	}

	@Override
	public void update(WidgetContext context) {
		boolean showLabel = shouldShowLabel();
		if (showLabel != lastShowLabel || isValueOnly() != lastValueOnly) {
			lastShowLabel = showLabel;
			lastValueOnly = isValueOnly();
			cachedUnscaledSize = null;
		}

		ensureSize(context.font(), showLabel);

		int fps = metrics.fps();
		if (fps != lastFps) {
			lastFps = fps;
			valueText = Integer.toString(fps);
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
		SettingGroup group = new SettingGroup(WIDGET_ID + ".settings", "FPS", "Frame rate display options.");
		group.add(new BooleanSetting("showLabel", "Show Label", "Display the FPS caption above the value.", true));
		group.add(new BooleanSetting("valueOnly", "Value Only", "Show only the numeric frame rate.", false));
		return group;
	}

	private boolean shouldShowLabel() {
		if (isValueOnly()) {
			return false;
		}
		return getSettings()
				.get("showLabel", BooleanSetting.class)
				.map(BooleanSetting::getValue)
				.orElse(true);
	}

	private boolean isValueOnly() {
		return getSettings()
				.get("valueOnly", BooleanSetting.class)
				.map(BooleanSetting::getValue)
				.orElse(false);
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
