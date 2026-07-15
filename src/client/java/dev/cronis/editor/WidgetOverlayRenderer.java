package dev.cronis.editor;

import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.theme.GuiTheme;
import dev.cronis.gui.theme.ThemeManager;
import dev.cronis.widget.Widget;
import dev.cronis.widget.WidgetBounds;
import dev.cronis.widget.WidgetContext;
import dev.cronis.widget.WidgetManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Renders HUD editor overlays such as bounds, handles, guides, and safe areas.
 */
public final class WidgetOverlayRenderer {
	private static final int OUTLINE_THICKNESS = 1;
	private static final int SELECTION_OUTLINE_THICKNESS = 2;
	private static final int GUIDE_THICKNESS = 1;

	public void render(
			GuiGraphicsExtractor graphics,
			WidgetEditorContext editorContext,
			WidgetSelectionManager selectionManager,
			WidgetSnapEngine snapEngine
	) {
		GuiTheme theme = ThemeManager.get();
		WidgetContext context = editorContext.widgetContext();

		if (editorContext.showSafeArea()) {
			renderSafeArea(graphics, editorContext, theme);
		}

		if (editorContext.showGrid() && editorContext.grid().isEnabled()) {
			renderAlignmentGrid(graphics, editorContext, theme);
		}

		renderWidgetBounds(graphics, context, selectionManager, theme);
		renderSnapGuides(graphics, editorContext, snapEngine, theme);
		renderSelection(graphics, selectionManager, context, theme);
	}

	private void renderSafeArea(GuiGraphicsExtractor graphics, WidgetEditorContext editorContext, GuiTheme theme) {
		int padding = editorContext.safeAreaPadding();
		int screenWidth = editorContext.screenWidth();
		int screenHeight = editorContext.screenHeight();
		int color = ColorUtil.withAlpha(theme.accent(), 0.12f);

		graphics.fill(0, 0, screenWidth, padding, color);
		graphics.fill(0, screenHeight - padding, screenWidth, screenHeight, color);
		graphics.fill(0, padding, padding, screenHeight - padding, color);
		graphics.fill(screenWidth - padding, padding, screenWidth, screenHeight - padding, color);
	}

	private void renderAlignmentGrid(GuiGraphicsExtractor graphics, WidgetEditorContext editorContext, GuiTheme theme) {
		int cellSize = editorContext.grid().cellSize();
		int screenWidth = editorContext.screenWidth();
		int screenHeight = editorContext.screenHeight();
		int color = ColorUtil.withAlpha(theme.cardBorder(), 0.18f);

		for (int x = 0; x <= screenWidth; x += cellSize) {
			graphics.fill(x, 0, x + 1, screenHeight, color);
		}

		for (int y = 0; y <= screenHeight; y += cellSize) {
			graphics.fill(0, y, screenWidth, y + 1, color);
		}
	}

	private void renderWidgetBounds(
			GuiGraphicsExtractor graphics,
			WidgetContext context,
			WidgetSelectionManager selectionManager,
			GuiTheme theme
	) {
		int boundsColor = ColorUtil.withAlpha(theme.cardBorder(), 0.55f);

		for (Widget widget : WidgetManager.get().getWidgets()) {
			if (!widget.isVisible() || !widget.isEnabled()) {
				continue;
			}

			if (selectionManager.isSelected(widget)) {
				continue;
			}

			WidgetBounds bounds = widget.getInteractionBounds(context);
			drawOutline(graphics, bounds, OUTLINE_THICKNESS, boundsColor);
		}
	}

	private void renderSelection(
			GuiGraphicsExtractor graphics,
			WidgetSelectionManager selectionManager,
			WidgetContext context,
			GuiTheme theme
	) {
		Widget selected = selectionManager.getSelectedOrNull();
		if (selected == null) {
			return;
		}

		WidgetBounds bounds = selected.getInteractionBounds(context);
		int selectionColor = theme.accent();
		drawOutline(graphics, bounds, SELECTION_OUTLINE_THICKNESS, selectionColor);
		renderHandles(graphics, bounds, theme);
	}

	private void renderHandles(GuiGraphicsExtractor graphics, WidgetBounds bounds, GuiTheme theme) {
		int fill = theme.controlBackground();
		int border = theme.controlBorderFocused();
		drawHandle(graphics, bounds.x(), bounds.y(), fill, border);
		drawHandle(graphics, bounds.right(), bounds.y(), fill, border);
		drawHandle(graphics, bounds.x(), bounds.bottom(), fill, border);
		drawHandle(graphics, bounds.right(), bounds.bottom(), fill, border);
	}

	private void drawHandle(GuiGraphicsExtractor graphics, int centerX, int centerY, int fill, int border) {
		int half = WidgetResizeController.HANDLE_SIZE / 2;
		int x = centerX - half;
		int y = centerY - half;
		graphics.fill(x, y, x + WidgetResizeController.HANDLE_SIZE, y + WidgetResizeController.HANDLE_SIZE, fill);
		drawOutline(
				graphics,
				new WidgetBounds(x, y, WidgetResizeController.HANDLE_SIZE, WidgetResizeController.HANDLE_SIZE),
				1,
				border
		);
	}

	private void renderSnapGuides(
			GuiGraphicsExtractor graphics,
			WidgetEditorContext editorContext,
			WidgetSnapEngine snapEngine,
			GuiTheme theme
	) {
		SnapGuideBuffer guides = snapEngine.guides();
		int color = ColorUtil.withAlpha(theme.accent(), 0.85f);
		int screenWidth = editorContext.screenWidth();
		int screenHeight = editorContext.screenHeight();

		for (int index = 0; index < guides.count(); index++) {
			SnapGuide guide = guides.get(index);
			if (guide.axis() == SnapGuide.SnapGuideAxis.VERTICAL) {
				int x = guide.position();
				int start = Math.max(0, guide.start());
				int end = Math.min(screenHeight, guide.end());
				graphics.fill(x, start, x + GUIDE_THICKNESS, end, color);
			} else {
				int y = guide.position();
				int start = Math.max(0, guide.start());
				int end = Math.min(screenWidth, guide.end());
				graphics.fill(start, y, end, y + GUIDE_THICKNESS, color);
			}
		}
	}

	private static void drawOutline(GuiGraphicsExtractor graphics, WidgetBounds bounds, int thickness, int color) {
		graphics.fill(bounds.x(), bounds.y(), bounds.right(), bounds.y() + thickness, color);
		graphics.fill(bounds.x(), bounds.bottom() - thickness, bounds.right(), bounds.bottom(), color);
		graphics.fill(bounds.x(), bounds.y(), bounds.x() + thickness, bounds.bottom(), color);
		graphics.fill(bounds.right() - thickness, bounds.y(), bounds.right(), bounds.bottom(), color);
	}
}
