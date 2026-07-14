package dev.cronis.settings;

/**
 * Outcome of validating a proposed setting value.
 *
 * @param valid   whether the value is acceptable
 * @param message human-readable reason when invalid; empty when valid
 */
public record ValidationResult(boolean valid, String message) {
	/**
	 * Returns a successful validation result.
	 *
	 * @return valid result
	 */
	public static ValidationResult ok() {
		return new ValidationResult(true, "");
	}

	/**
	 * Returns a failed validation result.
	 *
	 * @param message reason the value was rejected
	 * @return invalid result
	 */
	public static ValidationResult error(String message) {
		return new ValidationResult(false, message);
	}
}
