package dev.cronis.gui.render;

/**
 * Color manipulation helpers for Cronis GUI rendering.
 * <p>
 * All colors use 32-bit ARGB format. Alpha occupies the most significant byte.
 */
public final class ColorUtil {
	private ColorUtil() {
	}

	/**
	 * Packs separate ARGB channel values into a single color.
	 *
	 * @param alpha alpha channel in the range {@code 0-255}
	 * @param red   red channel in the range {@code 0-255}
	 * @param green green channel in the range {@code 0-255}
	 * @param blue  blue channel in the range {@code 0-255}
	 * @return packed ARGB color
	 */
	public static int argb(int alpha, int red, int green, int blue) {
		return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | (blue & 0xFF);
	}

	/**
	 * Extracts the alpha channel from a color.
	 *
	 * @param color packed ARGB color
	 * @return alpha channel in the range {@code 0-255}
	 */
	public static int alpha(int color) {
		return color >>> 24 & 0xFF;
	}

	/**
	 * Extracts the red channel from a color.
	 *
	 * @param color packed ARGB color
	 * @return red channel in the range {@code 0-255}
	 */
	public static int red(int color) {
		return color >> 16 & 0xFF;
	}

	/**
	 * Extracts the green channel from a color.
	 *
	 * @param color packed ARGB color
	 * @return green channel in the range {@code 0-255}
	 */
	public static int green(int color) {
		return color >> 8 & 0xFF;
	}

	/**
	 * Extracts the blue channel from a color.
	 *
	 * @param color packed ARGB color
	 * @return blue channel in the range {@code 0-255}
	 */
	public static int blue(int color) {
		return color & 0xFF;
	}

	/**
	 * Replaces the alpha channel of a color.
	 *
	 * @param color      source color
	 * @param alphaValue new alpha channel in the range {@code 0-255}
	 * @return color with the updated alpha channel
	 */
	public static int withAlpha(int color, int alphaValue) {
		return color & 0x00FFFFFF | (alphaValue & 0xFF) << 24;
	}

	/**
	 * Replaces the alpha channel of a color using a normalized value.
	 *
	 * @param color      source color
	 * @param alphaValue normalized alpha in the range {@code 0.0-1.0}
	 * @return color with the updated alpha channel
	 */
	public static int withAlpha(int color, float alphaValue) {
		return withAlpha(color, Math.round(clamp(alphaValue, 0f, 1f) * 255f));
	}

	/**
	 * Multiplies the alpha channel of a color by a scalar.
	 *
	 * @param color  source color
	 * @param factor alpha multiplier
	 * @return color with scaled alpha
	 */
	public static int multiplyAlpha(int color, float factor) {
		int scaledAlpha = Math.round(alpha(color) * factor);
		return withAlpha(color, Math.clamp(scaledAlpha, 0, 255));
	}

	/**
	 * Linearly interpolates between two colors across all ARGB channels.
	 *
	 * @param from start color
	 * @param to   end color
	 * @param t    interpolation factor in the range {@code 0.0-1.0}
	 * @return interpolated color
	 */
	public static int lerp(int from, int to, float t) {
		float amount = clamp(t, 0f, 1f);
		int a = Math.round(alpha(from) + (alpha(to) - alpha(from)) * amount);
		int r = Math.round(red(from) + (red(to) - red(from)) * amount);
		int g = Math.round(green(from) + (green(to) - green(from)) * amount);
		int b = Math.round(blue(from) + (blue(to) - blue(from)) * amount);
		return argb(a, r, g, b);
	}

	/**
	 * Adjusts the perceived brightness of a color while preserving alpha.
	 *
	 * @param color  source color
	 * @param factor brightness multiplier; values below {@code 1.0} darken, above {@code 1.0} lighten
	 * @return adjusted color
	 */
	public static int brightness(int color, float factor) {
		int r = Math.clamp(Math.round(red(color) * factor), 0, 255);
		int g = Math.clamp(Math.round(green(color) * factor), 0, 255);
		int b = Math.clamp(Math.round(blue(color) * factor), 0, 255);
		return withAlpha(argb(255, r, g, b), alpha(color));
	}

	/**
	 * Returns a subdued variant of a theme color for secondary content.
	 *
	 * @param color base theme color
	 * @return muted color
	 */
	public static int muted(int color) {
		return multiplyAlpha(brightness(color, 0.85f), 0.72f);
	}

	/**
	 * Returns an emphasized variant of a theme color for primary accents.
	 *
	 * @param color base theme color
	 * @return emphasized color
	 */
	public static int emphasis(int color) {
		return brightness(color, 1.12f);
	}

	/**
	 * Returns a readable foreground color for text placed on the given surface color.
	 *
	 * @param surfaceColor background or surface color
	 * @return contrasting foreground color
	 */
	public static int onSurface(int surfaceColor) {
		float luminance = (0.299f * red(surfaceColor) + 0.587f * green(surfaceColor) + 0.114f * blue(surfaceColor)) / 255f;
		return luminance > 0.58f ? 0xFF111318 : 0xFFF4F6FA;
	}

	/**
	 * Returns a disabled variant of a theme color.
	 *
	 * @param color base theme color
	 * @return disabled color
	 */
	public static int disabled(int color) {
		return multiplyAlpha(brightness(color, 0.75f), 0.45f);
	}

	private static float clamp(float value, float min, float max) {
		return Math.max(min, Math.min(max, value));
	}
}
