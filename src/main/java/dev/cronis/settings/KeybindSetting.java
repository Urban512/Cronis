package dev.cronis.settings;

/**
 * Key binding setting storing a GLFW key code.
 * <p>
 * {@link #UNBOUND} ({@code -1}) represents no assigned key.
 */
public final class KeybindSetting extends Setting<Integer> {
	/**
	 * Sentinel value indicating the keybind is unassigned.
	 */
	public static final int UNBOUND = -1;

	/**
	 * Creates a keybind setting.
	 *
	 * @param id           unique identifier
	 * @param displayName  user-facing label
	 * @param description  explanatory text
	 * @param defaultValue default key code, or {@link #UNBOUND}
	 */
	public KeybindSetting(String id, String displayName, String description, int defaultValue) {
		super(id, displayName, description, defaultValue);

		ValidationResult defaultValidation = validateValue(defaultValue);
		if (!defaultValidation.valid()) {
			throw new IllegalArgumentException(defaultValidation.message());
		}
	}

	/**
	 * Returns whether the keybind is currently unassigned.
	 *
	 * @return {@code true} when unbound
	 */
	public boolean isUnbound() {
		return getValue() == UNBOUND;
	}

	@Override
	public SettingType getType() {
		return SettingType.KEYBIND;
	}

	@Override
	protected ValidationResult validateValue(Integer value) {
		if (value == null) {
			return ValidationResult.error("Value must not be null");
		}
		if (value < UNBOUND) {
			return ValidationResult.error("Key code must be " + UNBOUND + " (unbound) or a non-negative GLFW key code");
		}
		return ValidationResult.ok();
	}

	@Override
	public String serializeValue(Integer value) {
		return Integer.toString(value);
	}

	@Override
	public Integer deserializeValue(String serialized) {
		return Integer.parseInt(serialized);
	}
}
