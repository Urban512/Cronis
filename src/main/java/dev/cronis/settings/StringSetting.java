package dev.cronis.settings;

/**
 * Text setting with optional maximum length.
 */
public final class StringSetting extends Setting<String> {
	private final int maxLength;

	/**
	 * Creates an unbounded string setting.
	 *
	 * @param id           unique identifier
	 * @param displayName  user-facing label
	 * @param description  explanatory text
	 * @param defaultValue default value
	 */
	public StringSetting(String id, String displayName, String description, String defaultValue) {
		this(id, displayName, description, defaultValue, Integer.MAX_VALUE);
	}

	/**
	 * Creates a length-limited string setting.
	 *
	 * @param id           unique identifier
	 * @param displayName  user-facing label
	 * @param description  explanatory text
	 * @param defaultValue default value
	 * @param maxLength    maximum allowed length
	 */
	public StringSetting(String id, String displayName, String description, String defaultValue, int maxLength) {
		super(id, displayName, description, defaultValue);
		if (maxLength < 0) {
			throw new IllegalArgumentException("maxLength must not be negative");
		}
		this.maxLength = maxLength;

		ValidationResult defaultValidation = validateValue(defaultValue);
		if (!defaultValidation.valid()) {
			throw new IllegalArgumentException(defaultValidation.message());
		}
	}

	/**
	 * Returns the maximum allowed string length.
	 *
	 * @return maximum length
	 */
	public int getMaxLength() {
		return maxLength;
	}

	@Override
	public SettingType getType() {
		return SettingType.STRING;
	}

	@Override
	protected ValidationResult validateValue(String value) {
		if (value == null) {
			return ValidationResult.error("Value must not be null");
		}
		if (value.length() > maxLength) {
			return ValidationResult.error("Value must not exceed " + maxLength + " characters");
		}
		return ValidationResult.ok();
	}

	@Override
	public String serializeValue(String value) {
		return value;
	}

	@Override
	public String deserializeValue(String serialized) {
		return serialized;
	}
}
