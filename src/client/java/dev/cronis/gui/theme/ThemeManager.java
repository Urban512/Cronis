package dev.cronis.gui.theme;

/**
 * Provides access to the active Cronis GUI theme.
 */
public final class ThemeManager {
	private static GuiTheme activeTheme = GuiTheme.cronisDark();

	private ThemeManager() {
	}

	/**
	 * Returns the currently active theme.
	 *
	 * @return active theme
	 */
	public static GuiTheme get() {
		return activeTheme;
	}

	/**
	 * Sets the active theme.
	 *
	 * @param theme theme to apply
	 */
	public static void set(GuiTheme theme) {
		activeTheme = theme;
	}
}
