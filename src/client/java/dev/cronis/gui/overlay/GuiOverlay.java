package dev.cronis.gui.overlay;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Overlay content rendered above the normal component hierarchy.
 */
public interface GuiOverlay {
	/**
	 * Returns the overlay layer used for ordering and input dispatch.
	 *
	 * @return overlay layer
	 */
	GuiOverlayLayer layer();

	/**
	 * Returns whether the overlay should participate in the current frame.
	 *
	 * @return {@code true} when active
	 */
	boolean isActive();

	/**
	 * Updates overlay state for the current frame.
	 *
	 * @param delta  frame delta time in seconds
	 * @param mouseX cursor X coordinate in GUI pixels
	 * @param mouseY cursor Y coordinate in GUI pixels
	 */
	void update(float delta, int mouseX, int mouseY);

	/**
	 * Renders the overlay content.
	 *
	 * @param context draw context
	 * @param font    active font
	 */
	void render(GuiGraphicsExtractor context, Font font);

	/**
	 * Handles a mouse button press on the overlay layer.
	 *
	 * @param mouseX mouse X coordinate in GUI pixels
	 * @param mouseY mouse Y coordinate in GUI pixels
	 * @param button mouse button
	 * @return {@code true} when handled
	 */
	default boolean mouseClicked(double mouseX, double mouseY, int button) {
		return false;
	}

	/**
	 * Handles a mouse button release on the overlay layer.
	 *
	 * @param mouseX mouse X coordinate in GUI pixels
	 * @param mouseY mouse Y coordinate in GUI pixels
	 * @param button mouse button
	 * @return {@code true} when handled
	 */
	default boolean mouseReleased(double mouseX, double mouseY, int button) {
		return false;
	}

	/**
	 * Handles a mouse scroll event on the overlay layer.
	 *
	 * @param mouseX  mouse X coordinate in GUI pixels
	 * @param mouseY  mouse Y coordinate in GUI pixels
	 * @param scrollX horizontal scroll delta
	 * @param scrollY vertical scroll delta
	 * @return {@code true} when handled
	 */
	default boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		return false;
	}

	/**
	 * Handles a key press on the overlay layer.
	 *
	 * @param event key event
	 * @return {@code true} when handled
	 */
	default boolean keyPressed(KeyEvent event) {
		return false;
	}

	/**
	 * Closes the overlay and removes it from active rendering.
	 */
	void close();
}
