package dev.cronis.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Abstract base for every Cronis setting.
 * <p>
 * Settings are plain data containers with validation and change notification.
 * They intentionally avoid any GUI or Minecraft dependencies so widgets, modules,
 * themes and profiles can share the same foundation.
 *
 * @param <T> value type
 */
public abstract class Setting<T> {
	private static final Predicate<Setting<?>> ALWAYS = setting -> true;

	private final String id;
	private final String displayName;
	private final String description;
	private final T defaultValue;
	private final List<SettingChangeListener<T>> listeners = new ArrayList<>(0);

	private T value;
	private Predicate<Setting<?>> visibilityPredicate = ALWAYS;
	private Predicate<Setting<?>> enabledPredicate = ALWAYS;

	protected Setting(String id, String displayName, String description, T defaultValue) {
		this.id = Objects.requireNonNull(id, "id");
		this.displayName = Objects.requireNonNull(displayName, "displayName");
		this.description = Objects.requireNonNullElse(description, "");
		this.defaultValue = Objects.requireNonNull(defaultValue, "defaultValue");
		this.value = defaultValue;
	}

	/**
	 * Returns the stable identifier for this setting.
	 *
	 * @return setting id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the user-facing label for this setting.
	 *
	 * @return display name
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Returns the optional explanatory text for this setting.
	 *
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns the default value defined at construction time.
	 *
	 * @return default value
	 */
	public T getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Returns the current value.
	 *
	 * @return current value
	 */
	public T getValue() {
		return value;
	}

	/**
	 * Returns the persisted type discriminator for this setting.
	 *
	 * @return setting type
	 */
	public abstract SettingType getType();

	/**
	 * Validates and assigns a new value.
	 *
	 * @param newValue proposed value
	 * @throws InvalidSettingValueException when validation fails
	 */
	public void setValue(T newValue) {
		if (Objects.equals(value, newValue)) {
			return;
		}

		ValidationResult result = validateValue(newValue);
		if (!result.valid()) {
			throw new InvalidSettingValueException(result.message());
		}

		T oldValue = value;
		value = newValue;
		notifyListeners(oldValue, newValue);
	}

	/**
	 * Validates and assigns a new value without throwing.
	 *
	 * @param newValue proposed value
	 * @return {@code true} when the value was accepted
	 */
	public boolean trySetValue(T newValue) {
		if (Objects.equals(value, newValue)) {
			return true;
		}

		ValidationResult result = validateValue(newValue);
		if (!result.valid()) {
			return false;
		}

		T oldValue = value;
		value = newValue;
		notifyListeners(oldValue, newValue);
		return true;
	}

	/**
	 * Restores the current value to the default.
	 */
	public void reset() {
		setValue(defaultValue);
	}

	/**
	 * Returns whether this setting should be shown.
	 *
	 * @return {@code true} when visible
	 */
	public boolean isVisible() {
		return visibilityPredicate.test(this);
	}

	/**
	 * Returns whether this setting should accept user interaction.
	 *
	 * @return {@code true} when enabled
	 */
	public boolean isEnabled() {
		return enabledPredicate.test(this);
	}

	/**
	 * Sets the predicate that controls setting visibility.
	 *
	 * @param predicate visibility predicate
	 */
	public void setVisibilityPredicate(Predicate<Setting<?>> predicate) {
		this.visibilityPredicate = Objects.requireNonNull(predicate, "predicate");
	}

	/**
	 * Sets the predicate that controls whether the setting is interactive.
	 *
	 * @param predicate enabled predicate
	 */
	public void setEnabledPredicate(Predicate<Setting<?>> predicate) {
		this.enabledPredicate = Objects.requireNonNull(predicate, "predicate");
	}

	/**
	 * Registers a listener for value changes.
	 *
	 * @param listener change listener
	 */
	public void addChangeListener(SettingChangeListener<T> listener) {
		listeners.add(Objects.requireNonNull(listener, "listener"));
	}

	/**
	 * Removes a previously registered change listener.
	 *
	 * @param listener change listener
	 * @return {@code true} when the listener was removed
	 */
	public boolean removeChangeListener(SettingChangeListener<T> listener) {
		return listeners.remove(listener);
	}

	/**
	 * Serializes the current value for future persistence.
	 *
	 * @return serialized value
	 */
	public String serializeValue() {
		return serializeValue(value);
	}

	/**
	 * Deserializes and assigns a persisted value.
	 *
	 * @param serialized persisted value
	 * @throws InvalidSettingValueException when the serialized value is invalid
	 */
	public void loadValue(String serialized) {
		setValue(deserializeValue(serialized));
	}

	/**
	 * Serializes a value of this setting's type.
	 *
	 * @param value value to serialize
	 * @return serialized value
	 */
	public abstract String serializeValue(T value);

	/**
	 * Deserializes a persisted value of this setting's type.
	 *
	 * @param serialized persisted value
	 * @return deserialized value
	 */
	public abstract T deserializeValue(String serialized);

	/**
	 * Validates a proposed value without assigning it.
	 *
	 * @param value proposed value
	 * @return validation result
	 */
	protected abstract ValidationResult validateValue(T value);

	private void notifyListeners(T oldValue, T newValue) {
		for (int i = 0, size = listeners.size(); i < size; i++) {
			listeners.get(i).onChanged(this, oldValue, newValue);
		}
	}
}
