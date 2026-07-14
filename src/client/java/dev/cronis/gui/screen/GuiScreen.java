package dev.cronis.gui.screen;

import dev.cronis.gui.component.GuiComponent;
import dev.cronis.gui.focus.FocusManager;
import dev.cronis.gui.overlay.GuiOverlayManager;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

/**
 * Framework-level base type for Cronis screens.
 * <p>
 * GuiScreen bridges Minecraft's {@link net.minecraft.client.gui.screens.Screen}
 * with the Cronis component tree, layout system, theme, and render pipeline.
 */
public abstract class GuiScreen extends net.minecraft.client.gui.screens.Screen {
	protected final FocusManager focusManager = new FocusManager();
	protected final GuiOverlayManager overlays = new GuiOverlayManager();

	protected GuiScreen() {
		super(net.minecraft.network.chat.Component.literal("Cronis"));
	}

	/**
	 * Attaches the screen focus manager to a component subtree.
	 *
	 * @param root root component
	 */
	protected void attachFocusManager(GuiComponent root) {
		root.setFocusManager(focusManager);
		root.setOverlayManager(overlays);
	}

	protected void renderOverlays(net.minecraft.client.gui.GuiGraphicsExtractor context, net.minecraft.client.gui.Font font, float delta, int mouseX, int mouseY) {
		overlays.update(delta, mouseX, mouseY);
		overlays.render(context, font);
	}

	protected boolean dispatchOverlayMouseClicked(double mouseX, double mouseY, int button) {
		return overlays.mouseClicked(mouseX, mouseY, button);
	}

	protected boolean dispatchOverlayMouseReleased(double mouseX, double mouseY, int button) {
		return overlays.mouseReleased(mouseX, mouseY, button);
	}

	protected boolean dispatchOverlayMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		return overlays.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	protected boolean dispatchOverlayKeyPressed(net.minecraft.client.input.KeyEvent event) {
		return overlays.keyPressed(event);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (focusManager.dispatchKeyPressed(event)) {
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		if (focusManager.dispatchCharTyped(event)) {
			return true;
		}
		return super.charTyped(event);
	}

	@Override
	public void removed() {
		focusManager.clearFocus();
		super.removed();
	}
}
