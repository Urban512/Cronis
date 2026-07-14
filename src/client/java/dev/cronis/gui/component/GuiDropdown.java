package dev.cronis.gui.component;

import dev.cronis.gui.animation.FadeAnimation;
import dev.cronis.gui.focus.Focusable;
import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.overlay.GuiOverlay;
import dev.cronis.gui.overlay.GuiOverlayLayer;
import dev.cronis.gui.overlay.GuiOverlayManager;
import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.render.RoundedRenderer;
import dev.cronis.gui.theme.GuiTheme;
import dev.cronis.gui.theme.ThemeManager;
import dev.cronis.gui.util.GuiBounds;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Select control with expandable option list.
 */
public class GuiDropdown extends GuiComponent implements Focusable {
	private static final int HEIGHT = 30;
	private static final int ITEM_HEIGHT = 28;
	private static final int CORNER_RADIUS = 10;
	private static final int MENU_RADIUS = 8;

	private final List<String> options = new ArrayList<>();
	private final FadeAnimation hoverAnimation = new FadeAnimation(10f);
	private final FadeAnimation focusAnimation = new FadeAnimation(10f);
	private final FadeAnimation openAnimation = new FadeAnimation(12f);
	private final DropdownMenuOverlay menuOverlay = new DropdownMenuOverlay();
	private int selectedIndex;
	private boolean hovered;
	private boolean focused;
	private boolean open;
	private Consumer<Integer> onChange;

	public GuiDropdown(String... options) {
		this(Arrays.asList(options));
	}

	public GuiDropdown(List<String> options) {
		this.options.addAll(options);
		this.height = HEIGHT;
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public String getSelectedValue() {
		if (options.isEmpty()) {
			return "";
		}
		return options.get(Math.min(selectedIndex, options.size() - 1));
	}

	public void setSelectedIndex(int index) {
		if (options.isEmpty()) {
			selectedIndex = 0;
			return;
		}

		int clamped = Math.max(0, Math.min(options.size() - 1, index));
		if (selectedIndex == clamped) {
			return;
		}

		selectedIndex = clamped;
		if (onChange != null) {
			onChange.accept(selectedIndex);
		}
	}

	public GuiDropdown setOnChange(Consumer<Integer> onChange) {
		this.onChange = onChange;
		return this;
	}

	@Override
	public int getPreferredHeight(int availableWidth) {
		return HEIGHT;
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		hovered = enabled && contains(mouseX, mouseY);
		hoverAnimation.setTarget(hovered ? 1f : 0f);
		focusAnimation.setTarget(focused ? 1f : 0f);
		openAnimation.setTarget(open ? 1f : 0f);
		hoverAnimation.update(delta);
		focusAnimation.update(delta);
		openAnimation.update(delta);
		super.update(delta, mouseX, mouseY);
	}

	@Override
	protected boolean handleMouseClicked(double mouseX, double mouseY, int button) {
		if (!enabled) {
			return false;
		}

		if (contains((int) mouseX, (int) mouseY)) {
			requestFocus(this);
			if (open) {
				closeMenu();
			} else {
				openMenu();
			}
			return true;
		}

		if (open) {
			closeMenu();
		}

		return false;
	}

	@Override
	public void onFocusGained() {
		focused = true;
	}

	@Override
	public void onFocusLost() {
		focused = false;
		closeMenu();
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (!enabled || options.isEmpty()) {
			return false;
		}

		if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
			closeMenu();
			clearFocus();
			return true;
		}
		if (event.key() == GLFW.GLFW_KEY_ENTER || event.key() == GLFW.GLFW_KEY_SPACE) {
			if (open) {
				closeMenu();
			} else {
				openMenu();
			}
			return true;
		}
		if (event.key() == GLFW.GLFW_KEY_UP) {
			setSelectedIndex(selectedIndex - 1);
			return true;
		}
		if (event.key() == GLFW.GLFW_KEY_DOWN) {
			setSelectedIndex(selectedIndex + 1);
			return true;
		}
		return false;
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		return false;
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		var theme = ThemeManager.get();
		int background = ColorUtil.lerp(theme.dropdownBackground(), theme.controlHover(), hoverAnimation.getValue() * 0.35f);
		int border = ColorUtil.lerp(theme.controlBorder(), theme.controlBorderFocused(), focusAnimation.getValue());

		RoundedRenderer.fill(context, x, y, width, height, CORNER_RADIUS, background);
		RoundedRenderer.outline(context, x, y, width, height, CORNER_RADIUS, 1, border);

		String label = getSelectedValue();
		int textColor = enabled ? theme.textPrimary() : theme.controlDisabled();
		context.text(font, label, x + Spacing.MD, y + (height - font.lineHeight) / 2, textColor, false);
		drawChevron(context, theme.iconDefault());
	}

	private void openMenu() {
		open = true;
		GuiOverlayManager overlayManager = getOverlayManager();
		if (overlayManager != null) {
			overlayManager.show(menuOverlay);
		}
	}

	private void closeMenu() {
		open = false;
		GuiOverlayManager overlayManager = getOverlayManager();
		if (overlayManager != null) {
			overlayManager.hide(menuOverlay);
		}
	}

	private GuiBounds getMenuBounds() {
		int menuY = y + height + Spacing.XS;
		int menuHeight = options.size() * ITEM_HEIGHT;
		return new GuiBounds(x, menuY, width, menuHeight);
	}

	private void renderMenu(GuiGraphicsExtractor context, Font font, GuiTheme theme) {
		GuiBounds menuBounds = getMenuBounds();
		float alpha = openAnimation.getValue();
		int menuBackground = ColorUtil.withAlpha(theme.cardBackground(), alpha);
		int menuBorder = ColorUtil.withAlpha(theme.dropdownMenuBorder(), alpha);

		RoundedRenderer.fill(context, menuBounds.x(), menuBounds.y(), menuBounds.width(), menuBounds.height(), MENU_RADIUS, menuBackground);
		RoundedRenderer.outline(context, menuBounds.x(), menuBounds.y(), menuBounds.width(), menuBounds.height(), MENU_RADIUS, 1, menuBorder);

		for (int index = 0; index < options.size(); index++) {
			int itemY = menuBounds.y() + index * ITEM_HEIGHT;
			boolean selected = index == selectedIndex;
			if (selected) {
				int selectedColor = ColorUtil.withAlpha(theme.dropdownItemHover(), alpha * 0.9f);
				RoundedRenderer.fill(context, menuBounds.x() + 2, itemY + 2, menuBounds.width() - 4, ITEM_HEIGHT - 4, 6, selectedColor);
			}

			int itemColor = selected ? theme.textPrimary() : theme.textSecondary();
			context.text(font, options.get(index), menuBounds.x() + Spacing.MD, itemY + (ITEM_HEIGHT - font.lineHeight) / 2, itemColor, false);
		}
	}

	private boolean handleMenuMouseClicked(double mouseX, double mouseY) {
		GuiBounds menuBounds = getMenuBounds();
		if (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height) {
			return false;
		}

		if (mouseX >= menuBounds.x() && mouseX < menuBounds.right() && mouseY >= menuBounds.y() && mouseY < menuBounds.bottom()) {
			int relativeY = (int) mouseY - menuBounds.y();
			int index = relativeY / ITEM_HEIGHT;
			if (index >= 0 && index < options.size()) {
				setSelectedIndex(index);
			}
			closeMenu();
			return true;
		}

		closeMenu();
		return true;
	}

	private void drawChevron(GuiGraphicsExtractor context, int color) {
		int centerX = x + width - Spacing.LG;
		int centerY = y + height / 2;
		context.fill(centerX - 3, centerY - 1, centerX + 3, centerY, color);
		context.fill(centerX - 2, centerY, centerX + 2, centerY + 1, color);
		context.fill(centerX - 1, centerY + 1, centerX + 1, centerY + 2, color);
	}

	private final class DropdownMenuOverlay implements GuiOverlay {
		@Override
		public GuiOverlayLayer layer() {
			return GuiOverlayLayer.POPUP;
		}

		@Override
		public boolean isActive() {
			return open;
		}

		@Override
		public void update(float delta, int mouseX, int mouseY) {
			openAnimation.update(delta);
		}

		@Override
		public void render(GuiGraphicsExtractor context, Font font) {
			if (openAnimation.getValue() <= 0f) {
				return;
			}

			renderMenu(context, font, ThemeManager.get());
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			return handleMenuMouseClicked(mouseX, mouseY);
		}

		@Override
		public void close() {
			closeMenu();
		}
	}
}
