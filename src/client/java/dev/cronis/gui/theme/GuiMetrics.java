package dev.cronis.gui.theme;

import dev.cronis.gui.layout.Padding;

/**
 * Backward-compatible aliases for {@link DesignTokens}.
 */
public final class GuiMetrics {
	public static final int BORDER_THIN = DesignTokens.BORDER_THICKNESS;

	public static final int RADIUS_WINDOW = DesignTokens.CORNER_RADIUS_WINDOW;
	public static final int RADIUS_PANEL = DesignTokens.CORNER_RADIUS_PANEL;
	public static final int RADIUS_CARD = DesignTokens.CORNER_RADIUS_CARD;
	public static final int RADIUS_WIDGET = DesignTokens.CORNER_RADIUS_WIDGET;
	public static final int RADIUS_CONTROL = DesignTokens.CORNER_RADIUS_CONTROL;
	public static final int RADIUS_COMPACT = DesignTokens.CORNER_RADIUS_COMPACT;
	public static final int RADIUS_CHECKBOX = DesignTokens.CORNER_RADIUS_CHECKBOX;

	public static final int SIDEBAR_WIDTH = DesignTokens.SIDEBAR_WIDTH;
	public static final int INSPECTOR_WIDTH = DesignTokens.INSPECTOR_WIDTH;
	public static final int HEADER_HEIGHT = DesignTokens.HEADER_HEIGHT;
	public static final int SCREEN_MARGIN = DesignTokens.SCREEN_MARGIN;

	public static final int HEIGHT_CONTROL = DesignTokens.HEIGHT_CONTROL;
	public static final int HEIGHT_BUTTON = DesignTokens.HEIGHT_BUTTON;
	public static final int HEIGHT_NAV_ITEM = DesignTokens.HEIGHT_NAV_ITEM;
	public static final int HEIGHT_INSPECTOR_HEADER = DesignTokens.HEIGHT_INSPECTOR_HEADER;

	public static final int ICON_SM = DesignTokens.ICON_SM;
	public static final int ICON_MD = DesignTokens.ICON_MD;
	public static final int ICON_LG = DesignTokens.ICON_LG;
	public static final int LOGO_SIZE = DesignTokens.LOGO_SIZE;
	public static final int LOGO_RADIUS = DesignTokens.CORNER_RADIUS_COMPACT;

	public static final int SECTION_GAP = DesignTokens.SECTION_SPACING;
	public static final int CARD_GAP = DesignTokens.CARD_SPACING;
	public static final int ROW_GAP = DesignTokens.ROW_SPACING;
	public static final int WIDGET_LINE_GAP = DesignTokens.WIDGET_LINE_SPACING;
	public static final int WIDGET_SECTION_GAP = DesignTokens.WIDGET_SPACING;

	public static final Padding PADDING_CARD = DesignTokens.CARD_PADDING;
	public static final Padding PADDING_WIDGET = DesignTokens.WIDGET_PADDING;
	public static final Padding PADDING_PANEL = DesignTokens.PANEL_PADDING;
	public static final Padding PADDING_CONTENT = DesignTokens.CONTENT_PADDING;

	public static final float TEXT_SCALE_CAPTION = DesignTokens.TEXT_SCALE_CAPTION;
	public static final float TEXT_SCALE_BODY = DesignTokens.TEXT_SCALE_BODY;
	public static final float TEXT_SCALE_VALUE = DesignTokens.TEXT_SCALE_VALUE;

	public static final int PROGRESS_HEIGHT = DesignTokens.PROGRESS_HEIGHT;

	public record Elevation(int shadowRadius, float shadowOpacity) {
	}

	public static final Elevation ELEVATION_WINDOW = toElevation(DesignTokens.ELEVATION_WINDOW);
	public static final Elevation ELEVATION_PANEL = toElevation(DesignTokens.ELEVATION_PANEL);
	public static final Elevation ELEVATION_CARD = toElevation(DesignTokens.ELEVATION_CARD);
	public static final Elevation ELEVATION_WIDGET = toElevation(DesignTokens.ELEVATION_WIDGET);

	private GuiMetrics() {
	}

	private static Elevation toElevation(DesignTokens.Elevation elevation) {
		return new Elevation(elevation.shadowRadius(), elevation.shadowOpacity());
	}
}
