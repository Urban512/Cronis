package dev.cronis.widget.persistence;

import dev.cronis.widget.Widget;
import dev.cronis.widget.WidgetPosition;
import dev.cronis.widget.WidgetSize;

import java.util.Objects;

/**
 * Immutable persisted layout state for a single widget.
 */
public record WidgetLayoutSnapshot(
		String id,
		boolean visible,
		WidgetPosition position,
		WidgetSize size,
		float scale
) {
	public WidgetLayoutSnapshot {
		Objects.requireNonNull(id, "id");
		Objects.requireNonNull(position, "position");
		Objects.requireNonNull(size, "size");
		scale = Widget.snapScale(scale);
	}

	/**
	 * Captures the current layout state from a widget.
	 *
	 * @param widget widget to capture
	 * @return layout snapshot
	 */
	public static WidgetLayoutSnapshot from(Widget widget) {
		Objects.requireNonNull(widget, "widget");
		return new WidgetLayoutSnapshot(
				widget.getId(),
				widget.isVisible(),
				widget.getPosition(),
				new WidgetSize(widget.getWidth(), widget.getHeight()),
				widget.getScale()
		);
	}

	/**
	 * Applies this snapshot to the provided widget.
	 * <p>
	 * For scale-only widgets, {@code scale} is authoritative and size is derived
	 * via {@link Widget#applyPreferredSize()}. Freeform widgets restore size.
	 *
	 * @param widget target widget
	 */
	public void applyTo(Widget widget) {
		Objects.requireNonNull(widget, "widget");
		if (!id.equals(widget.getId())) {
			throw new IllegalArgumentException("Layout id mismatch: expected " + id + ", got " + widget.getId());
		}

		widget.setVisible(visible);
		widget.setPosition(position);
		widget.setScale(scale);
		if (widget.supportsFreeformSize()) {
			widget.setSize(size.width(), size.height());
		} else {
			widget.applyPreferredSize();
		}
	}
}
