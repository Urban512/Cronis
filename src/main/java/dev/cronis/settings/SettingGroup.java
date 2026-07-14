package dev.cronis.settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Groups related settings under a shared namespace.
 * <p>
 * Groups preserve insertion order for deterministic iteration and support
 * nested child groups for hierarchical configuration trees.
 */
public final class SettingGroup {
	private final String id;
	private final String displayName;
	private final String description;
	private final LinkedHashMap<String, Setting<?>> settings = new LinkedHashMap<>();
	private final List<SettingGroup> children = new ArrayList<>();

	/**
	 * Creates a setting group.
	 *
	 * @param id          unique identifier
	 * @param displayName user-facing label
	 * @param description explanatory text
	 */
	public SettingGroup(String id, String displayName, String description) {
		this.id = Objects.requireNonNull(id, "id");
		this.displayName = Objects.requireNonNull(displayName, "displayName");
		this.description = Objects.requireNonNullElse(description, "");
	}

	/**
	 * Returns the stable identifier for this group.
	 *
	 * @return group id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the user-facing label for this group.
	 *
	 * @return display name
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Returns the optional explanatory text for this group.
	 *
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Registers a setting in this group.
	 *
	 * @param setting setting to register
	 * @param <T>     value type
	 * @return the registered setting
	 * @throws IllegalArgumentException when a setting with the same id already exists
	 */
	public <T> Setting<T> add(Setting<T> setting) {
		Objects.requireNonNull(setting, "setting");
		Setting<?> existing = settings.putIfAbsent(setting.getId(), setting);
		if (existing != null) {
			throw new IllegalArgumentException("Setting already registered: " + setting.getId());
		}
		return setting;
	}

	/**
	 * Registers a nested child group.
	 *
	 * @param group child group
	 * @return the registered group
	 * @throws IllegalArgumentException when a group with the same id already exists
	 */
	public SettingGroup addGroup(SettingGroup group) {
		Objects.requireNonNull(group, "group");
		for (SettingGroup child : children) {
			if (child.getId().equals(group.getId())) {
				throw new IllegalArgumentException("Group already registered: " + group.getId());
			}
		}
		children.add(group);
		return group;
	}

	/**
	 * Returns a registered setting by id.
	 *
	 * @param settingId setting identifier
	 * @return optional setting
	 */
	public Optional<Setting<?>> get(String settingId) {
		return Optional.ofNullable(settings.get(settingId));
	}

	/**
	 * Returns a registered setting by id and expected type.
	 *
	 * @param settingId setting identifier
	 * @param type      expected setting class
	 * @param <T>       setting type
	 * @return optional typed setting
	 */
	@SuppressWarnings("unchecked")
	public <T extends Setting<?>> Optional<T> get(String settingId, Class<T> type) {
		Objects.requireNonNull(type, "type");
		Setting<?> setting = settings.get(settingId);
		if (setting == null || !type.isInstance(setting)) {
			return Optional.empty();
		}
		return Optional.of((T) setting);
	}

	/**
	 * Returns an immutable view of direct settings in registration order.
	 *
	 * @return registered settings
	 */
	public Collection<Setting<?>> settings() {
		return Collections.unmodifiableCollection(settings.values());
	}

	/**
	 * Returns an immutable view of nested child groups.
	 *
	 * @return child groups
	 */
	public List<SettingGroup> groups() {
		return Collections.unmodifiableList(children);
	}

	/**
	 * Returns whether this group contains no settings or child groups.
	 *
	 * @return {@code true} when empty
	 */
	public boolean isEmpty() {
		return settings.isEmpty() && children.isEmpty();
	}

	/**
	 * Resets every setting in this group and all nested groups to defaults.
	 */
	public void resetAll() {
		for (Setting<?> setting : settings.values()) {
			setting.reset();
		}
		for (SettingGroup child : children) {
			child.resetAll();
		}
	}

	/**
	 * Returns direct settings that pass their visibility predicate.
	 *
	 * @return visible settings in registration order
	 */
	public List<Setting<?>> visibleSettings() {
		if (settings.isEmpty()) {
			return List.of();
		}

		List<Setting<?>> visible = new ArrayList<>(settings.size());
		for (Setting<?> setting : settings.values()) {
			if (setting.isVisible()) {
				visible.add(setting);
			}
		}
		return Collections.unmodifiableList(visible);
	}

	/**
	 * Returns a flat map of all settings in this group tree keyed by qualified id.
	 * <p>
	 * Qualified ids use {@code groupId.settingId} for direct settings and
	 * {@code parent.child.settingId} for nested groups. This format is intended
	 * for future profile and file serialization.
	 *
	 * @return qualified setting map
	 */
	public Map<String, Setting<?>> flattenSettings() {
		Map<String, Setting<?>> flattened = new LinkedHashMap<>();
		flattenSettingsInto("", flattened);
		return Collections.unmodifiableMap(flattened);
	}

	private void flattenSettingsInto(String prefix, Map<String, Setting<?>> target) {
		String groupPrefix = prefix.isEmpty() ? id : prefix + "." + id;

		for (Setting<?> setting : settings.values()) {
			target.put(groupPrefix + "." + setting.getId(), setting);
		}

		for (SettingGroup child : children) {
			child.flattenSettingsInto(groupPrefix, target);
		}
	}
}
