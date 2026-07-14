package dev.cronis.gui.component;

import dev.cronis.gui.layout.Padding;

/**
 * Immutable visual configuration for {@link GuiWindow}.
 *
 * @param backgroundColor window surface color in ARGB format
 * @param cornerRadius    corner radius in pixels
 * @param shadowRadius    shadow spread in pixels
 * @param shadowOpacity   maximum shadow opacity in the range {@code 0.0-1.0}
 * @param titleColor      title text color in ARGB format
 * @param padding         internal content padding
 * @param widthRatio      preferred width as a fraction of the available screen width
 * @param heightRatio     preferred height as a fraction of the available screen height
 * @param minWidth        minimum window width in pixels
 * @param minHeight       minimum window height in pixels
 * @param screenMargin    minimum margin kept between the window and the screen edge
 * @param blurBackground  whether the window surface requests blur through {@link dev.cronis.gui.render.BlurRenderer}
 */
public record GuiWindowStyle(
		int backgroundColor,
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
	private static final int DEFAULT_BACKGROUND = 0xFF141820;
	private static final int DEFAULT_TITLE = 0xFFF4F6FA;

	/**
	 * Returns the default Cronis window styling.
	 *
	 * @return default style
	 */
	public static GuiWindowStyle defaults() {
		return new GuiWindowStyle(
				DEFAULT_BACKGROUND,
				10,
				12,
				0.42f,
				DEFAULT_TITLE,
				Padding.all(16),
				0.55f,
				0.62f,
				280,
				200,
				24,
				false
		);
	}
}
