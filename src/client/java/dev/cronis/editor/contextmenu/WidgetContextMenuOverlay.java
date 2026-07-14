package dev.cronis.editor.contextmenu;

import dev.cronis.gui.component.GuiContextMenu;
import dev.cronis.gui.overlay.GuiOverlay;
import dev.cronis.gui.overlay.GuiOverlayLayer;
import dev.cronis.gui.overlay.GuiOverlayManager;
import dev.cronis.widget.Widget;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

/**
 * Overlay wrapper for the widget context menu.
 */
public final class WidgetContextMenuOverlay implements GuiOverlay {
	private static final int CURSOR_OFFSET = 4;

	private final GuiContextMenu menu = new GuiContextMenu();

	private boolean active;
	private Widget widget;
	private WidgetContextMenuHost host;
	private GuiOverlayManager overlayManager;

	/**
	 * Opens the context menu for the provided widget near the cursor.
	 *
	 * @param widget         target widget
	 * @param mouseX         cursor X coordinate in GUI pixels
	 * @param mouseY         cursor Y coordinate in GUI pixels
	 * @param host           editor host callbacks
	 * @param overlayManager overlay manager
	 */
	public void open(
			Widget widget,
			int mouseX,
			int mouseY,
			WidgetContextMenuHost host,
			GuiOverlayManager overlayManager
	) {
		this.widget = widget;
		this.host = host;
		this.overlayManager = overlayManager;
		menu.clearItems();

		for (WidgetContextMenuAction action : WidgetContextMenuRegistry.actions()) {
			if (!action.enabled().test(widget)) {
				continue;
			}

			menu.addItem(action.label(), () -> {
				action.handler().accept(new WidgetContextMenuContext(widget, host));
				close();
			});
		}

		active = !menu.getChildren().isEmpty();
		if (!active) {
			return;
		}

		menu.layoutAt(
				mouseX + CURSOR_OFFSET,
				mouseY + CURSOR_OFFSET,
				host.screenWidth(),
				host.screenHeight(),
				host.viewportContext().font()
		);
	}

	@Override
	public GuiOverlayLayer layer() {
		return GuiOverlayLayer.MODAL;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		if (!active) {
			return;
		}

		menu.update(delta, mouseX, mouseY);
	}

	@Override
	public void render(GuiGraphicsExtractor context, Font font) {
		if (!active) {
			return;
		}

		menu.render(context, font);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!active) {
			return false;
		}

		if (menu.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}

		close();
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (!active) {
			return false;
		}

		if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
			close();
			return true;
		}

		return false;
	}

	@Override
	public void close() {
		if (!active) {
			return;
		}

		active = false;
		if (overlayManager != null) {
			overlayManager.hide(this);
		}
		if (host != null) {
			host.onContextMenuClosed();
		}
	}
}
