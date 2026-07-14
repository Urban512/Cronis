package dev.cronis.gui.component;

import dev.cronis.gui.animation.FadeAnimation;
import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.render.IconRenderer;
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
	private static final int CORNER_RADIUS = 10;
	private static final int HEIGHT = 30;
	private static final int ICON_SIZE = 12;
	private static final int MIN_WIDTH = 160;
	private static final int MAX_WIDTH = 360;

	private final String placeholder;
	private final FadeAnimation hoverAnimation = new FadeAnimation(10f);
	private final FadeAnimation focusAnimation = new FadeAnimation(10f);
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
		return Math.clamp(280, MIN_WIDTH, MAX_WIDTH);
	}

	/**
	 * Resolves a responsive width for the available header space.
	 *
	 * @param availableWidth maximum width allocated to the search bar
	 * @return clamped search bar width
	 */
	public int resolveWidth(int availableWidth) {
		return Math.clamp(availableWidth, MIN_WIDTH, MAX_WIDTH);
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
		int background = ColorUtil.lerp(theme.searchBackground(), theme.sidebarItemHover(), hoverAnimation.getValue() * 0.4f);
		int border = ColorUtil.lerp(theme.searchBorder(), theme.searchBorderFocused(), focusAnimation.getValue());

		RoundedRenderer.fill(context, x, y, width, height, CORNER_RADIUS, background);
		RoundedRenderer.outline(context, x, y, width, height, CORNER_RADIUS, 1, border);

		int iconX = x + Spacing.MD;
		int iconY = y + (height - ICON_SIZE) / 2;
		IconRenderer.draw(context, IconRenderer.Icon.SEARCH, iconX, iconY, ICON_SIZE, theme.searchPlaceholder());

		int textX = iconX + ICON_SIZE + Spacing.SM;
		context.text(font, placeholder, textX, y + (height - font.lineHeight) / 2, theme.searchPlaceholder(), false);
	}
}
