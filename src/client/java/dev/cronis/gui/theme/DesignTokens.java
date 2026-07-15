package dev.cronis.gui.theme;

import dev.cronis.gui.layout.Padding;
import dev.cronis.gui.layout.Spacing;

/**
 * Central design tokens for Cronis rendering and layout.
 * <p>
 * Colors remain in {@link GuiTheme}. Components should consume these constants
 * instead of hardcoded pixel, motion, or typography values.
 */
public final class DesignTokens {
	public static final int BORDER_THICKNESS = 1;
	/** Default outline strength for idle surfaces. */
	public static final float BORDER_OPACITY = 0.48f;
	/** Softer outline for nested/low-emphasis surfaces. */
	public static final float BORDER_OPACITY_SUBTLE = 0.32f;
	/** Stronger outline for focus/hover emphasis. */
	public static final float BORDER_OPACITY_STRONG = 0.85f;

	public static final int CORNER_RADIUS_WINDOW = 16;
	public static final int CORNER_RADIUS_PANEL = 12;
	/** Menu cards share the panel radius for a unified desktop look. */
	public static final int CORNER_RADIUS_CARD = 12;
	public static final int CORNER_RADIUS_WIDGET = 10;
	public static final int CORNER_RADIUS_CONTROL = 8;
	public static final int CORNER_RADIUS_COMPACT = 6;
	public static final int CORNER_RADIUS_CHECKBOX = 4;

	public static final int SHADOW_SIZE_WINDOW = 14;
	public static final int SHADOW_SIZE_PANEL = 10;
	public static final int SHADOW_SIZE_CARD = 6;
	public static final int SHADOW_SIZE_WIDGET = 4;

	public static final int SIDEBAR_WIDTH = 220;
	public static final int INSPECTOR_WIDTH = 280;
	public static final int HEADER_HEIGHT = 56;
	public static final int SCREEN_MARGIN = Spacing.LG;

	public static final int HEIGHT_CONTROL = 34;
	public static final int HEIGHT_BUTTON = 34;
	public static final int HEIGHT_NAV_ITEM = 36;
	public static final int HEIGHT_INSPECTOR_HEADER = 34;

	public static final int ICON_SM = 12;
	public static final int ICON_MD = 14;
	public static final int ICON_LG = 16;
	public static final int LOGO_SIZE = 28;

	public static final int CARD_SPACING = Spacing.LG + Spacing.XS;
	public static final int WIDGET_SPACING = Spacing.SM;
	public static final int SECTION_SPACING = Spacing.XXL + Spacing.SM;
	public static final int ROW_SPACING = Spacing.MD;
	public static final int WIDGET_LINE_SPACING = Spacing.XS;

	public static final Padding WIDGET_PADDING = Padding.symmetric(Spacing.MD, Spacing.LG);
	public static final Padding CARD_PADDING = Padding.symmetric(Spacing.LG, Spacing.LG);
	public static final Padding PANEL_PADDING = Padding.all(Spacing.LG);
	public static final Padding CONTENT_PADDING = Padding.symmetric(Spacing.XXL + Spacing.XS, Spacing.XXL);

	public static final float TEXT_SCALE_PAGE = 1.15f;
	public static final float TEXT_SCALE_SECTION = 1.08f;
	public static final float TEXT_SCALE_TITLE = 1.0f;
	public static final float TEXT_SCALE_BODY = 1.0f;
	public static final float TEXT_SCALE_CAPTION = 0.85f;
	public static final float TEXT_SCALE_MUTED = 0.85f;
	/** Alias for metric/value caption scale. */
	public static final float TEXT_SCALE_VALUE = 0.92f;

	public static final int PROGRESS_HEIGHT = 3;

	/** Target transition duration for most UI fades (milliseconds). */
	public static final int ANIM_DURATION_MS = 150;
	public static final int ANIM_DURATION_FAST_MS = 120;
	public static final int ANIM_DURATION_PANEL_MS = 180;

	/**
	 * FadeAnimation speed (units per second) for a full 0→1 transition in
	 * {@code durationMs} milliseconds.
	 */
	public static float animSpeed(int durationMs) {
		int safe = Math.max(1, durationMs);
		return 1000f / safe;
	}

	public static final float ANIM_HOVER = animSpeed(ANIM_DURATION_MS);
	public static final float ANIM_FOCUS = animSpeed(ANIM_DURATION_FAST_MS);
	public static final float ANIM_PANEL = animSpeed(ANIM_DURATION_PANEL_MS);
	public static final float ANIM_OPEN = animSpeed(ANIM_DURATION_PANEL_MS);

	/** @deprecated Prefer {@link #ANIM_HOVER}. */
	@Deprecated
	public static final float ANIMATION_FAST = ANIM_FOCUS;
	/** @deprecated Prefer {@link #ANIM_HOVER}. */
	@Deprecated
	public static final float ANIMATION_NORMAL = ANIM_HOVER;
	/** @deprecated Prefer {@link #ANIM_PANEL}. */
	@Deprecated
	public static final float ANIMATION_SLOW = ANIM_PANEL;

	public static final float HOVER_OPACITY = 0.92f;
	public static final float HOVER_SCALE = 1.02f;
	public static final float FOCUS_OPACITY = 1.0f;
	public static final float DISABLED_OPACITY = 0.45f;

	public record Elevation(int shadowRadius, float shadowOpacity) {
	}

	public static final Elevation ELEVATION_WINDOW = new Elevation(SHADOW_SIZE_WINDOW, 0.36f);
	public static final Elevation ELEVATION_PANEL = new Elevation(SHADOW_SIZE_PANEL, 0.22f);
	public static final Elevation ELEVATION_CARD = new Elevation(SHADOW_SIZE_CARD, 0.12f);
	public static final Elevation ELEVATION_WIDGET = new Elevation(SHADOW_SIZE_WIDGET, 0.10f);

	private DesignTokens() {
	}
}
