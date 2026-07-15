package dev.cronis.gui.component;

import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.render.RenderUtil;
import dev.cronis.gui.theme.GuiMetrics;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Non-interactive text display component.
 */
public class GuiLabel extends GuiComponent {
	private String text;
	private final int color;
	private final RenderUtil.TextAlignment alignment;
	private final boolean shadow;
	private final LabelStyle style;

	public enum LabelStyle {
		BODY,
		HEADING,
		SECTION
	}

	public GuiLabel(String text, int color) {
		this(text, color, RenderUtil.TextAlignment.LEFT, false, LabelStyle.BODY);
	}

	public GuiLabel(String text, int color, RenderUtil.TextAlignment alignment, boolean shadow) {
		this(text, color, alignment, shadow, LabelStyle.BODY);
	}

	private GuiLabel(String text, int color, RenderUtil.TextAlignment alignment, boolean shadow, LabelStyle style) {
		this.text = text;
		this.color = color;
		this.alignment = alignment;
		this.shadow = shadow;
		this.style = style;
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
		return new GuiLabel(text, ThemeManager.get().textPrimary(), RenderUtil.TextAlignment.LEFT, false, LabelStyle.HEADING);
	}

	public static GuiLabel section(String text) {
		return new GuiLabel(text, ThemeManager.get().textPrimary(), RenderUtil.TextAlignment.LEFT, false, LabelStyle.SECTION);
	}

	public static GuiLabel value(String text) {
		return new GuiLabel(text, ThemeManager.get().textPrimary());
	}

	/**
	 * Updates the displayed label text.
	 *
	 * @param text new label text
	 */
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public int getPreferredHeight(int availableWidth) {
		return switch (style) {
			case SECTION -> 9 + Spacing.MD;
			case HEADING -> 9 + Spacing.SM;
			case BODY -> 9;
		};
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		int textY = switch (style) {
			case SECTION, HEADING -> y + Spacing.XS;
			case BODY -> y;
		};
		RenderUtil.drawAlignedText(context, font, text, x, textY, width, color, alignment, shadow);
	}
}
