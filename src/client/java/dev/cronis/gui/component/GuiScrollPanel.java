package dev.cronis.gui.component;

import dev.cronis.gui.animation.Easing;
import dev.cronis.gui.animation.FadeAnimation;
import dev.cronis.gui.layout.Layout;
import dev.cronis.gui.layout.Padding;
import dev.cronis.gui.render.RenderUtil;
import dev.cronis.gui.render.ScrollbarRenderer;
import dev.cronis.gui.theme.DesignTokens;
import dev.cronis.gui.util.GuiBounds;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Scrollable container that clips and offsets its children vertically.
 */
public class GuiScrollPanel extends GuiComponent {
	private static final float WHEEL_STEP = 28f;
	private static final float SMOOTH_SPEED = 14f;

	private final Layout layout;
	private Padding contentPadding = Padding.all(0);
	private float scrollOffset;
	private float scrollTarget;
	private float maxScroll;
	private int contentHeight;
	private final FadeAnimation scrollbarHoverAnimation = new FadeAnimation(DesignTokens.ANIM_HOVER);
	private boolean scrollbarHovered;

	public GuiScrollPanel(Layout layout) {
		this.layout = layout;
	}

	/**
	 * Sets internal padding applied around laid-out children.
	 *
	 * @param contentPadding padding inside the scroll viewport
	 */
	public void setContentPadding(Padding contentPadding) {
		this.contentPadding = contentPadding;
	}

	public void layoutChildren() {
		int contentWidth = Math.max(0, width - contentPadding.horizontal());
		contentHeight = layout.preferredHeight(contentWidth, getChildren()) + contentPadding.vertical();
		int contentX = x + contentPadding.left();
		int contentY = y + contentPadding.top() - Math.round(scrollOffset);
		GuiBounds contentBounds = new GuiBounds(contentX, contentY, contentWidth, contentHeight - contentPadding.vertical());
		layout.layout(contentBounds, getChildren());
		maxScroll = Math.max(0, contentHeight - height);
		scrollTarget = Math.max(0, Math.min(maxScroll, scrollTarget));
		scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		layoutChildren();
		scrollOffset = Easing.easeOut(scrollOffset, scrollTarget, delta, SMOOTH_SPEED);
		scrollbarHovered = isScrollbarHovered(mouseX, mouseY);
		scrollbarHoverAnimation.setTarget(scrollbarHovered ? 1f : 0f);
		scrollbarHoverAnimation.update(delta);
		super.update(delta, mouseX, mouseY);
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		layoutChildren();
		RenderUtil.scissor(context, x, y, width, height, () -> {
			for (GuiComponent child : getChildren()) {
				child.render(context, font);
			}
		});

		ScrollbarRenderer.drawEdgeFades(context, x, y, width, height, scrollOffset, contentHeight);
		ScrollbarRenderer.drawVertical(
				context,
				x + width,
				y,
				height,
				scrollOffset,
				contentHeight,
				scrollbarHoverAnimation.getValue()
		);
	}

	@Override
	protected boolean handleMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (!contains((int) mouseX, (int) mouseY)) {
			return false;
		}

		scrollTarget = Math.max(0, Math.min(maxScroll, scrollTarget - (float) scrollY * WHEEL_STEP));
		return true;
	}

	@Override
	protected void renderChildren(GuiGraphicsExtractor context, Font font) {
	}

	private boolean isScrollbarHovered(int mouseX, int mouseY) {
		if (contentHeight <= height) {
			return false;
		}

		return mouseX >= x + width - 12 && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}
}
