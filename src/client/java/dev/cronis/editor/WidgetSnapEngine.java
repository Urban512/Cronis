package dev.cronis.editor;

import dev.cronis.widget.Widget;
import dev.cronis.widget.WidgetBounds;
import dev.cronis.widget.WidgetContext;
import dev.cronis.widget.WidgetManager;

/**
 * Calculates snapping for drag and resize operations without mutating widget state.
 */
public final class WidgetSnapEngine {
	public static final int SNAP_THRESHOLD = 8;

	private final SnapGuideBuffer guides = new SnapGuideBuffer();
	private final int[] xTargets = new int[64];
	private final int[] yTargets = new int[64];

	public SnapGuideBuffer guides() {
		return guides;
	}

	/**
	 * Applies snapping to proposed bounds during a drag operation.
	 *
	 * @param proposed proposed bounds before snapping
	 * @param active   widget being moved
	 * @param context  viewport context
	 * @param grid     optional alignment grid
	 * @return snapped bounds
	 */
	public WidgetBounds snapDrag(WidgetBounds proposed, Widget active, WidgetContext context, WidgetGrid grid) {
		guides.clear();
		int xTargetCount = collectXTargets(active, context, grid, proposed.width());
		int yTargetCount = collectYTargets(active, context, grid, proposed.height());

		int deltaX = findAxisSnap(
				proposed.x(),
				proposed.right(),
				proposed.x() + proposed.width() / 2,
				xTargets,
				xTargetCount,
				context.screenWidth(),
				true
		);
		int deltaY = findAxisSnap(
				proposed.y(),
				proposed.bottom(),
				proposed.y() + proposed.height() / 2,
				yTargets,
				yTargetCount,
				context.screenHeight(),
				false
		);

		return proposed.translated(deltaX, deltaY).clampedToViewport(context.screenWidth(), context.screenHeight());
	}

	/**
	 * Applies snapping to proposed bounds during a resize operation.
	 *
	 * @param proposed proposed bounds before snapping
	 * @param active   widget being resized
	 * @param context  viewport context
	 * @param grid     optional alignment grid
	 * @return snapped bounds
	 */
	public WidgetBounds snapResize(WidgetBounds proposed, Widget active, WidgetContext context, WidgetGrid grid) {
		guides.clear();
		int xTargetCount = collectXTargets(active, context, grid, proposed.width());
		int yTargetCount = collectYTargets(active, context, grid, proposed.height());

		int deltaX = findAxisSnap(
				proposed.x(),
				proposed.right(),
				proposed.x() + proposed.width() / 2,
				xTargets,
				xTargetCount,
				context.screenWidth(),
				true
		);
		int deltaY = findAxisSnap(
				proposed.y(),
				proposed.bottom(),
				proposed.y() + proposed.height() / 2,
				yTargets,
				yTargetCount,
				context.screenHeight(),
				false
		);

		WidgetBounds snapped = proposed.translated(deltaX, deltaY);
		if (grid.isEnabled()) {
			int snappedWidth = grid.snapSize(snapped.width());
			int snappedHeight = grid.snapSize(snapped.height());
			snapped = snapped.withSize(snappedWidth, snappedHeight);
		}

		return snapped.clampedToViewport(context.screenWidth(), context.screenHeight());
	}

	private int collectXTargets(Widget active, WidgetContext context, WidgetGrid grid, int widgetWidth) {
		int count = 0;
		int screenWidth = context.screenWidth();

		xTargets[count++] = 0;
		xTargets[count++] = screenWidth / 2;
		xTargets[count++] = screenWidth;
		xTargets[count++] = (screenWidth - widgetWidth) / 2;
		xTargets[count++] = screenWidth - widgetWidth;

		for (Widget widget : WidgetManager.get().getWidgets()) {
			if (widget == active || !widget.isVisible() || !widget.isEnabled()) {
				continue;
			}

			WidgetBounds bounds = widget.resolveBounds(context);
			if (count + 3 > xTargets.length) {
				break;
			}

			xTargets[count++] = bounds.x();
			xTargets[count++] = bounds.right();
			xTargets[count++] = bounds.x() + bounds.width() / 2;
		}

		if (grid.isEnabled()) {
			int cellSize = grid.cellSize();
			for (int coordinate = 0; coordinate <= screenWidth && count < xTargets.length; coordinate += cellSize) {
				xTargets[count++] = coordinate;
			}
		}

		return count;
	}

	private int collectYTargets(Widget active, WidgetContext context, WidgetGrid grid, int widgetHeight) {
		int count = 0;
		int screenHeight = context.screenHeight();

		yTargets[count++] = 0;
		yTargets[count++] = screenHeight / 2;
		yTargets[count++] = screenHeight;
		yTargets[count++] = (screenHeight - widgetHeight) / 2;
		yTargets[count++] = screenHeight - widgetHeight;

		for (Widget widget : WidgetManager.get().getWidgets()) {
			if (widget == active || !widget.isVisible() || !widget.isEnabled()) {
				continue;
			}

			WidgetBounds bounds = widget.resolveBounds(context);
			if (count + 3 > yTargets.length) {
				break;
			}

			yTargets[count++] = bounds.y();
			yTargets[count++] = bounds.bottom();
			yTargets[count++] = bounds.y() + bounds.height() / 2;
		}

		if (grid.isEnabled()) {
			int cellSize = grid.cellSize();
			for (int coordinate = 0; coordinate <= screenHeight && count < yTargets.length; coordinate += cellSize) {
				yTargets[count++] = coordinate;
			}
		}

		return count;
	}

	private int findAxisSnap(
			int leadingEdge,
			int trailingEdge,
			int centerEdge,
			int[] targets,
			int targetCount,
			int screenSize,
			boolean vertical
	) {
		int bestDelta = 0;
		int bestDistance = SNAP_THRESHOLD + 1;
		SnapGuide.SnapGuideType bestType = SnapGuide.SnapGuideType.WIDGET_EDGE;
		int bestTarget = 0;

		for (int index = 0; index < targetCount; index++) {
			int target = targets[index];
			SnapGuide.SnapGuideType type = classifyTarget(target, screenSize, vertical);

			int delta = snapDelta(leadingEdge, target);
			int distance = Math.abs(delta);
			if (distance <= SNAP_THRESHOLD && distance < bestDistance) {
				bestDistance = distance;
				bestDelta = delta;
				bestTarget = target;
				bestType = type;
			}

			delta = snapDelta(trailingEdge, target);
			distance = Math.abs(delta);
			if (distance <= SNAP_THRESHOLD && distance < bestDistance) {
				bestDistance = distance;
				bestDelta = delta;
				bestTarget = target;
				bestType = type;
			}

			delta = snapDelta(centerEdge, target);
			distance = Math.abs(delta);
			if (distance <= SNAP_THRESHOLD && distance < bestDistance) {
				bestDistance = distance;
				bestDelta = delta;
				bestTarget = target;
				bestType = type;
			}
		}

		if (bestDistance <= SNAP_THRESHOLD) {
			int guideStart = vertical ? 0 : Math.min(leadingEdge, trailingEdge);
			int guideEnd = vertical ? screenSize : Math.max(trailingEdge, centerEdge);
			if (vertical) {
				guides.addVertical(bestTarget, guideStart, guideEnd, bestType);
			} else {
				guides.addHorizontal(bestTarget, guideStart, guideEnd, bestType);
			}
			return bestDelta;
		}

		return 0;
	}

	private static SnapGuide.SnapGuideType classifyTarget(int target, int screenSize, boolean vertical) {
		if (target == 0 || target == screenSize) {
			return SnapGuide.SnapGuideType.SCREEN_EDGE;
		}
		if (target == screenSize / 2) {
			return SnapGuide.SnapGuideType.SCREEN_CENTER;
		}
		return SnapGuide.SnapGuideType.WIDGET_EDGE;
	}

	private static int snapDelta(int edge, int target) {
		return target - edge;
	}
}
