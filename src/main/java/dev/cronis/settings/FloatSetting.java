package dev.cronis.settings;

/**
 * Single-precision floating point setting with optional inclusive bounds.
 */
public final class FloatSetting extends Setting<Float> {
	private final float min;
	private final float max;

	/**
	 * Creates an unbounded float setting.
	 *
	 * @param id           unique identifier
	 * @param displayName  user-facing label
	 * @param description  explanatory text
	 * @param defaultValue default value
	 */
	public FloatSetting(String id, String displayName, String description, float defaultValue) {
		this(id, displayName, description, defaultValue, Float.NEGATIVE_INFINITY, Float.POSITIVE_INFINITY);
	}

	/**
	 * Creates a bounded float setting.
	 *
	 * @param id           unique identifier
	 * @param displayName  user-facing label
	 * @param description  explanatory text
	 * @param defaultValue default value
	 * @param min          inclusive minimum
	 * @param max          inclusive maximum
	 */
	public FloatSetting(String id, String displayName, String description, float defaultValue, float min, float max) {
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
	public float getMin() {
		return min;
	}

	/**
	 * Returns the inclusive maximum allowed value.
	 *
	 * @return maximum value
	 */
	public float getMax() {
		return max;
	}

	@Override
	public SettingType getType() {
		return SettingType.FLOAT;
	}

	@Override
	protected ValidationResult validateValue(Float value) {
		if (value == null) {
			return ValidationResult.error("Value must not be null");
		}
		if (Float.isNaN(value)) {
			return ValidationResult.error("Value must not be NaN");
		}
		if (value < min || value > max) {
			return ValidationResult.error("Value must be between " + min + " and " + max);
		}
		return ValidationResult.ok();
	}

	@Override
	public String serializeValue(Float value) {
		return Float.toString(value);
	}

	@Override
	public Float deserializeValue(String serialized) {
		return Float.parseFloat(serialized);
	}
}
