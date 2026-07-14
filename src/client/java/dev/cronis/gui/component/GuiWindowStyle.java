package dev.cronis.gui.component;

import dev.cronis.gui.layout.Padding;
import dev.cronis.gui.theme.GuiTheme;
import dev.cronis.gui.theme.ThemeManager;

/**
 * Immutable visual configuration for {@link GuiWindow}.
 */
public record GuiWindowStyle(
		int backgroundColor,
		int borderColor,
		int cornerRadius,
		int shadowRadius,
		float shadowOpacity,
		int titleColor,
		Padding padding,
		float widthRatio,
		float heightRatio,
		int minWidth,
		int minHeight,
		int screenMargin,
		boolean blurBackground
) {
	/**
	 * Returns the default Cronis application window styling.
	 *
	 * @return default style derived from the active theme
	 */
	public static GuiWindowStyle defaults() {
		GuiTheme theme = ThemeManager.get();
		return new GuiWindowStyle(
				theme.windowBackground(),
				theme.windowBorder(),
				16,
				14,
				0.38f,
				theme.textPrimary(),
				Padding.all(0),
				0.72f,
				0.78f,
				320,
				240,
				20,
				false
		);
	}
}
