package dev.cronis.widget;

import dev.cronis.Cronis;

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
	private static final WidgetManager INSTANCE = new WidgetManager();
	private static final Path STORAGE_PATH = Path.of("config", "cronis", "widgets.json");

	private final Map<String, Widget> widgets = new LinkedHashMap<>();
	private final WidgetRenderer renderer = new WidgetRenderer();

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
	 * Persists registered widget layout state.
	 * <p>
	 * Persistence is not implemented yet.
	 *
	 * @return {@code false} until persistence is implemented
	 */
	public boolean save() {
		Cronis.LOGGER.debug("Widget persistence is not implemented yet: {}", STORAGE_PATH);
		return false;
	}

	/**
	 * Restores registered widget layout state.
	 * <p>
	 * Persistence is not implemented yet.
	 *
	 * @return {@code false} until persistence is implemented
	 */
	public boolean load() {
		Cronis.LOGGER.debug("Widget persistence is not implemented yet: {}", STORAGE_PATH);
		return false;
	}

	/**
	 * Returns the future storage path for widget layout data.
	 *
	 * @return storage path
	 */
	public Path storagePath() {
		return STORAGE_PATH;
	}
}
