package dev.cronis.gui.screen;

import dev.cronis.gui.component.GuiApplicationWindow;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;

/**
 * Primary entry screen for the Cronis interface.
 */
public class CronisScreen extends GuiScreen {
	private final GuiApplicationWindow window = new GuiApplicationWindow();

	public CronisScreen() {
		attachFocusManager(window);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		super.extractRenderState(context, mouseX, mouseY, delta);
		window.centerOnScreen(this.width, this.height);
		window.update(delta, mouseX, mouseY);
		window.render(context, this.font);
		renderOverlays(context, this.font, delta, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		return dispatchOverlayMouseClicked(event.x(), event.y(), event.button())
				|| window.mouseClicked(event.x(), event.y(), event.button())
				|| super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		return dispatchOverlayMouseReleased(event.x(), event.y(), event.button())
				|| window.mouseReleased(event.x(), event.y(), event.button())
				|| super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		return dispatchOverlayMouseScrolled(mouseX, mouseY, scrollX, scrollY)
				|| window.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
				|| super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}
}
