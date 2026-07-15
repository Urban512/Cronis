package dev.cronis.widget;

import dev.cronis.gui.render.CardRenderer;
import dev.cronis.gui.theme.GuiTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Shared HUD widget chrome used by every production widget.
 */
public final class WidgetSurface {
	private WidgetSurface() {
	}

	public static void render(
			GuiGraphicsExtractor graphics,
			int x,
			int y,
			int width,
			int height,
			GuiTheme theme
	) {
		CardRenderer.draw(
				graphics,
				x,
				y,
				width,
				height,
				CardRenderer.Style.widget(),
				theme.cardBackground(),
				theme.cardBorder(),
				theme.cardShadow()
		);
	}
}
