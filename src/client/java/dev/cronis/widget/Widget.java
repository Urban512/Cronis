package dev.cronis.widget;

import dev.cronis.Cronis;
import dev.cronis.settings.SettingGroup;

import java.util.Objects;

/**
 * Base type for every independent Cronis HUD widget.
 * <p>
 * Widgets only implement widget-specific update, render, and sizing logic.
 * Placement, visibility, and lifecycle orchestration are handled by the framework.
 */
public abstract class Widget {
	public static final float SCALE_MIN = 0.50f;
	public static final float SCALE_MAX = 2.00f;
	public static final float SCALE_STEP = 0.05f;
	public static final float SCALE_DEFAULT = 1.00f;

	private final String id;
	private final String displayName;
	private final WidgetCategory category;
	private final WidgetSurfaceStyle surfaceStyle;
	private WidgetAnchor anchor = WidgetAnchor.TOP_LEFT;
	private WidgetPosition position = WidgetPosition.zero();
	private int width;
	private int height;
	private float scale = SCALE_DEFAULT;
	private boolean visible = true;
	private boolean enabled = true;
	private SettingGroup settingsGroup;

	protected Widget(String id, String displayName, WidgetCategory category) {
		this(id, displayName, category, WidgetSurfaceStyle.CARD);
	}

	protected Widget(String id, String displayName, WidgetCategory category, WidgetSurfaceStyle surfaceStyle) {
		this.id = Objects.requireNonNull(id, "id");
		this.displayName = Objects.requireNonNull(displayName, "displayName");
		this.category = Objects.requireNonNull(category, "category");
		this.surfaceStyle = Objects.requireNonNull(surfaceStyle, "surfaceStyle");

		WidgetSize preferredSize = getPreferredSize();
		this.width = Math.max(0, Math.round(preferredSize.width() * scale));
		this.height = Math.max(0, Math.round(preferredSize.height() * scale));
	}

	public String getId() {
		return id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public WidgetCategory getCategory() {
		return category;
	}

	public WidgetSurfaceStyle getSurfaceStyle() {
		return surfaceStyle;
	}

	/**
	 * Returns whether the user may resize this widget by dragging corners.
	 * Text-only widgets use scale instead of manual width/height.
	 */
	public boolean isManuallyResizable() {
		return surfaceStyle == WidgetSurfaceStyle.CARD;
	}

	public WidgetAnchor getAnchor() {
		return anchor;
	}

	public void setAnchor(WidgetAnchor anchor) {
		this.anchor = Objects.requireNonNull(anchor, "anchor");
	}

	/**
	 * Changes the anchor while preserving the widget's current screen-space bounds.
	 *
	 * @param newAnchor    target anchor
	 * @param screenWidth  scaled viewport width
	 * @param screenHeight scaled viewport height
	 */
	public void setAnchorPreservingScreenPosition(WidgetAnchor newAnchor, int screenWidth, int screenHeight) {
		Objects.requireNonNull(newAnchor, "newAnchor");
		if (newAnchor == anchor) {
			return;
		}

		WidgetBounds screenBounds = anchor.resolve(screenWidth, screenHeight, position, width, height);
		setAnchor(newAnchor);
		setPosition(newAnchor.positionFromBounds(screenWidth, screenHeight, screenBounds, width, height));
	}

	/**
	 * Changes the anchor while preserving the widget's current screen-space bounds.
	 *
	 * @param newAnchor target anchor
	 * @param context   viewport context used for layout resolution
	 */
	public void setAnchorPreservingScreenPosition(WidgetAnchor newAnchor, WidgetContext context) {
		Objects.requireNonNull(context, "context");
		setAnchorPreservingScreenPosition(newAnchor, context.screenWidth(), context.screenHeight());
	}

	public WidgetPosition getPosition() {
		return position;
	}

	public void setPosition(WidgetPosition position) {
		this.position = Objects.requireNonNull(position, "position");
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void setSize(int width, int height) {
		this.width = Math.max(0, width);
		this.height = Math.max(0, height);
	}

	public float getScale() {
		return scale;
	}

	/**
	 * Sets the widget display scale, clamped and snapped to {@link #SCALE_STEP}.
	 *
	 * @param scale requested scale
	 */
	public void setScale(float scale) {
		this.scale = snapScale(scale);
	}

	/**
	 * Snaps a raw scale value into the allowed range and step.
	 *
	 * @param scale raw scale
	 * @return snapped scale
	 */
	public static float snapScale(float scale) {
		float clamped = Math.max(SCALE_MIN, Math.min(SCALE_MAX, scale));
		return Math.round(clamped / SCALE_STEP) * SCALE_STEP;
	}

	/**
	 * Applies preferred size multiplied by the current scale.
	 */
	public void applyPreferredSize() {
		WidgetSize preferredSize = getPreferredSize();
		setSize(
				Math.max(0, Math.round(preferredSize.width() * scale)),
				Math.max(0, Math.round(preferredSize.height() * scale))
		);
	}

	/**
	 * Scales an unscaled content size by the current widget scale.
	 *
	 * @param unscaled measured size at 1.0x
	 * @return scaled size
	 */
	protected final WidgetSize scaleSize(WidgetSize unscaled) {
		Objects.requireNonNull(unscaled, "unscaled");
		return new WidgetSize(
				Math.max(0, Math.round(unscaled.width() * scale)),
				Math.max(0, Math.round(unscaled.height() * scale))
		);
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Resolves the widget's screen-space bounds for the provided viewport context.
	 *
	 * @param context viewport context
	 * @return resolved bounds
	 */
	public WidgetBounds resolveBounds(WidgetContext context) {
		return anchor.resolve(context.screenWidth(), context.screenHeight(), position, width, height);
	}

	/**
	 * Updates widget state for the current frame.
	 *
	 * @param context widget-scoped context
	 */
	public abstract void update(WidgetContext context);

	/**
	 * Renders the widget for the current frame.
	 *
	 * @param context widget-scoped context
	 */
	public abstract void render(WidgetContext context);

	/**
	 * Returns the widget's preferred dimensions at 1.0x scale.
	 *
	 * @return preferred size
	 */
	public abstract WidgetSize getPreferredSize();

	/**
	 * Returns the settings exposed by this widget for the HUD editor inspector.
	 * <p>
	 * The default implementation returns an empty group. Override
	 * {@link #buildSettingsGroup()} to register widget-specific settings.
	 *
	 * @return widget settings group
	 */
	public SettingGroup getSettings() {
		if (settingsGroup == null) {
			settingsGroup = buildSettingsGroup();
		}
		return settingsGroup;
	}

	/**
	 * Creates the settings group for this widget.
	 * <p>
	 * Subclasses may override to register additional settings. The returned group
	 * must not duplicate layout state already managed by the widget API.
	 *
	 * @return settings group
	 */
	protected SettingGroup buildSettingsGroup() {
		return new SettingGroup(getId() + ".settings", "Settings", "");
	}

	/**
	 * Returns the smallest unscaled size that can display the widget without clipping.
	 * <p>
	 * The default implementation matches {@link #getPreferredSize()}. Override only
	 * when the minimum layout differs from the preferred default.
	 *
	 * @return minimum size at 1.0x
	 */
	public WidgetSize getMinimumSize() {
		return getPreferredSize();
	}

	/**
	 * Returns the minimum screen size including the current scale.
	 */
	public WidgetSize getScaledMinimumSize() {
		return scaleSize(getMinimumSize());
	}

	/**
	 * Called when the widget is registered with the {@link WidgetManager}.
	 */
	protected void onRegistered() {
	}

	/**
	 * Called when the widget is removed from the {@link WidgetManager}.
	 */
	protected void onUnregistered() {
	}

	@Override
	public final String toString() {
		return "Widget{" + id + "}";
	}

	/**
	 * Logs a widget warning through the Cronis logger.
	 *
	 * @param message warning message
	 */
	protected final void warn(String message) {
		Cronis.LOGGER.warn("[{}] {}", id, message);
	}
}
