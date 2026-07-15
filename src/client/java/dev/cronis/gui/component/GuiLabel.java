package dev.cronis.gui.component;

import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.render.RenderUtil;
import dev.cronis.gui.theme.DesignTokens;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

/**
 * Non-interactive text display with a shared typography hierarchy.
 */
public class GuiLabel extends GuiComponent {
	private String text;
	private final int color;
	private final RenderUtil.TextAlignment alignment;
	private final boolean shadow;
	private final LabelStyle style;

	public enum LabelStyle {
		PAGE,
		SECTION,
		TITLE,
		BODY,
		CAPTION,
		MUTED,
		/** @deprecated Prefer {@link #TITLE}. */
		@Deprecated
		HEADING
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
		return new GuiLabel(text, ThemeManager.get().textMuted(), RenderUtil.TextAlignment.LEFT, false, LabelStyle.MUTED);
	}

	public static GuiLabel caption(String text) {
		return new GuiLabel(text, ThemeManager.get().textSecondary(), RenderUtil.TextAlignment.LEFT, false, LabelStyle.CAPTION);
	}

	public static GuiLabel page(String text) {
		return new GuiLabel(text, ThemeManager.get().textPrimary(), RenderUtil.TextAlignment.LEFT, false, LabelStyle.PAGE);
	}

	public static GuiLabel title(String text) {
		return new GuiLabel(text, ThemeManager.get().textPrimary(), RenderUtil.TextAlignment.LEFT, false, LabelStyle.TITLE);
	}

	/**
	 * @deprecated Prefer {@link #title(String)}.
	 */
	@Deprecated
	public static GuiLabel heading(String text) {
		return title(text);
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
		int line = Math.max(1, Math.round(9 * scaleFor(style)));
		return switch (resolvedStyle()) {
			case PAGE -> line + Spacing.MD;
			case SECTION -> line + Spacing.MD;
			case TITLE, HEADING -> line + Spacing.SM;
			case BODY -> line;
			case CAPTION, MUTED -> line;
		};
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		LabelStyle resolved = resolvedStyle();
		int textY = switch (resolved) {
			case PAGE, SECTION, TITLE, HEADING -> y + Spacing.XS;
			case BODY, CAPTION, MUTED -> y;
		};
		float scale = scaleFor(resolved);
		drawScaledAlignedText(context, font, text, x, textY, width, color, alignment, shadow, scale);
	}

	private LabelStyle resolvedStyle() {
		return style == LabelStyle.HEADING ? LabelStyle.TITLE : style;
	}

	private static float scaleFor(LabelStyle style) {
		return switch (style) {
			case PAGE -> DesignTokens.TEXT_SCALE_PAGE;
			case SECTION -> DesignTokens.TEXT_SCALE_SECTION;
			case TITLE, HEADING -> DesignTokens.TEXT_SCALE_TITLE;
			case BODY -> DesignTokens.TEXT_SCALE_BODY;
			case CAPTION -> DesignTokens.TEXT_SCALE_CAPTION;
			case MUTED -> DesignTokens.TEXT_SCALE_MUTED;
		};
	}

	private static void drawScaledAlignedText(
			GuiGraphicsExtractor context,
			Font font,
			String text,
			int x,
			int y,
			int width,
			int color,
			RenderUtil.TextAlignment alignment,
			boolean shadow,
			float scale
	) {
		int textWidth = Math.round(font.width(text) * scale);
		int drawX = switch (alignment) {
			case LEFT -> x;
			case CENTER -> x + Math.max(0, (width - textWidth) / 2);
			case RIGHT -> x + Math.max(0, width - textWidth);
		};

		if (scale == 1.0f) {
			context.text(font, text, drawX, y, color, shadow);
			return;
		}

		Matrix3x2fStack pose = context.pose();
		pose.pushMatrix();
		pose.translate(drawX, y);
		pose.scale(scale, scale);
		context.text(font, text, 0, 0, color, shadow);
		pose.popMatrix();
	}
}
