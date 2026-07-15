package dev.cronis.widget;

/**
 * High-level grouping used by the widget editor and module registry.
 */
public enum WidgetCategory {
	GENERAL("General"),
	HUD("HUD"),
	PERFORMANCE("Performance"),
	MEDIA("Media"),
	INFORMATION("Information"),
	UTILITY("Utility"),
	SKYBLOCK("SkyBlock"),
	SOCIAL("Social"),
	COMBAT("Combat"),
	DEVELOPER("Developer");

	private final String displayName;

	WidgetCategory(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * Returns the human-readable category label.
	 *
	 * @return display label
	 */
	public String displayName() {
		return displayName;
	}
}
