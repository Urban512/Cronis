package dev.cronis.settings;

/**
 * Boolean on/off setting.
 */
public final class BooleanSetting extends Setting<Boolean> {
	/**
	 * Creates a boolean setting.
	 *
	 * @param id           unique identifier
	 * @param displayName  user-facing label
	 * @param description  explanatory text
	 * @param defaultValue default value
	 */
	public BooleanSetting(String id, String displayName, String description, boolean defaultValue) {
		super(id, displayName, description, defaultValue);
	}

	@Override
	public SettingType getType() {
		return SettingType.BOOLEAN;
	}

	@Override
	protected ValidationResult validateValue(Boolean value) {
		return value == null ? ValidationResult.error("Value must not be null") : ValidationResult.ok();
	}

	@Override
	public String serializeValue(Boolean value) {
		return Boolean.toString(value);
	}

	@Override
	public Boolean deserializeValue(String serialized) {
		return Boolean.parseBoolean(serialized);
	}
}
