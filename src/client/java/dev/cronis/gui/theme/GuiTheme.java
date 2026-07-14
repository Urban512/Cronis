package dev.cronis.gui.theme;

/**
 * Immutable visual tokens used across the Cronis interface.
 * <p>
 * Every color consumed by components must originate from a theme so future
 * community themes can recolor the entire product without code changes.
 *
 * @param windowBackground      primary application window surface
 * @param windowBorder          subtle window outline
 * @param sidebarBackground     sidebar panel surface
 * @param sidebarItemHover      sidebar item hover overlay
 * @param sidebarItemSelected   sidebar item selected surface
 * @param sidebarItemText       primary sidebar label color
 * @param sidebarItemTextMuted  muted sidebar label color
 * @param headerBackground      top header bar surface
 * @param headerDivider         separator between header and body
 * @param contentBackground     main content panel surface
 * @param cardBackground        card surface color
 * @param cardBorder            card outline color
 * @param textPrimary           primary body text
 * @param textSecondary         secondary descriptive text
 * @param textMuted             de-emphasized text
 * @param accent                primary accent color
 * @param searchBackground      search field background
 * @param searchBorder          search field border
 * @param searchBorderFocused   search field border when focused
 * @param searchPlaceholder     search placeholder text
 * @param iconDefault           default icon color
 * @param iconHover             icon hover color
 * @param shadow                drop shadow tint
 * @param logoAccent            accent used for branding elements
 */
public record GuiTheme(
		int windowBackground,
		int windowBorder,
		int sidebarBackground,
		int sidebarItemHover,
		int sidebarItemSelected,
		int sidebarItemText,
		int sidebarItemTextMuted,
		int headerBackground,
		int headerDivider,
		int contentBackground,
		int cardBackground,
		int cardBorder,
		int textPrimary,
		int textSecondary,
		int textMuted,
		int accent,
		int searchBackground,
		int searchBorder,
		int searchBorderFocused,
		int searchPlaceholder,
		int iconDefault,
		int iconHover,
		int shadow,
		int logoAccent
) {
	/**
	 * Returns the default Cronis dark theme.
	 *
	 * @return default theme
	 */
	public static GuiTheme cronisDark() {
		return new GuiTheme(
				0xFF12151C,
				0xFF252A35,
				0xFF0E1117,
				0xFF1C2230,
				0xFF1A2233,
				0xFFE8ECF2,
				0xFF8B95A8,
				0xFF141820,
				0xFF252A35,
				0xFF12151C,
				0xFF171B24,
				0xFF252A35,
				0xFFF0F3F8,
				0xFFB8C0CE,
				0xFF7A8496,
				0xFF5B8DEF,
				0xFF1A1F2A,
				0xFF2A3140,
				0xFF5B8DEF,
				0xFF6B7588,
				0xFF9AA3B5,
				0xFFC8D0DE,
				0xFF000000,
				0xFF5B8DEF
		);
	}
}
