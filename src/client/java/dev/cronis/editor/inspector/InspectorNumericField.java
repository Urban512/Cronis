package dev.cronis.editor.inspector;

import dev.cronis.gui.animation.FadeAnimation;
import dev.cronis.gui.component.GuiComponent;
import dev.cronis.gui.focus.Focusable;
import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.render.CardRenderer;
import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.theme.DesignTokens;
import dev.cronis.gui.theme.GuiMetrics;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

/**
 * Numeric inspector field that keeps temporary edit text separate from committed values.
 */
public final class InspectorNumericField extends GuiComponent implements Focusable {
	public static final int HEIGHT = GuiMetrics.HEIGHT_CONTROL;

	private static final int CORNER_RADIUS = GuiMetrics.RADIUS_CONTROL;
	private static final int PADDING_X = Spacing.MD;
	private static final int MAX_LENGTH = 16;

	private final Mode mode;
	private final FadeAnimation hoverAnimation = new FadeAnimation(DesignTokens.ANIM_HOVER);
	private final FadeAnimation focusAnimation = new FadeAnimation(DesignTokens.ANIM_FOCUS);
	private final StringBuilder text = new StringBuilder();

	private String committedText = "";
	private int cursor;
	private int selectionStart;
	private int selectionEnd;
	private int cursorBlink;
	private boolean hovered;
	private boolean focused;
	private Consumer<String> onCommit;

	public InspectorNumericField(Mode mode) {
		this.mode = mode;
		this.height = HEIGHT;
	}

	public Mode mode() {
		return mode;
	}

	public boolean isEditing() {
		return focused;
	}

	public void setOnCommit(Consumer<String> onCommit) {
		this.onCommit = onCommit;
	}

	/**
	 * Updates the committed display value without interrupting an active edit session.
	 *
	 * @param value committed value text
	 */
	public void applyCommittedValue(String value) {
		committedText = value == null ? "" : truncate(value);
		if (!focused) {
			setEditText(committedText);
		}
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
		cursor = resolveCursorAt((int) mouseX, Minecraft.getInstance().font);
		clearSelection();
		return true;
	}

	@Override
	public void onFocusGained() {
		focused = true;
		cursorBlink = 0;
		setEditText(committedText);
		cursor = text.length();
		clearSelection();
	}

	@Override
	public void onFocusLost() {
		if (!focused) {
			return;
		}

		focused = false;
		commitOrRevert();
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (!enabled) {
			return false;
		}

		if (isSelectAllShortcut(event)) {
			selectAll();
			return true;
		}

		return switch (event.key()) {
			case GLFW.GLFW_KEY_BACKSPACE -> {
				deleteBeforeCursor();
				yield true;
			}
			case GLFW.GLFW_KEY_DELETE -> {
				deleteAfterCursor();
				yield true;
			}
			case GLFW.GLFW_KEY_LEFT -> {
				moveCursor(-1, isShiftDown());
				yield true;
			}
			case GLFW.GLFW_KEY_RIGHT -> {
				moveCursor(1, isShiftDown());
				yield true;
			}
			case GLFW.GLFW_KEY_HOME -> {
				cursor = 0;
				updateSelection(isShiftDown());
				yield true;
			}
			case GLFW.GLFW_KEY_END -> {
				cursor = text.length();
				updateSelection(isShiftDown());
				yield true;
			}
			case GLFW.GLFW_KEY_ENTER -> {
				commitOrRevert();
				clearFocus();
				yield true;
			}
			case GLFW.GLFW_KEY_ESCAPE -> {
				restoreFromCommitted();
				clearFocus();
				yield true;
			}
			default -> false;
		};
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

		insertCodepoint(codepoint);
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
		int textX = x + PADDING_X;
		String value = text.toString();
		int textColor = enabled ? theme.textPrimary() : theme.controlDisabled();

		if (hasSelection()) {
			int startX = textX + font.width(value.substring(0, selectionMin()));
			int endX = textX + font.width(value.substring(0, selectionMax()));
			context.fill(startX, textY - 1, endX, textY + font.lineHeight + 1, ColorUtil.withAlpha(theme.accent(), 0.35f));
		}

		if (!value.isEmpty()) {
			context.text(font, value, textX, textY, textColor, false);
		}

		if (focused && cursorBlink / 20 % 2 == 0) {
			int cursorX = textX + font.width(value.substring(0, Math.clamp(cursor, 0, value.length())));
			context.fill(cursorX, textY - 1, cursorX + 1, textY + font.lineHeight + 1, theme.accent());
		}
	}

	private void commitOrRevert() {
		String raw = text.toString().trim();
		if (!isCommitValid(raw)) {
			restoreFromCommitted();
			return;
		}

		committedText = raw;
		setEditText(committedText);
		if (onCommit != null) {
			onCommit.accept(raw);
		}
	}

	private boolean isCommitValid(String raw) {
		if (raw.isEmpty()) {
			return false;
		}

		try {
			return switch (mode) {
				case SIGNED_FLOAT -> {
					Float.parseFloat(raw);
					yield true;
				}
				case POSITIVE_INT -> Integer.parseInt(raw) > 0;
			};
		} catch (NumberFormatException exception) {
			return false;
		}
	}

	private void restoreFromCommitted() {
		setEditText(committedText);
		cursor = text.length();
		clearSelection();
	}

	private void setEditText(String value) {
		text.setLength(0);
		if (value != null) {
			text.append(truncate(value));
		}
	}

	private void insertCodepoint(int codepoint) {
		if (!isAllowedInput((char) codepoint)) {
			return;
		}

		deleteSelection();
		if (text.length() >= MAX_LENGTH) {
			return;
		}

		text.insert(cursor, Character.toString(codepoint));
		cursor++;
		clearSelection();
	}

	private boolean isAllowedInput(char character) {
		return switch (mode) {
			case SIGNED_FLOAT -> isAllowedSignedFloatInput(character);
			case POSITIVE_INT -> Character.isDigit(character);
		};
	}

	private boolean isAllowedSignedFloatInput(char character) {
		if (Character.isDigit(character)) {
			return true;
		}

		if (character == '-') {
			return cursor == 0 && text.indexOf("-") < 0;
		}

		if (character == '.') {
			return text.indexOf(".") < 0;
		}

		return false;
	}

	private void deleteBeforeCursor() {
		if (hasSelection()) {
			deleteSelection();
			return;
		}

		if (cursor <= 0) {
			return;
		}

		text.deleteCharAt(cursor - 1);
		cursor--;
	}

	private void deleteAfterCursor() {
		if (hasSelection()) {
			deleteSelection();
			return;
		}

		if (cursor >= text.length()) {
			return;
		}

		text.deleteCharAt(cursor);
	}

	private void deleteSelection() {
		if (!hasSelection()) {
			return;
		}

		text.delete(selectionMin(), selectionMax());
		cursor = selectionMin();
		clearSelection();
	}

	private void moveCursor(int delta, boolean extendSelection) {
		if (extendSelection) {
			if (!hasSelection()) {
				selectionStart = cursor;
			}
			cursor = Math.clamp(cursor + delta, 0, text.length());
			selectionEnd = cursor;
			return;
		}

		cursor = Math.clamp(cursor + delta, 0, text.length());
		clearSelection();
	}

	private void updateSelection(boolean extendSelection) {
		if (extendSelection) {
			if (!hasSelection()) {
				selectionStart = cursor;
			}
			selectionEnd = cursor;
			return;
		}

		clearSelection();
	}

	private void selectAll() {
		selectionStart = 0;
		selectionEnd = text.length();
		cursor = text.length();
	}

	private void clearSelection() {
		selectionStart = cursor;
		selectionEnd = cursor;
	}

	private boolean hasSelection() {
		return selectionStart != selectionEnd;
	}

	private int selectionMin() {
		return Math.min(selectionStart, selectionEnd);
	}

	private int selectionMax() {
		return Math.max(selectionStart, selectionEnd);
	}

	private int resolveCursorAt(int mouseX, Font font) {
		String value = text.toString();
		int relativeX = Math.max(0, mouseX - (x + PADDING_X));
		for (int index = 0; index <= value.length(); index++) {
			if (font.width(value.substring(0, index)) > relativeX) {
				return index;
			}
		}
		return value.length();
	}

	private static boolean isSelectAllShortcut(KeyEvent event) {
		return event.key() == GLFW.GLFW_KEY_A && isControlDown();
	}

	private static boolean isControlDown() {
		long window = Minecraft.getInstance().getWindow().handle();
		return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
				|| GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
	}

	private static boolean isShiftDown() {
		long window = Minecraft.getInstance().getWindow().handle();
		return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
				|| GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
	}

	private static String truncate(String value) {
		if (value.length() <= MAX_LENGTH) {
			return value;
		}
		return value.substring(0, MAX_LENGTH);
	}

	/**
	 * Numeric validation mode for inspector fields.
	 */
	public enum Mode {
		SIGNED_FLOAT,
		POSITIVE_INT
	}
}
