package dev.cronis.gui.component;

import dev.cronis.gui.layout.Padding;
import dev.cronis.gui.render.BlurRenderer;
import dev.cronis.gui.render.RenderUtil;
import dev.cronis.gui.render.RoundedRenderer;
import dev.cronis.gui.util.GuiBounds;
import dev.cronis.gui.util.GuiSize;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.Objects;

/**
 * Reusable framed container used as the root surface for Cronis screens.
 */
public class GuiWindow extends GuiComponent {
	private static final int TITLE_SPACING = 8;

	private final String title;
	private final GuiWindowStyle style;
	private final BlurRenderer blurRenderer;

	private boolean centered = true;

	/**
	 * Creates a window with the default Cronis styling.
	 *
	 * @param title window title
	 */
	public GuiWindow(String title) {
		this(title, GuiWindowStyle.defaults(), new BlurRenderer());
	}

	/**
	 * Creates a window with custom styling and blur integration.
	 */
	public GuiWindow(String title, GuiWindowStyle style, BlurRenderer blurRenderer) {
		this.title = Objects.requireNonNull(title, "title");
		this.style = Objects.requireNonNull(style, "style");
		this.blurRenderer = Objects.requireNonNull(blurRenderer, "blurRenderer");
	}

	public String getTitle() {
		return title;
	}

	public GuiWindowStyle getStyle() {
		return style;
	}

	public GuiBounds getContentBounds(Font font) {
		Padding padding = style.padding();
		int titleBlockHeight = titleBlockHeight(font);
		return new GuiBounds(
				x + padding.left(),
				y + padding.top() + titleBlockHeight,
				Math.max(0, width - padding.horizontal()),
				Math.max(0, height - padding.vertical() - titleBlockHeight)
		);
	}

	public void centerOnScreen(int screenWidth, int screenHeight) {
		centered = true;
		layout(screenWidth, screenHeight);
	}

	public void layout(int screenWidth, int screenHeight) {
		GuiSize size = calculateSize(screenWidth, screenHeight);
		width = size.width();
		height = size.height();

		if (centered) {
			x = RenderUtil.centerX(screenWidth, width);
			y = RenderUtil.centerY(screenHeight, height);
		}
	}

	public void setPosition(int x, int y) {
		centered = false;
		this.x = x;
		this.y = y;
	}

	@Override
	public void render(GuiGraphicsExtractor context, Font font) {
		if (width <= 0 || height <= 0) {
			return;
		}

		if (style.blurBackground()) {
			blurRenderer.drawBlurArea(context, x, y, width, height, style.cornerRadius());
		}

		RoundedRenderer.fill(context, x, y, width, height, style.cornerRadius(), style.backgroundColor());
		RoundedRenderer.outline(context, x, y, width, height, style.cornerRadius(), 1, style.borderColor());
		renderTitle(context, font);
		renderChildren(context, font);
	}

	protected GuiSize calculateSize(int screenWidth, int screenHeight) {
		int maxWidth = Math.max(style.minWidth(), screenWidth - style.screenMargin() * 2);
		int maxHeight = Math.max(style.minHeight(), screenHeight - style.screenMargin() * 2);
		int resolvedWidth = Math.clamp(Math.round(screenWidth * style.widthRatio()), style.minWidth(), maxWidth);
		int resolvedHeight = Math.clamp(Math.round(screenHeight * style.heightRatio()), style.minHeight(), maxHeight);
		return new GuiSize(resolvedWidth, resolvedHeight);
	}

	private void renderTitle(GuiGraphicsExtractor context, Font font) {
		if (title.isEmpty()) {
			return;
		}

		Padding padding = style.padding();
		int titleY = y + padding.top();
		int titleWidth = Math.max(0, width - padding.horizontal());
		RenderUtil.drawAlignedText(
				context,
				font,
				title,
				x + padding.left(),
				titleY,
				titleWidth,
				style.titleColor(),
				RenderUtil.TextAlignment.CENTER,
				false
		);
	}

	private int titleBlockHeight(Font font) {
		if (title.isEmpty()) {
			return 0;
		}

		return font.lineHeight + TITLE_SPACING;
	}
}
