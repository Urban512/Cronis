package dev.cronis.editor;

import dev.cronis.widget.Widget;
import dev.cronis.widget.WidgetBounds;
import dev.cronis.widget.WidgetPosition;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Framework-level scale adjustments for HUD editor interactions.
 * <p>
 * All widgets share the same {@link Widget} scale property; there is no
 * widget-specific scaling logic.
 */
public final class WidgetScaleController {
	private WidgetScaleController() {
	}

	/**
	 * Resolves the wheel scale step from keyboard modifiers.
	 * Shift wins over Ctrl when both are held (fine adjustment).
	 *
	 * @param shift whether Shift is held
	 * @param ctrl  whether Ctrl is held
	 * @return snap/adjust step
	 */
	public static float resolveStep(boolean shift, boolean ctrl) {
		if (shift) {
			return Widget.SCALE_STEP_FINE;
		}
		if (ctrl) {
			return Widget.SCALE_STEP_COARSE;
		}
		return Widget.SCALE_STEP;
	}

	/**
	 * Adjusts widget scale from a mouse-wheel delta using current keyboard modifiers.
	 *
	 * @param widget  selected widget
	 * @param scrollY vertical wheel delta (positive = up / increase scale)
	 * @return {@code true} when the scale value changed
	 */
	public static boolean adjustFromScroll(Widget widget, double scrollY) {
		return adjustFromScroll(widget, scrollY, isShiftDown(), isControlDown());
	}

	/**
	 * Adjusts widget scale from a mouse-wheel delta and reapplies layout size.
	 *
	 * @param widget  selected widget
	 * @param scrollY vertical wheel delta (positive = up / increase scale)
	 * @param shift   fine step when true
	 * @param ctrl    coarse step when true (ignored if shift)
	 * @return {@code true} when the scale value changed
	 */
	public static boolean adjustFromScroll(Widget widget, double scrollY, boolean shift, boolean ctrl) {
		if (widget == null || scrollY == 0.0) {
			return false;
		}

		float step = resolveStep(shift, ctrl);
		float direction = scrollY > 0.0 ? 1.0f : -1.0f;
		float previous = widget.getScale();
		widget.setScaleAndApplyLayout(previous + direction * step, step);
		return Float.compare(widget.getScale(), previous) != 0;
	}

	/**
	 * Sets scale, applies preferred×scale size, and repositions so the pivot corner stays fixed.
	 *
	 * @param widget       target widget
	 * @param scale        requested scale
	 * @param step         snap step
	 * @param handle       active corner handle being dragged
	 * @param pivotX       fixed opposite-corner X
	 * @param pivotY       fixed opposite-corner Y
	 * @param screenWidth  viewport width
	 * @param screenHeight viewport height
	 */
	public static void applyScalePreservingPivot(
			Widget widget,
			float scale,
			float step,
			WidgetResizeController.Handle handle,
			int pivotX,
			int pivotY,
			int screenWidth,
			int screenHeight
	) {
		widget.setScaleAndApplyLayout(scale, step);
		int width = widget.getWidth();
		int height = widget.getHeight();

		int x = switch (handle) {
			case TOP_LEFT, BOTTOM_LEFT -> pivotX - width;
			case TOP_RIGHT, BOTTOM_RIGHT -> pivotX;
		};
		int y = switch (handle) {
			case TOP_LEFT, TOP_RIGHT -> pivotY - height;
			case BOTTOM_LEFT, BOTTOM_RIGHT -> pivotY;
		};

		WidgetBounds bounds = new WidgetBounds(x, y, width, height);
		WidgetPosition position = widget.getAnchor().positionFromBounds(
				screenWidth,
				screenHeight,
				bounds,
				width,
				height
		);
		widget.setPosition(position);
	}

	public static boolean isShiftDown() {
		long window = Minecraft.getInstance().getWindow().handle();
		return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
				|| GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
	}

	public static boolean isControlDown() {
		long window = Minecraft.getInstance().getWindow().handle();
		return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
				|| GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
	}
}
