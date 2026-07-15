package dev.cronis.widget;

/**
 * Dedicated render pass for Cronis HUD widgets.
 * <p>
 * Rendering is isolated from registration, persistence, and update orchestration.
 */
public final class WidgetRenderer {
	/**
	 * Renders a single widget using its resolved context.
	 *
	 * @param widget  widget to render
	 * @param context widget-scoped context
	 */
	public void render(Widget widget, WidgetContext context) {
		if (!widget.isVisible()) {
			return;
		}

		WidgetBounds bounds = widget.getInteractionBounds(context);
		if (!bounds.intersectsViewport(context.screenWidth(), context.screenHeight())) {
			return;
		}

		widget.render(context.withWidgetBounds(bounds));
	}
}
