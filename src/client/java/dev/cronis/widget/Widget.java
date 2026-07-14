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
	private final String id;
	private final String displayName;
	private final WidgetCategory category;
	private WidgetAnchor anchor = WidgetAnchor.TOP_LEFT;
	private WidgetPosition position = WidgetPosition.zero();
	private int width;
	private int height;
	private boolean visible = true;
	private boolean enabled = true;
	private SettingGroup settingsGroup;

	protected Widget(String id, String displayName, WidgetCategory category) {
		this.id = Objects.requireNonNull(id, "id");
		this.displayName = Objects.requireNonNull(displayName, "displayName");
		this.category = Objects.requireNonNull(category, "category");

		WidgetSize preferredSize = getPreferredSize();
		this.width = Math.max(0, preferredSize.width());
		this.height = Math.max(0, preferredSize.height());
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

	public void applyPreferredSize() {
		WidgetSize preferredSize = getPreferredSize();
		setSize(preferredSize.width(), preferredSize.height());
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
	 * Returns the widget's preferred dimensions.
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
	 * Returns the smallest size that can display the widget without clipping content.
	 * <p>
	 * The default implementation matches {@link #getPreferredSize()}. Override only
	 * when the minimum layout differs from the preferred default.
	 *
	 * @return minimum size
	 */
	public WidgetSize getMinimumSize() {
		return getPreferredSize();
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
