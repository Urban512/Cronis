package dev.cronis.widget.persistence;

import com.google.gson.JsonParseException;
import dev.cronis.Cronis;
import dev.cronis.settings.Setting;
import dev.cronis.widget.Widget;
import dev.cronis.widget.persistence.WidgetSettingsCodec.WidgetSettingsDocument;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * File-backed persistence layer for widget setting values.
 */
public final class WidgetSettingsStorage {
	private final Path storagePath;
	private final Map<String, Map<String, String>> widgetSettings = new LinkedHashMap<>();

	private boolean loaded;
	private boolean dirty;

	public WidgetSettingsStorage(Path storagePath) {
		this.storagePath = Objects.requireNonNull(storagePath, "storagePath");
	}

	/**
	 * Loads widget settings from disk into memory.
	 *
	 * @return {@code true} when a readable settings document was loaded
	 */
	public boolean load() {
		loaded = true;
		widgetSettings.clear();
		dirty = false;

		if (!Files.exists(storagePath)) {
			Cronis.LOGGER.debug("Widget settings file does not exist yet: {}", storagePath);
			return false;
		}

		try {
			String json = Files.readString(storagePath, StandardCharsets.UTF_8);
			if (json.isBlank()) {
				Cronis.LOGGER.warn("Widget settings file is empty, using defaults: {}", storagePath);
				return false;
			}

			WidgetSettingsDocument document = WidgetSettingsCodec.decode(json);
			widgetSettings.putAll(document.widgets());
			Cronis.LOGGER.debug("Loaded widget settings for {} widget(s) from {}", widgetSettings.size(), storagePath);
			return true;
		} catch (JsonParseException exception) {
			Cronis.LOGGER.warn("Failed to parse widget settings file, using defaults: {}", storagePath, exception);
		} catch (IOException exception) {
			Cronis.LOGGER.warn("Failed to read widget settings file, using defaults: {}", storagePath, exception);
		}

		widgetSettings.clear();
		return false;
	}

	/**
	 * Applies persisted settings to a widget when values exist.
	 *
	 * @param widget widget to restore
	 */
	public void restoreSettings(Widget widget) {
		Objects.requireNonNull(widget, "widget");
		ensureLoaded();

		Map<String, String> serialized = widgetSettings.get(widget.getId());
		if (serialized == null || serialized.isEmpty()) {
			return;
		}

		WidgetSettingsCodec.applySettings(widget.getSettings().flattenSettings(), serialized);
	}

	/**
	 * Records a widget's current settings and marks the document dirty when they changed.
	 *
	 * @param widget widget whose settings changed
	 */
	public void recordSettingsChange(Widget widget) {
		Objects.requireNonNull(widget, "widget");
		ensureLoaded();

		Map<String, String> serialized = WidgetSettingsCodec.captureSettings(widget.getSettings().flattenSettings());
		Map<String, String> previous = widgetSettings.get(widget.getId());
		if (serialized.equals(previous)) {
			return;
		}

		widgetSettings.put(widget.getId(), serialized);
		dirty = true;
	}

	/**
	 * Persists in-memory settings when they have changed.
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
	 * Persists the current settings for all registered widgets.
	 *
	 * @param registeredWidgets currently registered widgets
	 * @return {@code true} when data was written to disk
	 */
	public boolean save(Collection<Widget> registeredWidgets) {
		Objects.requireNonNull(registeredWidgets, "registeredWidgets");
		ensureLoaded();

		Map<String, Map<String, String>> mergedSettings = new LinkedHashMap<>(widgetSettings);
		for (Widget widget : registeredWidgets) {
			mergedSettings.put(
					widget.getId(),
					WidgetSettingsCodec.captureSettings(widget.getSettings().flattenSettings())
			);
		}

		WidgetSettingsDocument document = new WidgetSettingsDocument(WidgetSettingsCodec.FORMAT_VERSION, mergedSettings);
		try {
			Files.createDirectories(storagePath.getParent());
			Files.writeString(storagePath, WidgetSettingsCodec.encode(document), StandardCharsets.UTF_8);
			widgetSettings.clear();
			widgetSettings.putAll(mergedSettings);
			dirty = false;
			Cronis.LOGGER.debug("Saved widget settings for {} widget(s) to {}", mergedSettings.size(), storagePath);
			return true;
		} catch (IOException exception) {
			Cronis.LOGGER.warn("Failed to save widget settings to {}", storagePath, exception);
			return false;
		}
	}

	private void ensureLoaded() {
		if (!loaded) {
			load();
		}
	}
}
