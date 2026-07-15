package dev.cronis.widget;

/**
 * Declares how a widget draws its chrome.
 * <p>
 * Lightweight informational widgets use {@link #TEXT_ONLY}. Structured widgets
 * such as Spotify or future maps use {@link #CARD}.
 */
public enum WidgetSurfaceStyle {
	TEXT_ONLY,
	CARD
}
