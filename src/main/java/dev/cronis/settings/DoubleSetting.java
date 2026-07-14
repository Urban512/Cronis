package dev.cronis.settings;

/**
 * Double-precision floating point setting with optional inclusive bounds.
 */
public final class DoubleSetting extends Setting<Double> {
	private final double min;
	private final double max;

	/**
	 * Creates an unbounded double setting.
	 *
	 * @param id           unique identifier
	 * @param displayName  user-facing label
	 * @param description  explanatory text
	 * @param defaultValue default value
	 */
	public DoubleSetting(String id, String displayName, String description, double defaultValue) {
		this(id, displayName, description, defaultValue, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
	}

	/**
	 * Creates a bounded double setting.
	 *
	 * @param id           unique identifier
	 * @param displayName  user-facing label
	 * @param description  explanatory text
	 * @param defaultValue default value
	 * @param min          inclusive minimum
	 * @param max          inclusive maximum
	 */
	public DoubleSetting(String id, String displayName, String description, double defaultValue, double min, double max) {
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
	public double getMin() {
		return min;
	}

	/**
	 * Returns the inclusive maximum allowed value.
	 *
	 * @return maximum value
	 */
	public double getMax() {
		return max;
	}

	@Override
	public SettingType getType() {
		return SettingType.DOUBLE;
	}

	@Override
	protected ValidationResult validateValue(Double value) {
		if (value == null) {
			return ValidationResult.error("Value must not be null");
		}
		if (Double.isNaN(value)) {
			return ValidationResult.error("Value must not be NaN");
		}
		if (value < min || value > max) {
			return ValidationResult.error("Value must be between " + min + " and " + max);
		}
		return ValidationResult.ok();
	}

	@Override
	public String serializeValue(Double value) {
		return Double.toString(value);
	}

	@Override
	public Double deserializeValue(String serialized) {
		return Double.parseDouble(serialized);
	}
}
