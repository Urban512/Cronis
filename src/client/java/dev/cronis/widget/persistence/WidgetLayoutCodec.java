package dev.cronis.widget.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import dev.cronis.Cronis;
import dev.cronis.widget.WidgetPosition;
import dev.cronis.widget.WidgetSize;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * JSON serialization for widget layout documents.
 */
final class WidgetLayoutCodec {
	static final int FORMAT_VERSION = 1;
	static final String DEFAULT_LAYOUT_NAME = "default";

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private WidgetLayoutCodec() {
	}

	static WidgetLayoutDocument decode(String json) {
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
		String activeLayout = readActiveLayout(root);
		Map<String, Map<String, WidgetLayoutSnapshot>> layouts = readLayouts(root, activeLayout);

		return new WidgetLayoutDocument(version, activeLayout, layouts);
	}

	static String encode(WidgetLayoutDocument document) {
		Objects.requireNonNull(document, "document");

		JsonObject root = new JsonObject();
		root.addProperty("version", document.version());
		root.addProperty("activeLayout", document.activeLayout());

		JsonObject layoutsObject = new JsonObject();
		for (Map.Entry<String, Map<String, WidgetLayoutSnapshot>> layoutEntry : document.layouts().entrySet()) {
			layoutsObject.add(layoutEntry.getKey(), encodeLayout(layoutEntry.getValue()));
		}
		root.add("layouts", layoutsObject);

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

	private static String readActiveLayout(JsonObject root) {
		if (!root.has("activeLayout")) {
			return DEFAULT_LAYOUT_NAME;
		}

		try {
			String activeLayout = root.get("activeLayout").getAsString();
			return activeLayout == null || activeLayout.isBlank() ? DEFAULT_LAYOUT_NAME : activeLayout;
		} catch (RuntimeException exception) {
			throw new JsonParseException("Invalid activeLayout field", exception);
		}
	}

	private static Map<String, Map<String, WidgetLayoutSnapshot>> readLayouts(
			JsonObject root,
			String activeLayout
	) {
		Map<String, Map<String, WidgetLayoutSnapshot>> layouts = new LinkedHashMap<>();

		if (root.has("layouts") && root.get("layouts").isJsonObject()) {
			JsonObject layoutsObject = root.getAsJsonObject("layouts");
			for (Map.Entry<String, JsonElement> layoutEntry : layoutsObject.entrySet()) {
				if (!layoutEntry.getValue().isJsonObject()) {
					Cronis.LOGGER.warn("Skipping invalid layout profile '{}': expected object", layoutEntry.getKey());
					continue;
				}

				Map<String, WidgetLayoutSnapshot> widgets = decodeLayout(
						layoutEntry.getKey(),
						layoutEntry.getValue().getAsJsonObject()
				);
				layouts.put(layoutEntry.getKey(), widgets);
			}
			return layouts;
		}

		if (root.has("widgets") && root.get("widgets").isJsonObject()) {
			layouts.put(activeLayout, decodeLayout(activeLayout, root.getAsJsonObject("widgets")));
		}

		return layouts;
	}

	private static Map<String, WidgetLayoutSnapshot> decodeLayout(String layoutName, JsonObject layoutObject) {
		Map<String, WidgetLayoutSnapshot> widgets = new LinkedHashMap<>();

		JsonObject widgetsObject = layoutObject;
		if (layoutObject.has("widgets") && layoutObject.get("widgets").isJsonObject()) {
			widgetsObject = layoutObject.getAsJsonObject("widgets");
		}

		for (Map.Entry<String, JsonElement> widgetEntry : widgetsObject.entrySet()) {
			if (!widgetEntry.getValue().isJsonObject()) {
				Cronis.LOGGER.warn(
						"Skipping invalid widget entry '{}' in layout '{}': expected object",
						widgetEntry.getKey(),
						layoutName
				);
				continue;
			}

			try {
				WidgetLayoutSnapshot snapshot = decodeWidget(widgetEntry.getKey(), widgetEntry.getValue().getAsJsonObject());
				widgets.put(snapshot.id(), snapshot);
			} catch (RuntimeException exception) {
				Cronis.LOGGER.warn(
						"Skipping invalid widget entry '{}' in layout '{}': {}",
						widgetEntry.getKey(),
						layoutName,
						exception.getMessage()
				);
			}
		}

		return widgets;
	}

	private static WidgetLayoutSnapshot decodeWidget(String widgetKey, JsonObject widgetObject) {
		String id = readString(widgetObject, "id", widgetKey);
		if (id.isBlank()) {
			throw new JsonParseException("Widget id is blank");
		}

		boolean visible = readBoolean(widgetObject, "visible", true);
		WidgetPosition position = readPosition(widgetObject);
		WidgetSize size = readSize(widgetObject);

		return new WidgetLayoutSnapshot(id, visible, position, size);
	}

	private static JsonObject encodeLayout(Map<String, WidgetLayoutSnapshot> widgets) {
		JsonObject layoutObject = new JsonObject();
		JsonObject widgetsObject = new JsonObject();

		for (WidgetLayoutSnapshot snapshot : widgets.values()) {
			widgetsObject.add(snapshot.id(), encodeWidget(snapshot));
		}

		layoutObject.add("widgets", widgetsObject);
		return layoutObject;
	}

	private static JsonObject encodeWidget(WidgetLayoutSnapshot snapshot) {
		JsonObject widgetObject = new JsonObject();
		widgetObject.addProperty("id", snapshot.id());
		widgetObject.addProperty("visible", snapshot.visible());

		JsonObject positionObject = new JsonObject();
		positionObject.addProperty("x", snapshot.position().offsetX());
		positionObject.addProperty("y", snapshot.position().offsetY());
		widgetObject.add("position", positionObject);

		JsonObject sizeObject = new JsonObject();
		sizeObject.addProperty("width", snapshot.size().width());
		sizeObject.addProperty("height", snapshot.size().height());
		widgetObject.add("size", sizeObject);

		return widgetObject;
	}

	private static WidgetPosition readPosition(JsonObject widgetObject) {
		if (!widgetObject.has("position") || !widgetObject.get("position").isJsonObject()) {
			return WidgetPosition.zero();
		}

		JsonObject positionObject = widgetObject.getAsJsonObject("position");
		float x = readFloat(positionObject, "x", 0f);
		float y = readFloat(positionObject, "y", 0f);
		return new WidgetPosition(x, y);
	}

	private static WidgetSize readSize(JsonObject widgetObject) {
		if (!widgetObject.has("size") || !widgetObject.get("size").isJsonObject()) {
			return WidgetSize.zero();
		}

		JsonObject sizeObject = widgetObject.getAsJsonObject("size");
		int width = readInt(sizeObject, "width", 0);
		int height = readInt(sizeObject, "height", 0);
		return new WidgetSize(Math.max(0, width), Math.max(0, height));
	}

	private static String readString(JsonObject object, String field, String defaultValue) {
		if (!object.has(field)) {
			return defaultValue;
		}

		try {
			return object.get(field).getAsString();
		} catch (RuntimeException exception) {
			throw new JsonParseException("Invalid " + field + " field", exception);
		}
	}

	private static boolean readBoolean(JsonObject object, String field, boolean defaultValue) {
		if (!object.has(field)) {
			return defaultValue;
		}

		try {
			return object.get(field).getAsBoolean();
		} catch (RuntimeException exception) {
			throw new JsonParseException("Invalid " + field + " field", exception);
		}
	}

	private static float readFloat(JsonObject object, String field, float defaultValue) {
		if (!object.has(field)) {
			return defaultValue;
		}

		try {
			return object.get(field).getAsFloat();
		} catch (RuntimeException exception) {
			throw new JsonParseException("Invalid " + field + " field", exception);
		}
	}

	private static int readInt(JsonObject object, String field, int defaultValue) {
		if (!object.has(field)) {
			return defaultValue;
		}

		try {
			return object.get(field).getAsInt();
		} catch (RuntimeException exception) {
			throw new JsonParseException("Invalid " + field + " field", exception);
		}
	}

	record WidgetLayoutDocument(
			int version,
			String activeLayout,
			Map<String, Map<String, WidgetLayoutSnapshot>> layouts
	) {
		WidgetLayoutDocument {
			Objects.requireNonNull(activeLayout, "activeLayout");
			Objects.requireNonNull(layouts, "layouts");
		}

		Map<String, WidgetLayoutSnapshot> activeWidgets() {
			return layouts.getOrDefault(activeLayout, Map.of());
		}

		static WidgetLayoutDocument empty(String activeLayout) {
			Map<String, Map<String, WidgetLayoutSnapshot>> layouts = new LinkedHashMap<>();
			layouts.put(activeLayout, new LinkedHashMap<>());
			return new WidgetLayoutDocument(FORMAT_VERSION, activeLayout, layouts);
		}
	}
}
