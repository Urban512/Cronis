package dev.cronis.gui.component;

import dev.cronis.gui.animation.FadeAnimation;
import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.render.RoundedRenderer;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Search input surface with hover and focus animations.
 * <p>
 * Searching is not implemented yet; this component is visual only.
 */
public class GuiSearchBar extends GuiComponent {
	private static final int CORNER_RADIUS = 8;
	private static final int HEIGHT = 28;

	private final String placeholder;
	private final FadeAnimation hoverAnimation = new FadeAnimation(8f);
	private final FadeAnimation focusAnimation = new FadeAnimation(8f);
	private boolean hovered;
	private boolean focused;

	public GuiSearchBar(String placeholder) {
		this.placeholder = placeholder;
		this.height = HEIGHT;
	}

	@Override
	public int getPreferredHeight(int availableWidth) {
		return HEIGHT;
	}

	@Override
	public int getPreferredWidth(int availableHeight) {
		return 220;
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		hovered = contains(mouseX, mouseY);
		hoverAnimation.setTarget(hovered ? 1f : 0f);
		focusAnimation.setTarget(focused ? 1f : 0f);
		hoverAnimation.update(delta);
		focusAnimation.update(delta);
		super.update(delta, mouseX, mouseY);
	}

	@Override
	protected boolean handleMouseClicked(double mouseX, double mouseY, int button) {
		if (contains((int) mouseX, (int) mouseY)) {
			focused = true;
			return true;
		}

		focused = false;
		return false;
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		var theme = ThemeManager.get();
		int background = ColorUtil.lerp(theme.searchBackground(), theme.sidebarItemHover(), hoverAnimation.getValue() * 0.35f);
		int border = ColorUtil.lerp(theme.searchBorder(), theme.searchBorderFocused(), focusAnimation.getValue());

		RoundedRenderer.fill(context, x, y, width, height, CORNER_RADIUS, background);
		RoundedRenderer.outline(context, x, y, width, height, CORNER_RADIUS, 1, border);
		context.text(font, placeholder, x + 10, y + (height - font.lineHeight) / 2, theme.searchPlaceholder(), false);
	}
}
