package dev.cronis.widget.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import dev.cronis.Cronis;
import dev.cronis.settings.InvalidSettingValueException;
import dev.cronis.settings.Setting;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * JSON serialization for widget setting values.
 */
final class WidgetSettingsCodec {
	static final int FORMAT_VERSION = 1;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private WidgetSettingsCodec() {
	}

	static WidgetSettingsDocument decode(String json) {
		Objects.requireNonNull(json, "json");

		JsonObject root;
		try {
			root = GSON.fromJson(json, JsonObject.class);
		} catch (JsonSyntaxException exception) {
			throw new JsonParseException("Root value is not a JSON object", exception);
		}

		if (root == null) {
			throw new JsonParseException("Root value is empty");
		}

		int version = readVersion(root);
		Map<String, Map<String, String>> widgets = readWidgets(root);
		return new WidgetSettingsDocument(version, widgets);
	}

	static String encode(WidgetSettingsDocument document) {
		Objects.requireNonNull(document, "document");

		JsonObject root = new JsonObject();
		root.addProperty("version", document.version());

		JsonObject widgetsObject = new JsonObject();
		for (Map.Entry<String, Map<String, String>> widgetEntry : document.widgets().entrySet()) {
			widgetsObject.add(widgetEntry.getKey(), encodeWidgetSettings(widgetEntry.getValue()));
		}
		root.add("widgets", widgetsObject);

		return GSON.toJson(root);
	}

	private static int readVersion(JsonObject root) {
		if (!root.has("version")) {
			return FORMAT_VERSION;
		}

		try {
			return root.get("version").getAsInt();
		} catch (RuntimeException exception) {
			throw new JsonParseException("Invalid version field", exception);
		}
	}

	private static Map<String, Map<String, String>> readWidgets(JsonObject root) {
		Map<String, Map<String, String>> widgets = new LinkedHashMap<>();

		if (!root.has("widgets") || !root.get("widgets").isJsonObject()) {
			return widgets;
		}

		JsonObject widgetsObject = root.getAsJsonObject("widgets");
		for (Map.Entry<String, JsonElement> widgetEntry : widgetsObject.entrySet()) {
			if (!widgetEntry.getValue().isJsonObject()) {
				Cronis.LOGGER.warn(
						"Skipping invalid widget settings entry '{}': expected object",
						widgetEntry.getKey()
				);
				continue;
			}

			widgets.put(widgetEntry.getKey(), decodeWidgetSettings(widgetEntry.getValue().getAsJsonObject()));
		}

		return widgets;
	}

	private static Map<String, String> decodeWidgetSettings(JsonObject widgetObject) {
		Map<String, String> settings = new LinkedHashMap<>();

		for (Map.Entry<String, JsonElement> settingEntry : widgetObject.entrySet()) {
			if (!settingEntry.getValue().isJsonPrimitive() || !settingEntry.getValue().getAsJsonPrimitive().isString()) {
				Cronis.LOGGER.warn("Skipping invalid setting '{}': expected string value", settingEntry.getKey());
				continue;
			}

			settings.put(settingEntry.getKey(), settingEntry.getValue().getAsString());
		}

		return settings;
	}

	private static JsonObject encodeWidgetSettings(Map<String, String> settings) {
		JsonObject widgetObject = new JsonObject();
		for (Map.Entry<String, String> settingEntry : settings.entrySet()) {
			widgetObject.addProperty(settingEntry.getKey(), settingEntry.getValue());
		}
		return widgetObject;
	}

	static Map<String, String> captureSettings(Map<String, Setting<?>> flattenedSettings) {
		Map<String, String> serialized = new LinkedHashMap<>();
		for (Map.Entry<String, Setting<?>> entry : flattenedSettings.entrySet()) {
			serialized.put(entry.getKey(), entry.getValue().serializeValue());
		}
		return serialized;
	}

	static void applySettings(Map<String, Setting<?>> flattenedSettings, Map<String, String> serializedSettings) {
		for (Map.Entry<String, String> entry : serializedSettings.entrySet()) {
			Setting<?> setting = flattenedSettings.get(entry.getKey());
			if (setting == null) {
				continue;
			}

			try {
				setting.loadValue(entry.getValue());
			} catch (InvalidSettingValueException exception) {
				Cronis.LOGGER.warn("Skipping invalid persisted value for setting '{}': {}", entry.getKey(), exception.getMessage());
			}
		}
	}

	record WidgetSettingsDocument(int version, Map<String, Map<String, String>> widgets) {
		WidgetSettingsDocument {
			Objects.requireNonNull(widgets, "widgets");
		}

		static WidgetSettingsDocument empty() {
			return new WidgetSettingsDocument(FORMAT_VERSION, new LinkedHashMap<>());
		}
	}
}
