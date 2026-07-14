package dev.cronis.gui.component;

import dev.cronis.gui.layout.Spacing;
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
	private final boolean heading;

	public GuiLabel(String text, int color) {
		this(text, color, RenderUtil.TextAlignment.LEFT, false, false);
	}

	public GuiLabel(String text, int color, RenderUtil.TextAlignment alignment, boolean shadow) {
		this(text, color, alignment, shadow, false);
	}

	private GuiLabel(String text, int color, RenderUtil.TextAlignment alignment, boolean shadow, boolean heading) {
		this.text = text;
		this.color = color;
		this.alignment = alignment;
		this.shadow = shadow;
		this.heading = heading;
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

	public static GuiLabel heading(String text) {
		return new GuiLabel(text, ThemeManager.get().textPrimary(), RenderUtil.TextAlignment.LEFT, false, true);
	}

	@Override
	public int getPreferredHeight(int availableWidth) {
		return heading ? 14 : 10;
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		int textY = heading ? y + Spacing.XS : y;
		RenderUtil.drawAlignedText(context, font, text, x, textY, width, color, alignment, shadow);
	}
}
