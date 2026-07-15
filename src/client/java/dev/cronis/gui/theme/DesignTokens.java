package dev.cronis.gui.theme;

import dev.cronis.gui.layout.Padding;
import dev.cronis.gui.layout.Spacing;

/**
 * Central design tokens for Cronis rendering and layout.
 * <p>
 * Colors remain in {@link GuiTheme}. Use these constants instead of hardcoded pixel values.
 */
public final class DesignTokens {
	public static final int BORDER_THICKNESS = 1;

	public static final int CORNER_RADIUS_WINDOW = 16;
	public static final int CORNER_RADIUS_PANEL = 12;
	public static final int CORNER_RADIUS_CARD = 10;
	public static final int CORNER_RADIUS_WIDGET = 10;
	public static final int CORNER_RADIUS_CONTROL = 8;
	public static final int CORNER_RADIUS_COMPACT = 6;
	public static final int CORNER_RADIUS_CHECKBOX = 4;

	public static final int SHADOW_SIZE_WINDOW = 14;
	public static final int SHADOW_SIZE_PANEL = 10;
	public static final int SHADOW_SIZE_CARD = 6;
	public static final int SHADOW_SIZE_WIDGET = 4;

	public static final int SIDEBAR_WIDTH = 216;
	public static final int INSPECTOR_WIDTH = 272;
	public static final int HEADER_HEIGHT = 52;
	public static final int SCREEN_MARGIN = Spacing.LG;

	public static final int HEIGHT_CONTROL = 32;
	public static final int HEIGHT_BUTTON = 32;
	public static final int HEIGHT_NAV_ITEM = 34;
	public static final int HEIGHT_INSPECTOR_HEADER = 32;

	public static final int ICON_SM = 12;
	public static final int ICON_MD = 14;
	public static final int ICON_LG = 16;
	public static final int LOGO_SIZE = 28;

	public static final int CARD_SPACING = Spacing.MD;
	public static final int WIDGET_SPACING = Spacing.SM;
	public static final int SECTION_SPACING = Spacing.XL;
	public static final int ROW_SPACING = Spacing.MD;
	public static final int WIDGET_LINE_SPACING = Spacing.XS;

	public static final Padding WIDGET_PADDING = Padding.symmetric(Spacing.MD, Spacing.LG);
	public static final Padding CARD_PADDING = Padding.symmetric(Spacing.MD, Spacing.LG);
	public static final Padding PANEL_PADDING = Padding.all(Spacing.LG);
	public static final Padding CONTENT_PADDING = Padding.all(Spacing.XL);

	public static final float TEXT_SCALE_CAPTION = 0.85f;
	public static final float TEXT_SCALE_BODY = 1.0f;
	public static final float TEXT_SCALE_VALUE = 0.92f;

	public static final int PROGRESS_HEIGHT = 3;

	public static final float ANIMATION_FAST = 10f;
	public static final float ANIMATION_NORMAL = 12f;
	public static final float ANIMATION_SLOW = 14f;

	public static final float HOVER_OPACITY = 0.9f;
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
