package dev.cronis.gui.theme;

/**
 * Immutable visual tokens used across the Cronis interface.
 * <p>
 * Every color consumed by components must originate from a theme so future
 * community themes can recolor the entire product without code changes.
 *
 * @param windowBackground        primary application window surface
 * @param windowBorder            subtle window outline
 * @param sidebarBackground       sidebar panel surface
 * @param sidebarItemHover        sidebar item hover overlay
 * @param sidebarItemSelected     sidebar item selected surface
 * @param sidebarItemText         primary sidebar label color
 * @param sidebarItemTextMuted    muted sidebar label color
 * @param sidebarAccentIndicator  accent bar shown on selected sidebar items
 * @param headerBackground        top header bar surface
 * @param headerDivider           separator between header and body
 * @param contentBackground       main content panel surface
 * @param cardBackground          card surface color
 * @param cardBorder              card outline color
 * @param cardHoverBorder         card outline color when hovered
 * @param textPrimary             primary body text
 * @param textSecondary           secondary descriptive text
 * @param textMuted               de-emphasized text
 * @param accent                  primary accent color
 * @param accentHover             accent color on hover
 * @param searchBackground        search field background
 * @param searchBorder            search field border
 * @param searchBorderFocused     search field border when focused
 * @param searchPlaceholder       search placeholder text
 * @param iconDefault             default icon color
 * @param iconHover               icon hover color
 * @param shadow                  drop shadow tint
 * @param cardShadow              subtle card elevation shadow
 * @param logoAccent              accent used for branding elements
 * @param scrollbarTrack          scrollbar track surface
 * @param scrollbarThumb          scrollbar thumb color
 * @param scrollbarThumbHover     scrollbar thumb color when hovered
 * @param overlayFade             edge fade overlay matching the content surface
 */
public record GuiTheme(
		int windowBackground,
		int windowBorder,
		int sidebarBackground,
		int sidebarItemHover,
		int sidebarItemSelected,
		int sidebarItemText,
		int sidebarItemTextMuted,
		int sidebarAccentIndicator,
		int headerBackground,
		int headerDivider,
		int contentBackground,
		int cardBackground,
		int cardBorder,
		int cardHoverBorder,
		int textPrimary,
		int textSecondary,
		int textMuted,
		int accent,
		int accentHover,
		int searchBackground,
		int searchBorder,
		int searchBorderFocused,
		int searchPlaceholder,
		int iconDefault,
		int iconHover,
		int shadow,
		int cardShadow,
		int logoAccent,
		int scrollbarTrack,
		int scrollbarThumb,
		int scrollbarThumbHover,
		int overlayFade
) {
	/**
	 * Returns the default Cronis dark theme.
	 *
	 * @return default theme
	 */
	public static GuiTheme cronisDark() {
		return new GuiTheme(
				0xFF17181F,
				0xFF2B2D38,
				0xFF12141B,
				0xFF22252F,
				0xFF1E2129,
				0xFFD8DCE8,
				0xFF6B7280,
				0xFF8B5CF6,
				0xFF17181F,
				0xFF2B2D38,
				0xFF17181F,
				0xFF20212A,
				0xFF2B2D38,
				0xFF3D3F4D,
				0xFFFFFFFF,
				0xFFA3A6B5,
				0xFF6B7280,
				0xFF8B5CF6,
				0xFFA78BFA,
				0xFF1C1D26,
				0xFF2B2D38,
				0xFF8B5CF6,
				0xFF6B7280,
				0xFF8B95A8,
				0xFFC8D0DE,
				0x73000000,
				0x40000000,
				0xFF8B5CF6,
				0x00000000,
				0xFF3D3F4D,
				0xFF5A5D6E,
				0xFF17181F
		);
	}
}
