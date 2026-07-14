package dev.cronis.settings;

/**
 * Integer setting with optional inclusive bounds.
 */
public final class IntSetting extends Setting<Integer> {
	private final int min;
	private final int max;

	/**
	 * Creates an unbounded integer setting.
	 *
	 * @param id           unique identifier
	 * @param displayName  user-facing label
	 * @param description  explanatory text
	 * @param defaultValue default value
	 */
	public IntSetting(String id, String displayName, String description, int defaultValue) {
		this(id, displayName, description, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}

	/**
	 * Creates a bounded integer setting.
	 *
	 * @param id           unique identifier
	 * @param displayName  user-facing label
	 * @param description  explanatory text
	 * @param defaultValue default value
	 * @param min          inclusive minimum
	 * @param max          inclusive maximum
	 */
	public IntSetting(String id, String displayName, String description, int defaultValue, int min, int max) {
		super(id, displayName, description, defaultValue);
		if (min > max) {
			throw new IllegalArgumentException("min must not exceed max");
		}
		this.min = min;
		this.max = max;

		ValidationResult defaultValidation = validateValue(defaultValue);
		if (!defaultValidation.valid()) {
			throw new IllegalArgumentException(defaultValidation.message());
		}
	}

	/**
	 * Returns the inclusive minimum allowed value.
	 *
	 * @return minimum value
	 */
	public int getMin() {
		return min;
	}

	/**
	 * Returns the inclusive maximum allowed value.
	 *
	 * @return maximum value
	 */
	public int getMax() {
		return max;
	}

	@Override
	public SettingType getType() {
		return SettingType.INT;
	}

	@Override
	protected ValidationResult validateValue(Integer value) {
		if (value == null) {
			return ValidationResult.error("Value must not be null");
		}
		if (value < min || value > max) {
			return ValidationResult.error("Value must be between " + min + " and " + max);
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
