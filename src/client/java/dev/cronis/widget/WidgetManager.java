package dev.cronis.widget;

import dev.cronis.Cronis;
import dev.cronis.settings.Setting;
import dev.cronis.widget.persistence.WidgetLayoutStorage;
import dev.cronis.widget.persistence.WidgetSettingsStorage;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Central registry and lifecycle coordinator for Cronis HUD widgets.
 */
public final class WidgetManager {
	private static final Path STORAGE_PATH = Path.of("config", "cronis", "widgets.json");
	private static final Path SETTINGS_STORAGE_PATH = Path.of("config", "cronis", "widget-settings.json");
	private static final WidgetManager INSTANCE = new WidgetManager();

	private final Map<String, Widget> widgets = new LinkedHashMap<>();
	private final WidgetRenderer renderer = new WidgetRenderer();
	private final WidgetLayoutStorage layoutStorage = new WidgetLayoutStorage(STORAGE_PATH);
	private final WidgetSettingsStorage settingsStorage = new WidgetSettingsStorage(SETTINGS_STORAGE_PATH);

	private WidgetManager() {
	}

	/**
	 * Returns the shared widget manager instance.
	 *
	 * @return widget manager
	 */
	public static WidgetManager get() {
		return INSTANCE;
	}

	/**
	 * Registers a widget with a unique identifier.
	 *
	 * @param widget widget to register
	 * @throws IllegalArgumentException when a widget with the same id already exists
	 */
	public void register(Widget widget) {
		Objects.requireNonNull(widget, "widget");
		Widget existing = widgets.putIfAbsent(widget.getId(), widget);
		if (existing != null) {
			throw new IllegalArgumentException("Widget already registered: " + widget.getId());
		}

		if (layoutStorage.restoreLayout(widget)) {
			Cronis.LOGGER.debug("Restored saved layout for widget: {}", widget.getId());
		}

		settingsStorage.restoreSettings(widget);
		wireSettingsPersistence(widget);

		widget.onRegistered();
		Cronis.LOGGER.debug("Registered widget: {}", widget.getId());
	}

	/**
	 * Unregisters a widget by id.
	 *
	 * @param id widget identifier
	 * @return {@code true} when a widget was removed
	 */
	public boolean unregister(String id) {
		Widget removed = widgets.remove(id);
		if (removed == null) {
			return false;
		}

		removed.onUnregistered();
		Cronis.LOGGER.debug("Unregistered widget: {}", id);
		return true;
	}

	/**
	 * Returns an immutable snapshot of all registered widgets.
	 *
	 * @return registered widgets
	 */
	public Collection<Widget> getWidgets() {
		return Collections.unmodifiableCollection(widgets.values());
	}

	/**
	 * Returns widgets filtered by category.
	 *
	 * @param category widget category
	 * @return matching widgets
	 */
	public List<Widget> getWidgets(WidgetCategory category) {
		List<Widget> matches = new ArrayList<>();
		for (Widget widget : widgets.values()) {
			if (widget.getCategory() == category) {
				matches.add(widget);
			}
		}
		return Collections.unmodifiableList(matches);
	}

	/**
	 * Returns a registered widget by id.
	 *
	 * @param id widget identifier
	 * @return optional widget
	 */
	public Optional<Widget> getWidget(String id) {
		return Optional.ofNullable(widgets.get(id));
	}

	/**
	 * Updates all visible, enabled widgets within the viewport.
	 *
	 * @param context viewport context
	 */
	public void update(WidgetContext context) {
		Objects.requireNonNull(context, "context");
		forEachInViewport(context, Widget::update);
	}

	/**
	 * Renders all visible, enabled widgets within the viewport in registration order.
	 *
	 * @param context viewport context
	 */
	public void render(WidgetContext context) {
		Objects.requireNonNull(context, "context");
		forEachInViewport(context, this::renderInViewport);
	}

	private void renderInViewport(Widget widget, WidgetContext context) {
		renderer.render(widget, context);
	}

	private void forEachInViewport(WidgetContext context, WidgetPass pass) {
		if (widgets.isEmpty()) {
			return;
		}

		int screenWidth = context.screenWidth();
		int screenHeight = context.screenHeight();

		for (Widget widget : widgets.values()) {
			if (!widget.isVisible() || !widget.isEnabled()) {
				continue;
			}

			WidgetBounds bounds = widget.resolveBounds(context);
			if (!bounds.intersectsViewport(screenWidth, screenHeight)) {
				continue;
			}

			pass.accept(widget, context.withWidgetBounds(bounds));
		}
	}

	@FunctionalInterface
	private interface WidgetPass {
		void accept(Widget widget, WidgetContext context);
	}

	/**
	 * Loads widget layout data from disk into memory.
	 * <p>
	 * Should be called during client startup before widgets are registered.
	 *
	 * @return {@code true} when a readable layout document was loaded
	 */
	public boolean load() {
		boolean layoutLoaded = layoutStorage.load();
		boolean settingsLoaded = settingsStorage.load();
		return layoutLoaded || settingsLoaded;
	}

	/**
	 * Persists registered widget layout state to disk.
	 *
	 * @return {@code true} when data was written to disk
	 */
	public boolean save() {
		return layoutStorage.save(getWidgets());
	}

	/**
	 * Records a layout change for the provided widget and persists when needed.
	 *
	 * @param widget widget whose layout changed
	 */
	public void notifyLayoutChanged(Widget widget) {
		Objects.requireNonNull(widget, "widget");
		layoutStorage.recordLayoutChange(widget);
		layoutStorage.saveIfDirty(getWidgets());
	}

	/**
	 * Records a settings change for the provided widget and persists when needed.
	 *
	 * @param widget widget whose settings changed
	 */
	public void notifySettingsChanged(Widget widget) {
		Objects.requireNonNull(widget, "widget");
		settingsStorage.recordSettingsChange(widget);
		settingsStorage.saveIfDirty(getWidgets());
	}

	/**
	 * Returns the future storage path for widget layout data.
	 *
	 * @return storage path
	 */
	public Path storagePath() {
		return STORAGE_PATH;
	}

	private void wireSettingsPersistence(Widget widget) {
		for (Setting<?> setting : widget.getSettings().flattenSettings().values()) {
			setting.addChangeListener((changedSetting, oldValue, newValue) -> notifySettingsChanged(widget));
		}
	}
}
