package dev.cronis.gui.component;

import dev.cronis.gui.animation.FadeAnimation;
import dev.cronis.gui.animation.ValueAnimation;
import dev.cronis.gui.focus.Focusable;
import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.render.ProgressBarRenderer;
import dev.cronis.gui.render.RoundedRenderer;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

/**
 * Numeric range slider with draggable thumb and optional step snapping.
 */
public class GuiSlider extends GuiComponent implements Focusable {
	private static final int TRACK_HEIGHT = 6;
	private static final int THUMB_SIZE = 14;

	private final float min;
	private final float max;
	private final float step;
	private final FadeAnimation hoverAnimation = new FadeAnimation(10f);
	private final FadeAnimation focusAnimation = new FadeAnimation(10f);
	private final ValueAnimation valueAnimation;
	private float value;
	private boolean hovered;
	private boolean focused;
	private boolean dragging;
	private Consumer<Float> onChange;
	private Consumer<Float> onRelease;

	public GuiSlider(float min, float max, float value) {
		this(min, max, value, 0f);
	}

	public GuiSlider(float min, float max, float value, float step) {
		this.min = min;
		this.max = max;
		this.step = Math.max(0f, step);
		this.value = snap(clamp(value));
		this.height = 28;
		this.valueAnimation = new ValueAnimation(16f, min, max, this.value);
		this.valueAnimation.setImmediate(this.value);
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		float snapped = snap(clamp(value));
		if (this.value == snapped) {
			return;
		}

		this.value = snapped;
		valueAnimation.setTarget(snapped);
		if (onChange != null) {
			onChange.accept(snapped);
		}
	}

	/**
	 * Sets the slider value without firing {@code onChange}.
	 */
	public void setValueSilent(float value) {
		float snapped = snap(clamp(value));
		this.value = snapped;
		valueAnimation.setImmediate(snapped);
	}

	public GuiSlider setOnChange(Consumer<Float> onChange) {
		this.onChange = onChange;
		return this;
	}

	/**
	 * Invoked when a drag interaction ends or keyboard adjustment settles.
	 */
	public GuiSlider setOnRelease(Consumer<Float> onRelease) {
		this.onRelease = onRelease;
		return this;
	}

	@Override
	public int getPreferredHeight(int availableWidth) {
		return 28;
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		if (dragging && enabled) {
			setValue(valueFromMouse(mouseX));
		}

		hovered = enabled && contains(mouseX, mouseY);
		hoverAnimation.setTarget(hovered || dragging ? 1f : 0f);
		focusAnimation.setTarget(focused ? 1f : 0f);
		valueAnimation.setTarget(value);
		hoverAnimation.update(delta);
		focusAnimation.update(delta);
		valueAnimation.update(delta);
		super.update(delta, mouseX, mouseY);
	}

	@Override
	protected boolean handleMouseClicked(double mouseX, double mouseY, int button) {
		if (!enabled || !contains((int) mouseX, (int) mouseY)) {
			return false;
		}

		requestFocus(this);
		dragging = true;
		setValue(valueFromMouse((int) mouseX));
		return true;
	}

	@Override
	protected boolean handleMouseReleased(double mouseX, double mouseY, int button) {
		if (!dragging) {
			return false;
		}

		dragging = false;
		if (onRelease != null) {
			onRelease.accept(value);
		}
		return true;
	}

	@Override
	public void onFocusGained() {
		focused = true;
	}

	@Override
	public void onFocusLost() {
		focused = false;
		dragging = false;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (!enabled) {
			return false;
		}

		float keyStep = step > 0f ? step : (max - min) * 0.05f;
		if (event.key() == GLFW.GLFW_KEY_LEFT) {
			setValue(value - keyStep);
			if (onRelease != null) {
				onRelease.accept(value);
			}
			return true;
		}
		if (event.key() == GLFW.GLFW_KEY_RIGHT) {
			setValue(value + keyStep);
			if (onRelease != null) {
				onRelease.accept(value);
			}
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
		int trackY = y + (height - TRACK_HEIGHT) / 2;
		float ratio = normalize(valueAnimation.getValue());
		ProgressBarRenderer.draw(context, x, trackY, width, TRACK_HEIGHT, ratio, theme.sliderTrack(), theme.sliderFill());

		int thumbX = x + Math.round((width - THUMB_SIZE) * ratio);
		int thumbY = y + (height - THUMB_SIZE) / 2;
		if (hoverAnimation.getValue() > 0f) {
			int halo = ColorUtil.withAlpha(theme.controlHover(), hoverAnimation.getValue() * 0.5f);
			RoundedRenderer.fill(context, thumbX - 2, thumbY - 2, THUMB_SIZE + 4, THUMB_SIZE + 4, (THUMB_SIZE + 4) / 2, halo);
		}
		RoundedRenderer.fill(context, thumbX, thumbY, THUMB_SIZE, THUMB_SIZE, THUMB_SIZE / 2, theme.sliderThumb());
	}

	private float valueFromMouse(int mouseX) {
		if (width <= THUMB_SIZE) {
			return min;
		}

		float ratio = (mouseX - x - THUMB_SIZE / 2f) / (width - THUMB_SIZE);
		return min + (max - min) * Math.max(0f, Math.min(1f, ratio));
	}

	private float normalize(float amount) {
		if (max <= min) {
			return 0f;
		}
		return (amount - min) / (max - min);
	}

	private float clamp(float amount) {
		return Math.max(min, Math.min(max, amount));
	}

	private float snap(float amount) {
		if (step <= 0f) {
			return amount;
		}
		return Math.round(amount / step) * step;
	}
}
