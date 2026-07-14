package dev.cronis.gui.component;

import dev.cronis.gui.animation.FadeAnimation;
import dev.cronis.gui.focus.Focusable;
import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.render.RoundedRenderer;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

/**
 * Labeled checkbox for boolean selection.
 */
public class GuiCheckbox extends GuiComponent implements Focusable {
	private static final int BOX_SIZE = 18;
	private static final int CORNER_RADIUS = 5;

	private final String label;
	private final FadeAnimation hoverAnimation = new FadeAnimation(10f);
	private final FadeAnimation checkedAnimation = new FadeAnimation(12f);
	private final FadeAnimation focusAnimation = new FadeAnimation(10f);
	private boolean checked;
	private boolean hovered;
	private boolean focused;
	private Consumer<Boolean> onChange;

	public GuiCheckbox(String label, boolean checked) {
		this.label = label;
		this.checked = checked;
		this.height = 24;
		checkedAnimation.setImmediate(checked ? 1f : 0f);
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		if (this.checked == checked) {
			return;
		}

		this.checked = checked;
		checkedAnimation.setTarget(checked ? 1f : 0f);
		if (onChange != null) {
			onChange.accept(checked);
		}
	}

	public GuiCheckbox setOnChange(Consumer<Boolean> onChange) {
		this.onChange = onChange;
		return this;
	}

	@Override
	public int getPreferredHeight(int availableWidth) {
		return 24;
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		hovered = enabled && contains(mouseX, mouseY);
		hoverAnimation.setTarget(hovered ? 1f : 0f);
		checkedAnimation.setTarget(checked ? 1f : 0f);
		focusAnimation.setTarget(focused ? 1f : 0f);
		hoverAnimation.update(delta);
		checkedAnimation.update(delta);
		focusAnimation.update(delta);
		super.update(delta, mouseX, mouseY);
	}

	@Override
	protected boolean handleMouseClicked(double mouseX, double mouseY, int button) {
		if (!enabled || !contains((int) mouseX, (int) mouseY)) {
			return false;
		}

		requestFocus(this);
		setChecked(!checked);
		return true;
	}

	@Override
	public void onFocusGained() {
		focused = true;
	}

	@Override
	public void onFocusLost() {
		focused = false;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (!enabled) {
			return false;
		}

		if (event.key() == GLFW.GLFW_KEY_SPACE) {
			setChecked(!checked);
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
		int boxX = x;
		int boxY = y + (height - BOX_SIZE) / 2;
		int background = ColorUtil.lerp(theme.checkboxBackground(), theme.controlHover(), hoverAnimation.getValue() * 0.35f);
		int border = ColorUtil.lerp(theme.checkboxBorder(), theme.controlBorderFocused(), focusAnimation.getValue());

		RoundedRenderer.fill(context, boxX, boxY, BOX_SIZE, BOX_SIZE, CORNER_RADIUS, background);
		RoundedRenderer.outline(context, boxX, boxY, BOX_SIZE, BOX_SIZE, CORNER_RADIUS, 1, border);

		float mark = checkedAnimation.getValue();
		if (mark > 0f) {
			int markColor = ColorUtil.withAlpha(theme.checkboxMark(), mark);
			context.fill(boxX + 4, boxY + 9, boxX + 7, boxY + 12, markColor);
			context.fill(boxX + 7, boxY + 6, boxX + 14, boxY + 9, markColor);
		}

		int textColor = enabled ? theme.textPrimary() : theme.controlDisabled();
		context.text(font, label, boxX + BOX_SIZE + Spacing.SM, y + (height - font.lineHeight) / 2, textColor, false);
	}
}
