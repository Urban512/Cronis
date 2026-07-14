package dev.cronis.widget;

import dev.cronis.gui.theme.GuiTheme;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Immutable frame context shared with widgets during update and render passes.
 */
public final class WidgetContext {
	private final GuiGraphicsExtractor graphics;
	private final Font font;
	private final float delta;
	private final int screenWidth;
	private final int screenHeight;
	private final int windowWidth;
	private final int windowHeight;
	private final int viewportX;
	private final int viewportY;
	private final double guiScale;
	private final WidgetBounds widgetBounds;

	private WidgetContext(
			GuiGraphicsExtractor graphics,
			Font font,
			float delta,
			int screenWidth,
			int screenHeight,
			int windowWidth,
			int windowHeight,
			int viewportX,
			int viewportY,
			double guiScale,
			WidgetBounds widgetBounds
	) {
		this.graphics = graphics;
		this.font = font;
		this.delta = delta;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		this.windowWidth = windowWidth;
		this.windowHeight = windowHeight;
		this.viewportX = viewportX;
		this.viewportY = viewportY;
		this.guiScale = guiScale;
		this.widgetBounds = widgetBounds;
	}

	/**
	 * Creates a viewport context from the active Minecraft window.
	 *
	 * @param graphics draw context
	 * @param font     active font
	 * @param delta    frame delta time in seconds
	 * @return viewport context without widget bounds
	 */
	public static WidgetContext create(GuiGraphicsExtractor graphics, Font font, float delta) {
		var window = Minecraft.getInstance().getWindow();
		return new WidgetContext(
				graphics,
				font,
				delta,
				window.getGuiScaledWidth(),
				window.getGuiScaledHeight(),
				window.getWidth(),
				window.getHeight(),
				window.getX(),
				window.getY(),
				window.getGuiScale(),
				WidgetBounds.zero()
		);
	}

	/**
	 * Returns a copy bound to a specific widget's resolved bounds.
	 *
	 * @param bounds widget bounds
	 * @return widget-scoped context
	 */
	public WidgetContext withWidgetBounds(WidgetBounds bounds) {
		return new WidgetContext(
				graphics,
				font,
				delta,
				screenWidth,
				screenHeight,
				windowWidth,
				windowHeight,
				viewportX,
				viewportY,
				guiScale,
				bounds
		);
	}

	public GuiGraphicsExtractor graphics() {
		return graphics;
	}

	public Font font() {
		return font;
	}

	public float delta() {
		return delta;
	}

	public int screenWidth() {
		return screenWidth;
	}

	public int screenHeight() {
		return screenHeight;
	}

	public int windowWidth() {
		return windowWidth;
	}

	public int windowHeight() {
		return windowHeight;
	}

	public int viewportX() {
		return viewportX;
	}

	public int viewportY() {
		return viewportY;
	}

	public double guiScale() {
		return guiScale;
	}

	public WidgetBounds widgetBounds() {
		return widgetBounds;
	}

	/**
	 * Returns the active Cronis theme for widget rendering.
	 *
	 * @return active theme
	 */
	public GuiTheme theme() {
		return ThemeManager.get();
	}
}
