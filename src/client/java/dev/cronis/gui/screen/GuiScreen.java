package dev.cronis.gui.screen;

import dev.cronis.gui.component.GuiComponent;
import dev.cronis.gui.focus.FocusManager;
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
}
