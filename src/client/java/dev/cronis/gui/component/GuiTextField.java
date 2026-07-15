package dev.cronis.gui.component;

import dev.cronis.gui.animation.FadeAnimation;
import dev.cronis.gui.focus.Focusable;
import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.render.CardRenderer;
import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.theme.DesignTokens;
import dev.cronis.gui.theme.GuiMetrics;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

/**
 * Single-line text input field.
 */
public class GuiTextField extends GuiComponent implements Focusable {
	private static final int CORNER_RADIUS = GuiMetrics.RADIUS_CONTROL;
	private static final int HEIGHT = GuiMetrics.HEIGHT_CONTROL;
	private static final int PADDING_X = Spacing.MD;

	private final String placeholder;
	private final int maxLength;
	private final FadeAnimation hoverAnimation = new FadeAnimation(DesignTokens.ANIM_HOVER);
	private final FadeAnimation focusAnimation = new FadeAnimation(DesignTokens.ANIM_FOCUS);
	private final StringBuilder text = new StringBuilder();
	private boolean hovered;
	private boolean focused;
	private int cursorBlink;
	private Consumer<String> onChange;
	private Consumer<String> onCommit;
	private Runnable onEditBegin;
	private Runnable onEditCancel;

	public GuiTextField(String placeholder) {
		this(placeholder, 128);
	}

	public GuiTextField(String placeholder, int maxLength) {
		this.placeholder = placeholder;
		this.maxLength = Math.max(1, maxLength);
		this.height = HEIGHT;
	}

	public String getText() {
		return text.toString();
	}

	public void setText(String value) {
		applyCommittedValue(value);
		notifyChange();
	}

	/**
	 * Replaces the visible text without notifying change listeners.
	 *
	 * @param value committed value to display
	 */
	public void applyCommittedValue(String value) {
		text.setLength(0);
		if (value != null) {
			text.append(truncate(value));
		}
	}

	public boolean isFocused() {
		return focused;
	}

	public GuiTextField setOnChange(Consumer<String> onChange) {
		this.onChange = onChange;
		return this;
	}

	public GuiTextField setOnCommit(Consumer<String> onCommit) {
		this.onCommit = onCommit;
		return this;
	}

	public GuiTextField setOnEditBegin(Runnable onEditBegin) {
		this.onEditBegin = onEditBegin;
		return this;
	}

	public GuiTextField setOnEditCancel(Runnable onEditCancel) {
		this.onEditCancel = onEditCancel;
		return this;
	}

	@Override
	public int getPreferredHeight(int availableWidth) {
		return HEIGHT;
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		hovered = contains(mouseX, mouseY);
		hoverAnimation.setTarget(hovered ? 1f : 0f);
		focusAnimation.setTarget(focused ? 1f : 0f);
		hoverAnimation.update(delta);
		focusAnimation.update(delta);
		if (focused) {
			cursorBlink++;
		}
		super.update(delta, mouseX, mouseY);
	}

	@Override
	protected boolean handleMouseClicked(double mouseX, double mouseY, int button) {
		if (!enabled || !contains((int) mouseX, (int) mouseY)) {
			return false;
		}

		requestFocus(this);
		return true;
	}

	@Override
	public void onFocusGained() {
		focused = true;
		cursorBlink = 0;
		if (onEditBegin != null) {
			onEditBegin.run();
		}
	}

	@Override
	public void onFocusLost() {
		if (!focused) {
			return;
		}

		focused = false;
		commitEdit();
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (!enabled) {
			return false;
		}

		if (event.key() == GLFW.GLFW_KEY_BACKSPACE && !text.isEmpty()) {
			text.deleteCharAt(text.length() - 1);
			notifyChange();
			return true;
		}
		if (event.key() == GLFW.GLFW_KEY_ENTER) {
			clearFocus();
			return true;
		}
		if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
			cancelEdit();
			clearFocus();
			return true;
		}
		return false;
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		if (!enabled) {
			return false;
		}

		int codepoint = event.codepoint();
		if (Character.isISOControl(codepoint)) {
			return false;
		}

		appendCodepoint(codepoint);
		return true;
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		var theme = ThemeManager.get();
		int background = ColorUtil.lerp(theme.controlBackground(), theme.controlHover(), hoverAnimation.getValue() * 0.35f);
		int border = ColorUtil.lerp(theme.controlBorder(), theme.controlBorderFocused(), focusAnimation.getValue());

		CardRenderer.draw(
				context,
				x,
				y,
				width,
				height,
				CardRenderer.Style.control(),
				background,
				border,
				focusAnimation.getValue() > 0.35f
		);

		int textY = y + (height - font.lineHeight) / 2;
		if (text.isEmpty()) {
			context.text(font, placeholder, x + PADDING_X, textY, theme.searchPlaceholder(), false);
			return;
		}

		String value = text.toString();
		int textColor = enabled ? theme.textPrimary() : theme.controlDisabled();
		context.text(font, value, x + PADDING_X, textY, textColor, false);

		if (focused && cursorBlink / 20 % 2 == 0) {
			int cursorX = x + PADDING_X + font.width(value);
			context.fill(cursorX, textY - 1, cursorX + 1, textY + font.lineHeight + 1, theme.accent());
		}
	}

	private void appendCodepoint(int codepoint) {
		if (text.length() >= maxLength) {
			return;
		}

		text.appendCodePoint(codepoint);
		notifyChange();
	}

	private String truncate(String value) {
		if (value.length() <= maxLength) {
			return value;
		}
		return value.substring(0, maxLength);
	}

	private void notifyChange() {
		if (onChange != null) {
			onChange.accept(text.toString());
		}
	}

	private void commitEdit() {
		if (onCommit != null) {
			onCommit.accept(text.toString());
		}
	}

	private void cancelEdit() {
		if (onEditCancel != null) {
			onEditCancel.run();
		}
	}
}
