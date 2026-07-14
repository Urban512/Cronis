package dev.cronis.settings;

/**
 * Thrown when a setting receives a value that fails validation.
 */
public final class InvalidSettingValueException extends IllegalArgumentException {
	/**
	 * Creates an exception for the given validation message.
	 *
	 * @param message validation failure reason
	 */
	public InvalidSettingValueException(String message) {
		super(message);
	}
}
