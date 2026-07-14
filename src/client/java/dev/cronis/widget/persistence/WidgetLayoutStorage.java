package dev.cronis.widget.persistence;

import com.google.gson.JsonParseException;
import dev.cronis.Cronis;
import dev.cronis.widget.Widget;
import dev.cronis.widget.persistence.WidgetLayoutCodec.WidgetLayoutDocument;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * File-backed persistence layer for widget layout state.
 */
public final class WidgetLayoutStorage {
	private final Path storagePath;
	private final Map<String, WidgetLayoutSnapshot> activeLayout = new LinkedHashMap<>();

	private String activeLayoutName = WidgetLayoutCodec.DEFAULT_LAYOUT_NAME;
	private boolean loaded;
	private boolean dirty;

	public WidgetLayoutStorage(Path storagePath) {
		this.storagePath = Objects.requireNonNull(storagePath, "storagePath");
	}

	/**
	 * Loads layout data from disk into memory.
	 *
	 * @return {@code true} when a readable layout document was loaded
	 */
	public boolean load() {
		loaded = true;
		activeLayout.clear();
		dirty = false;

		if (!Files.exists(storagePath)) {
			Cronis.LOGGER.debug("Widget layout file does not exist yet: {}", storagePath);
			return false;
		}

		try {
			String json = Files.readString(storagePath, StandardCharsets.UTF_8);
			if (json.isBlank()) {
				Cronis.LOGGER.warn("Widget layout file is empty, using defaults: {}", storagePath);
				return false;
			}

			WidgetLayoutDocument document = WidgetLayoutCodec.decode(json);
			activeLayoutName = document.activeLayout();
			activeLayout.putAll(document.activeWidgets());
			Cronis.LOGGER.debug(
					"Loaded {} widget layout(s) from {} (layout: {})",
					activeLayout.size(),
					storagePath,
					activeLayoutName
			);
			return true;
		} catch (JsonParseException exception) {
			Cronis.LOGGER.warn("Failed to parse widget layout file, using defaults: {}", storagePath, exception);
		} catch (IOException exception) {
			Cronis.LOGGER.warn("Failed to read widget layout file, using defaults: {}", storagePath, exception);
		}

		activeLayout.clear();
		return false;
	}

	/**
	 * Returns the saved layout for a widget, if one exists.
	 *
	 * @param widgetId widget identifier
	 * @return optional saved layout
	 */
	public Optional<WidgetLayoutSnapshot> findLayout(String widgetId) {
		ensureLoaded();
		return Optional.ofNullable(activeLayout.get(widgetId));
	}

	/**
	 * Applies a saved layout to a widget when one exists.
	 *
	 * @param widget widget to restore
	 * @return {@code true} when a saved layout was applied
	 */
	public boolean restoreLayout(Widget widget) {
		Objects.requireNonNull(widget, "widget");
		return findLayout(widget.getId()).map(snapshot -> {
			snapshot.applyTo(widget);
			return true;
		}).orElse(false);
	}

	/**
	 * Records a widget's current layout and marks the document dirty when it changed.
	 *
	 * @param widget widget whose layout changed
	 */
	public void recordLayoutChange(Widget widget) {
		Objects.requireNonNull(widget, "widget");
		ensureLoaded();

		WidgetLayoutSnapshot snapshot = WidgetLayoutSnapshot.from(widget);
		WidgetLayoutSnapshot previous = activeLayout.get(snapshot.id());
		if (snapshot.equals(previous)) {
			return;
		}

		activeLayout.put(snapshot.id(), snapshot);
		dirty = true;
	}

	/**
	 * Persists in-memory layout state when it has changed.
	 *
	 * @param registeredWidgets currently registered widgets
	 * @return {@code true} when data was written to disk
	 */
	public boolean saveIfDirty(Collection<Widget> registeredWidgets) {
		ensureLoaded();
		if (!dirty) {
			return false;
		}

		return save(registeredWidgets);
	}

	/**
	 * Persists the current layout state for all registered widgets.
	 *
	 * @param registeredWidgets currently registered widgets
	 * @return {@code true} when data was written to disk
	 */
	public boolean save(Collection<Widget> registeredWidgets) {
		Objects.requireNonNull(registeredWidgets, "registeredWidgets");
		ensureLoaded();

		Map<String, WidgetLayoutSnapshot> mergedLayout = new LinkedHashMap<>(activeLayout);
		for (Widget widget : registeredWidgets) {
			mergedLayout.put(widget.getId(), WidgetLayoutSnapshot.from(widget));
		}

		WidgetLayoutDocument document = buildDocument(mergedLayout);
		try {
			Files.createDirectories(storagePath.getParent());
			Files.writeString(storagePath, WidgetLayoutCodec.encode(document), StandardCharsets.UTF_8);
			activeLayout.clear();
			activeLayout.putAll(mergedLayout);
			dirty = false;
			Cronis.LOGGER.debug("Saved {} widget layout(s) to {}", mergedLayout.size(), storagePath);
			return true;
		} catch (IOException exception) {
			Cronis.LOGGER.warn("Failed to save widget layouts to {}", storagePath, exception);
			return false;
		}
	}

	private WidgetLayoutDocument buildDocument(Map<String, WidgetLayoutSnapshot> widgetsForActiveLayout) {
		Map<String, Map<String, WidgetLayoutSnapshot>> layouts = new LinkedHashMap<>();
		layouts.put(activeLayoutName, widgetsForActiveLayout);
		return new WidgetLayoutDocument(WidgetLayoutCodec.FORMAT_VERSION, activeLayoutName, layouts);
	}

	private void ensureLoaded() {
		if (!loaded) {
			load();
		}
	}
}
