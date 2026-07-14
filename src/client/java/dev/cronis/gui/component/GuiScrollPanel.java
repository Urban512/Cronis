package dev.cronis.gui.component;

import dev.cronis.gui.layout.Layout;
import dev.cronis.gui.render.RenderUtil;
import dev.cronis.gui.util.GuiBounds;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Scrollable container that clips and offsets its children vertically.
 */
public class GuiScrollPanel extends GuiComponent {
	private static final int SCROLL_STEP = 16;

	private final Layout layout;
	private float scrollOffset;
	private float maxScroll;

	public GuiScrollPanel(Layout layout) {
		this.layout = layout;
	}

	public void layoutChildren() {
		int contentHeight = layout.preferredHeight(width, getChildren());
		GuiBounds contentBounds = new GuiBounds(x, y - (int) scrollOffset, width, contentHeight);
		layout.layout(contentBounds, getChildren());
		maxScroll = Math.max(0, contentHeight - height);
		scrollOffset = Math.min(scrollOffset, maxScroll);
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		layoutChildren();
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
	}

	@Override
	protected boolean handleMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (!contains((int) mouseX, (int) mouseY)) {
			return false;
		}

		scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset - (float) scrollY * SCROLL_STEP));
		return true;
	}

	@Override
	protected void renderChildren(GuiGraphicsExtractor context, Font font) {
	}
}
