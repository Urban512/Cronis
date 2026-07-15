package dev.cronis.widget.performance;

/**
 * Clock display formats supported by {@link ClockWidget}.
 */
public enum ClockFormat {
	HOUR_24("24-hour"),
	HOUR_12("12-hour");

	private final String displayName;

	ClockFormat(String displayName) {
		this.displayName = displayName;
	}

	public String displayName() {
		return displayName;
	}
}
