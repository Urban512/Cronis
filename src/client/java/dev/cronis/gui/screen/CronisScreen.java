package dev.cronis.gui.screen;

import dev.cronis.gui.component.GuiWindow;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Primary entry screen for the Cronis interface.
 * <p>
 * Opened by the {@code /cronis} client command and hosts the root component tree
 * once the GUI is implemented.
 */
public class CronisScreen extends GuiScreen {
	private final GuiWindow window = new GuiWindow("Cronis");

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		super.extractRenderState(context, mouseX, mouseY, delta);
		window.centerOnScreen(this.width, this.height);
		window.render(context, this.font);
	}
}
