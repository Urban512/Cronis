package dev.cronis.gui.component;

import dev.cronis.gui.layout.Padding;
import dev.cronis.gui.theme.GuiMetrics;
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
		GuiMetrics.Elevation elevation = GuiMetrics.ELEVATION_WINDOW;
		return new GuiWindowStyle(
				theme.windowBackground(),
				theme.windowBorder(),
				GuiMetrics.RADIUS_WINDOW,
				elevation.shadowRadius(),
				elevation.shadowOpacity(),
				theme.textPrimary(),
				Padding.all(0),
				0.76f,
				0.82f,
				500,
				340,
				GuiMetrics.SCREEN_MARGIN,
				false
		);
	}
}
