package dev.cronis.gui.screen;



import dev.cronis.editor.WidgetDragController;

import dev.cronis.editor.WidgetEditorContext;

import dev.cronis.editor.WidgetGrid;

import dev.cronis.editor.WidgetOverlayRenderer;

import dev.cronis.editor.WidgetResizeController;

import dev.cronis.editor.WidgetSelectionManager;

import dev.cronis.editor.WidgetSnapEngine;

import dev.cronis.editor.contextmenu.WidgetContextMenuHost;

import dev.cronis.editor.contextmenu.WidgetContextMenuOverlay;

import dev.cronis.editor.contextmenu.WidgetEditorActions;

import dev.cronis.editor.inspector.WidgetInspectorPanel;

import dev.cronis.gui.overlay.GuiOverlayLayer;

import dev.cronis.gui.render.ColorUtil;

import dev.cronis.gui.theme.ThemeManager;

import dev.cronis.gui.util.GuiBounds;

import dev.cronis.widget.Widget;

import dev.cronis.widget.WidgetBounds;

import dev.cronis.widget.WidgetContext;

import dev.cronis.widget.WidgetManager;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.minecraft.client.input.CharacterEvent;

import net.minecraft.client.input.KeyEvent;

import net.minecraft.client.input.MouseButtonEvent;

import org.lwjgl.glfw.GLFW;



/**

 * Full-screen HUD editor for positioning and sizing Cronis widgets.

 */

public class HudEditorScreen extends GuiScreen implements WidgetContextMenuHost {

	private static final int SAFE_AREA_PADDING = 8;



	private final WidgetManager widgetManager = WidgetManager.get();

	private final WidgetSelectionManager selectionManager = new WidgetSelectionManager();

	private final WidgetDragController dragController = new WidgetDragController();

	private final WidgetResizeController resizeController = new WidgetResizeController();

	private final WidgetSnapEngine snapEngine = new WidgetSnapEngine();

	private final WidgetGrid grid = new WidgetGrid();

	private final WidgetOverlayRenderer overlayRenderer = new WidgetOverlayRenderer();

	private final WidgetInspectorPanel inspector = new WidgetInspectorPanel();

	private final WidgetContextMenuOverlay contextMenu = new WidgetContextMenuOverlay();



	private EditorFrameState frameState;

	private WidgetContext lastWidgetContext;

	private boolean showGrid;

	private boolean showSafeArea = true;

	private boolean inspectorOpen;



	public HudEditorScreen() {

		WidgetEditorActions.registerDefaults();

		attachFocusManager(inspector);

		inspector.setOnClose(this::closeInspector);

		inspector.setOnLayoutChanged(this::refreshEditorLayout);

	}



	@Override

	public boolean isPauseScreen() {

		return false;

	}



	@Override

	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {

		super.extractRenderState(context, mouseX, mouseY, delta);



		WidgetContext widgetContext = WidgetContext.create(context, font, delta);

		lastWidgetContext = widgetContext;



		WidgetEditorContext editorContext = new WidgetEditorContext(

				widgetContext,

				mouseX,

				mouseY,

				grid,

				showGrid,

				showSafeArea,

				SAFE_AREA_PADDING

		);



		if (dragController.isActive()) {

			dragController.update(editorContext, snapEngine);

		} else if (resizeController.isActive()) {

			resizeController.update(editorContext, snapEngine);

		}



		if (inspectorOpen) {

			inspector.bind(selectionManager.getSelectedOrNull());

			if (!dragController.isActive() && !resizeController.isActive()) {

				inspector.syncFromWidget();

			}

		}



		frameState = new EditorFrameState(context, widgetContext, editorContext, delta, mouseX, mouseY);

		renderEditorContent(frameState);

	}



	@Override

	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {

		int mouseX = (int) event.x();

		int mouseY = (int) event.y();



		if (dispatchOverlayMouseClicked(event.x(), event.y(), event.button())) {

			return true;

		}



		if (inspectorOpen && isInspectorMouse(mouseX, mouseY)) {

			return inspector.mouseClicked(event.x(), event.y(), event.button()) || super.mouseClicked(event, doubleClick);

		}



		if (inspectorOpen && !isInspectorMouse(mouseX, mouseY)) {

			closeInspector();

		}



		if (lastWidgetContext == null) {

			return super.mouseClicked(event, doubleClick);

		}



		if (event.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {

			closeContextMenu();

			selectionManager.hitTest(lastWidgetContext, mouseX, mouseY).ifPresent(widget -> {

				selectionManager.select(widget);

				openContextMenu(widget, mouseX, mouseY);

			});

			return true;

		}



		if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {

			return super.mouseClicked(event, doubleClick);

		}



		closeContextMenu();



		Widget selected = selectionManager.getSelectedOrNull();

		if (selected != null) {

			WidgetBounds selectedBounds = selected.resolveBounds(lastWidgetContext);

			WidgetResizeController.Handle handle = resizeController.hitTest(selectedBounds, mouseX, mouseY);

			if (handle != null) {

				resizeController.begin(selected, handle, mouseX, mouseY, selectedBounds);

				return true;

			}

		}



		selectionManager.hitTest(lastWidgetContext, mouseX, mouseY).ifPresentOrElse(

				widget -> {

					selectionManager.select(widget);

					WidgetBounds bounds = widget.resolveBounds(lastWidgetContext);

					dragController.begin(widget, mouseX, mouseY, bounds);

				},

				() -> {

					selectionManager.clear();

					closeInspector();

				}

		);



		return true;

	}



	@Override

	public boolean mouseReleased(MouseButtonEvent event) {

		if (dispatchOverlayMouseReleased(event.x(), event.y(), event.button())) {

			return true;

		}



		if (inspectorOpen && isInspectorMouse((int) event.x(), (int) event.y())) {

			return inspector.mouseReleased(event.x(), event.y(), event.button()) || super.mouseReleased(event);

		}



		if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {

			Widget layoutWidget = dragController.isActive()

					? dragController.widget()

					: resizeController.isActive() ? resizeController.widget() : null;



			dragController.end();

			resizeController.end();



			if (layoutWidget != null) {

				widgetManager.notifyLayoutChanged(layoutWidget);

				refreshEditorLayout();

			}



			if (inspectorOpen) {

				inspector.syncFromWidget();

			}

			return true;

		}



		return super.mouseReleased(event);

	}



	@Override

	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {

		if (dispatchOverlayMouseScrolled(mouseX, mouseY, scrollX, scrollY)) {

			return true;

		}



		if (inspectorOpen && isInspectorMouse((int) mouseX, (int) mouseY)) {

			return inspector.mouseScrolled(mouseX, mouseY, scrollX, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

		}



		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

	}



	@Override

	public boolean keyPressed(KeyEvent event) {

		if (dispatchOverlayKeyPressed(event)) {

			return true;

		}



		if (focusManager.dispatchKeyPressed(event)) {

			return true;

		}



		if (event.key() == GLFW.GLFW_KEY_ESCAPE) {

			if (overlays.hasActive(GuiOverlayLayer.MODAL)) {

				overlays.closeLayer(GuiOverlayLayer.MODAL);

				return true;

			}

			if (overlays.hasActive(GuiOverlayLayer.POPUP)) {

				overlays.closeLayer(GuiOverlayLayer.POPUP);

				return true;

			}

			if (inspectorOpen) {

				closeInspector();

				return true;

			}

			onClose();

			return true;

		}



		if (event.key() == GLFW.GLFW_KEY_G) {

			showGrid = !showGrid;

			grid.setEnabled(showGrid);

			return true;

		}



		return super.keyPressed(event);

	}



	@Override

	public boolean charTyped(CharacterEvent event) {

		if (focusManager.dispatchCharTyped(event)) {

			return true;

		}



		return super.charTyped(event);

	}



	@Override

	public WidgetContext viewportContext() {

		return lastWidgetContext;

	}



	@Override

	public int screenWidth() {

		return width;

	}



	@Override

	public int screenHeight() {

		return height;

	}



	@Override

	public void openInspector(Widget widget) {

		if (widget == null) {

			return;

		}



		selectionManager.select(widget);

		inspectorOpen = true;

		inspector.bind(widget);

	}



	@Override

	public void refreshEditorLayout() {

		if (frameState == null || lastWidgetContext == null) {

			return;

		}



		renderWidgetLayer(frameState);

	}



	@Override

	public void onContextMenuClosed() {

	}



	private void openContextMenu(Widget widget, int mouseX, int mouseY) {

		if (lastWidgetContext == null) {

			return;

		}



		overlays.closeLayer(GuiOverlayLayer.MODAL);

		contextMenu.open(widget, mouseX, mouseY, this, overlays);

		if (contextMenu.isActive()) {

			overlays.show(contextMenu);

		}

	}



	private void closeContextMenu() {

		contextMenu.close();

	}



	private void closeInspector() {

		if (!inspectorOpen) {

			return;

		}



		inspectorOpen = false;

		overlays.closeLayer(GuiOverlayLayer.POPUP);

		focusManager.clearFocus();

	}



	private void renderEditorContent(EditorFrameState frame) {

		renderScrim(frame.graphics());

		renderWidgetLayer(frame);



		if (inspectorOpen) {

			renderInspector(frame.graphics(), frame.delta(), frame.mouseX(), frame.mouseY());

		}



		renderOverlays(frame.graphics(), font, frame.delta(), frame.mouseX(), frame.mouseY());

	}



	private void renderWidgetLayer(EditorFrameState frame) {

		widgetManager.update(frame.widgetContext());

		widgetManager.render(frame.widgetContext());

		overlayRenderer.render(frame.graphics(), frame.editorContext(), selectionManager, snapEngine);

	}



	private void renderInspector(GuiGraphicsExtractor context, float delta, int mouseX, int mouseY) {

		inspector.layoutFloating(width, height);

		inspector.update(delta, mouseX, mouseY);

		inspector.render(context, font);

	}



	private void renderScrim(GuiGraphicsExtractor context) {

		int scrim = ColorUtil.withAlpha(ThemeManager.get().modalOverlay(), 0.45f);

		context.fill(0, 0, width, height, scrim);

	}



	private boolean isInspectorMouse(int mouseX, int mouseY) {

		if (!inspectorOpen) {

			return false;

		}



		GuiBounds bounds = inspector.getBounds();

		return mouseX >= bounds.x()

				&& mouseX < bounds.x() + bounds.width()

				&& mouseY >= bounds.y()

				&& mouseY < bounds.y() + bounds.height();

	}



	private record EditorFrameState(

			GuiGraphicsExtractor graphics,

			WidgetContext widgetContext,

			WidgetEditorContext editorContext,

			float delta,

			int mouseX,

			int mouseY

	) {

	}

}


