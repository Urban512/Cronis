package dev.cronis.gui.component;

import dev.cronis.gui.animation.FadeAnimation;
import dev.cronis.gui.animation.ValueAnimation;
import dev.cronis.gui.focus.Focusable;
import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.render.RoundedRenderer;
import dev.cronis.gui.theme.DesignTokens;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

/**
 * Animated on/off switch control.
 */
public class GuiToggle extends GuiComponent implements Focusable {
	private static final int TRACK_WIDTH = 40;
	private static final int TRACK_HEIGHT = 22;
	private static final int THUMB_SIZE = 16;

	private final FadeAnimation hoverAnimation = new FadeAnimation(DesignTokens.ANIM_HOVER);
	private final FadeAnimation focusAnimation = new FadeAnimation(DesignTokens.ANIM_FOCUS);
	private final ValueAnimation thumbAnimation = new ValueAnimation(DesignTokens.ANIM_PANEL, 0f, 1f, 0f);
	private boolean on;
	private boolean hovered;
	private boolean focused;
	private Consumer<Boolean> onChange;

	public GuiToggle(boolean on) {
		this.on = on;
		this.width = TRACK_WIDTH;
		this.height = TRACK_HEIGHT;
		thumbAnimation.setImmediate(on ? 1f : 0f);
	}

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		if (this.on == on) {
			return;
		}

		this.on = on;
		thumbAnimation.setTarget(on ? 1f : 0f);
		if (onChange != null) {
			onChange.accept(on);
		}
	}

	public GuiToggle setOnChange(Consumer<Boolean> onChange) {
		this.onChange = onChange;
		return this;
	}

	@Override
	public int getPreferredWidth(int availableHeight) {
		return TRACK_WIDTH;
	}

	@Override
	public int getPreferredHeight(int availableWidth) {
		return TRACK_HEIGHT;
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		hovered = enabled && contains(mouseX, mouseY);
		hoverAnimation.setTarget(hovered ? 1f : 0f);
		focusAnimation.setTarget(focused ? 1f : 0f);
		thumbAnimation.setTarget(on ? 1f : 0f);
		hoverAnimation.update(delta);
		focusAnimation.update(delta);
		thumbAnimation.update(delta);
		super.update(delta, mouseX, mouseY);
	}

	@Override
	protected boolean handleMouseClicked(double mouseX, double mouseY, int button) {
		if (!enabled || !contains((int) mouseX, (int) mouseY)) {
			return false;
		}

		requestFocus(this);
		setOn(!on);
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
			setOn(!on);
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
		int trackColor = ColorUtil.lerp(theme.toggleTrack(), theme.toggleTrackActive(), thumbAnimation.getValue());
		if (hoverAnimation.getValue() > 0f) {
			trackColor = ColorUtil.lerp(trackColor, theme.controlHover(), hoverAnimation.getValue() * 0.25f);
		}

		RoundedRenderer.fill(context, x, y, width, height, height / 2, trackColor);
		int border = ColorUtil.lerp(theme.controlBorder(), theme.controlBorderFocused(), focusAnimation.getValue());
		RoundedRenderer.outline(context, x, y, width, height, height / 2, 1, border);

		int thumbTravel = width - THUMB_SIZE - 4;
		int thumbX = x + 2 + Math.round(thumbTravel * thumbAnimation.getValue());
		int thumbY = y + (height - THUMB_SIZE) / 2;
		RoundedRenderer.fill(context, thumbX, thumbY, THUMB_SIZE, THUMB_SIZE, THUMB_SIZE / 2, theme.toggleThumb());
	}
}
