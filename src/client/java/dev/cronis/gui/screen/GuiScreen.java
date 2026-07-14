package dev.cronis.gui.screen;

/**
 * Framework-level base type for Cronis screens.
 * <p>
 * GuiScreen bridges Minecraft's {@link net.minecraft.client.gui.screens.Screen}
 * with the Cronis component tree, layout system, theme, and render pipeline.
 */
public abstract class GuiScreen extends net.minecraft.client.gui.screens.Screen {
	protected GuiScreen() {
		super(net.minecraft.network.chat.Component.literal("Cronis"));
	}
}
