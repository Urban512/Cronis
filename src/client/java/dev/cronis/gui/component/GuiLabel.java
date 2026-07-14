package dev.cronis.gui.component;

import dev.cronis.gui.render.RenderUtil;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Non-interactive text display component.
 */
public class GuiLabel extends GuiComponent {
	private final String text;
	private final int color;
	private final RenderUtil.TextAlignment alignment;
	private final boolean shadow;

	public GuiLabel(String text, int color) {
		this(text, color, RenderUtil.TextAlignment.LEFT, false);
	}

	public GuiLabel(String text, int color, RenderUtil.TextAlignment alignment, boolean shadow) {
		this.text = text;
		this.color = color;
		this.alignment = alignment;
		this.shadow = shadow;
	}

	public static GuiLabel primary(String text) {
		return new GuiLabel(text, ThemeManager.get().textPrimary());
	}

	public static GuiLabel secondary(String text) {
		return new GuiLabel(text, ThemeManager.get().textSecondary());
	}

	public static GuiLabel muted(String text) {
		return new GuiLabel(text, ThemeManager.get().textMuted());
	}

	@Override
	public int getPreferredHeight(int availableWidth) {
		return 9;
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		RenderUtil.drawAlignedText(context, font, text, x, y, width, color, alignment, shadow);
	}
}
