package dev.cronis.gui.overlay;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Manages layered overlays rendered above the normal GUI component hierarchy.
 */
public final class GuiOverlayManager {
	private final Map<GuiOverlayLayer, List<GuiOverlay>> layers = new EnumMap<>(GuiOverlayLayer.class);

	public GuiOverlayManager() {
		for (GuiOverlayLayer layer : GuiOverlayLayer.values()) {
			layers.put(layer, new ArrayList<>());
		}
	}

	/**
	 * Shows an overlay on its declared layer.
	 *
	 * @param overlay overlay to show
	 */
	public void show(GuiOverlay overlay) {
		List<GuiOverlay> layerOverlays = layers.get(overlay.layer());
		if (!layerOverlays.contains(overlay)) {
			layerOverlays.add(overlay);
		}
	}

	/**
	 * Hides an overlay without invoking {@link GuiOverlay#close()}.
	 *
	 * @param overlay overlay to hide
	 */
	public void hide(GuiOverlay overlay) {
		layers.get(overlay.layer()).remove(overlay);
	}

	/**
	 * Returns whether any overlay is active on the provided layer.
	 *
	 * @param layer overlay layer
	 * @return {@code true} when active overlays exist
	 */
	public boolean hasActive(GuiOverlayLayer layer) {
		for (GuiOverlay overlay : layers.get(layer)) {
			if (overlay.isActive()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether any overlay is active.
	 *
	 * @return {@code true} when any overlay is active
	 */
	public boolean hasAnyActive() {
		for (GuiOverlayLayer layer : GuiOverlayLayer.values()) {
			if (hasActive(layer)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Closes every active overlay on the provided layer.
	 *
	 * @param layer overlay layer
	 */
	public void closeLayer(GuiOverlayLayer layer) {
		List<GuiOverlay> layerOverlays = new ArrayList<>(layers.get(layer));
		for (GuiOverlay overlay : layerOverlays) {
			overlay.close();
		}
		layers.get(layer).clear();
	}

	/**
	 * Closes every active overlay.
	 */
	public void closeAll() {
		for (GuiOverlayLayer layer : GuiOverlayLayer.values()) {
			closeLayer(layer);
		}
	}

	public void update(float delta, int mouseX, int mouseY) {
		pruneInactive();
		for (GuiOverlayLayer layer : GuiOverlayLayer.values()) {
			for (GuiOverlay overlay : layers.get(layer)) {
				if (overlay.isActive()) {
					overlay.update(delta, mouseX, mouseY);
				}
			}
		}
		pruneInactive();
	}

	public void render(GuiGraphicsExtractor context, Font font) {
		renderLayer(context, font, GuiOverlayLayer.POPUP);
		renderLayer(context, font, GuiOverlayLayer.TOOLTIP);
		renderLayer(context, font, GuiOverlayLayer.MODAL);
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return dispatchMouseEvent(GuiOverlayLayer.MODAL, overlay -> overlay.mouseClicked(mouseX, mouseY, button))
				|| dispatchMouseEvent(GuiOverlayLayer.TOOLTIP, overlay -> overlay.mouseClicked(mouseX, mouseY, button))
				|| dispatchMouseEvent(GuiOverlayLayer.POPUP, overlay -> overlay.mouseClicked(mouseX, mouseY, button));
	}

	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return dispatchMouseEvent(GuiOverlayLayer.MODAL, overlay -> overlay.mouseReleased(mouseX, mouseY, button))
				|| dispatchMouseEvent(GuiOverlayLayer.TOOLTIP, overlay -> overlay.mouseReleased(mouseX, mouseY, button))
				|| dispatchMouseEvent(GuiOverlayLayer.POPUP, overlay -> overlay.mouseReleased(mouseX, mouseY, button));
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		return dispatchMouseEvent(GuiOverlayLayer.MODAL, overlay -> overlay.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
				|| dispatchMouseEvent(GuiOverlayLayer.TOOLTIP, overlay -> overlay.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
				|| dispatchMouseEvent(GuiOverlayLayer.POPUP, overlay -> overlay.mouseScrolled(mouseX, mouseY, scrollX, scrollY));
	}

	public boolean keyPressed(KeyEvent event) {
		return dispatchEvent(GuiOverlayLayer.MODAL, overlay -> overlay.keyPressed(event))
				|| dispatchEvent(GuiOverlayLayer.TOOLTIP, overlay -> overlay.keyPressed(event))
				|| dispatchEvent(GuiOverlayLayer.POPUP, overlay -> overlay.keyPressed(event));
	}

	private void renderLayer(GuiGraphicsExtractor context, Font font, GuiOverlayLayer layer) {
		for (GuiOverlay overlay : layers.get(layer)) {
			if (overlay.isActive()) {
				overlay.render(context, font);
			}
		}
	}

	private boolean dispatchMouseEvent(GuiOverlayLayer layer, OverlayMouseHandler handler) {
		List<GuiOverlay> layerOverlays = layers.get(layer);
		for (int index = layerOverlays.size() - 1; index >= 0; index--) {
			GuiOverlay overlay = layerOverlays.get(index);
			if (overlay.isActive() && handler.handle(overlay)) {
				return true;
			}
		}
		return false;
	}

	private boolean dispatchEvent(GuiOverlayLayer layer, OverlayKeyHandler handler) {
		List<GuiOverlay> layerOverlays = layers.get(layer);
		for (int index = layerOverlays.size() - 1; index >= 0; index--) {
			GuiOverlay overlay = layerOverlays.get(index);
			if (overlay.isActive() && handler.handle(overlay)) {
				return true;
			}
		}
		return false;
	}

	private void pruneInactive() {
		for (GuiOverlayLayer layer : GuiOverlayLayer.values()) {
			layers.get(layer).removeIf(overlay -> !overlay.isActive());
		}
	}

	@FunctionalInterface
	private interface OverlayMouseHandler {
		boolean handle(GuiOverlay overlay);
	}

	@FunctionalInterface
	private interface OverlayKeyHandler {
		boolean handle(GuiOverlay overlay);
	}
}
