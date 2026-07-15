package dev.cronis.gui.settings;

import dev.cronis.editor.inspector.InspectorNumericField;
import dev.cronis.editor.inspector.InspectorRow;
import dev.cronis.gui.component.GuiComponent;
import dev.cronis.gui.component.GuiLabel;
import dev.cronis.gui.component.GuiSlider;
import dev.cronis.gui.component.GuiToggle;
import dev.cronis.gui.render.CardRenderer;
import dev.cronis.gui.theme.DesignTokens;
import dev.cronis.gui.theme.GuiMetrics;
import dev.cronis.gui.theme.ThemeManager;
import dev.cronis.widget.Widget;
import dev.cronis.widget.WidgetManager;
import dev.cronis.widget.WidgetPosition;
import dev.cronis.widget.WidgetSize;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Layout controls for a single HUD widget under /cronis → HUD → General.
 */
public final class HudWidgetLayoutPanel extends GuiComponent {
	private final GuiLabel titleLabel;
	private final InspectorRow visibilityRow = new InspectorRow("Visible", new GuiToggle(true));
	private final InspectorRow scaleRow = new InspectorRow(
			"Scale",
			new GuiSlider(Widget.SCALE_MIN, Widget.SCALE_MAX, Widget.SCALE_DEFAULT, Widget.SCALE_STEP)
	);
	private final InspectorRow positionXRow = new InspectorRow(
			"Position X",
			new InspectorNumericField(InspectorNumericField.Mode.SIGNED_FLOAT)
	);
	private final InspectorRow positionYRow = new InspectorRow(
			"Position Y",
			new InspectorNumericField(InspectorNumericField.Mode.SIGNED_FLOAT)
	);
	private final InspectorRow widthRow = new InspectorRow(
			"Width",
			new InspectorNumericField(InspectorNumericField.Mode.POSITIVE_INT)
	);
	private final InspectorRow heightRow = new InspectorRow(
			"Height",
			new InspectorNumericField(InspectorNumericField.Mode.POSITIVE_INT)
	);
	private final SettingGroupPanel settingsPanel = new SettingGroupPanel();

	private final GuiToggle visibilityToggle = (GuiToggle) visibilityRow.control();
	private final GuiSlider scaleSlider = (GuiSlider) scaleRow.control();
	private final InspectorNumericField positionXField = (InspectorNumericField) positionXRow.control();
	private final InspectorNumericField positionYField = (InspectorNumericField) positionYRow.control();
	private final InspectorNumericField widthField = (InspectorNumericField) widthRow.control();
	private final InspectorNumericField heightField = (InspectorNumericField) heightRow.control();

	private Widget boundWidget;
	private boolean syncing;

	public HudWidgetLayoutPanel(String title) {
		this.titleLabel = GuiLabel.heading(title);
		addChild(titleLabel);
		addChild(visibilityRow);
		addChild(scaleRow);
		addChild(positionXRow);
		addChild(positionYRow);
		addChild(widthRow);
		addChild(heightRow);
		addChild(settingsPanel);
		wireHandlers();
	}

	public void bind(Widget widget) {
		boundWidget = widget;
		if (widget == null) {
			settingsPanel.bind(null);
			widthRow.setVisible(false);
			heightRow.setVisible(false);
			return;
		}

		titleLabel.setText(widget.getDisplayName());
		widthRow.setVisible(widget.isManuallyResizable());
		heightRow.setVisible(widget.isManuallyResizable());
		settingsPanel.bind(widget.getSettings());
		syncFromWidget();
	}

	@Override
	public int getPreferredHeight(int availableWidth) {
		int contentWidth = Math.max(0, availableWidth - DesignTokens.CARD_PADDING.horizontal());
		int total = DesignTokens.CARD_PADDING.vertical();
		boolean first = true;
		for (GuiComponent child : getChildren()) {
			if (!child.isVisible()) {
				continue;
			}
			if (!first) {
				total += GuiMetrics.ROW_GAP;
			}
			total += child.getPreferredHeight(contentWidth);
			first = false;
		}
		return total;
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		layoutChildren();
		super.update(delta, mouseX, mouseY);
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		layoutChildren();
		var theme = ThemeManager.get();
		CardRenderer.draw(
				context,
				x,
				y,
				width,
				height,
				CardRenderer.Style.card(),
				theme.cardBackground(),
				theme.cardBorder()
		);
	}

	@Override
	protected void renderChildren(GuiGraphicsExtractor context, Font font) {
		for (GuiComponent child : getChildren()) {
			if (child.isVisible()) {
				child.render(context, font);
			}
		}
	}

	private void layoutChildren() {
		int contentX = x + DesignTokens.CARD_PADDING.left();
		int contentWidth = Math.max(0, width - DesignTokens.CARD_PADDING.horizontal());
		int currentY = y + DesignTokens.CARD_PADDING.top();
		boolean first = true;

		for (GuiComponent child : getChildren()) {
			if (!child.isVisible()) {
				continue;
			}
			if (!first) {
				currentY += GuiMetrics.ROW_GAP;
			}
			int childHeight = child.getPreferredHeight(contentWidth);
			child.setBounds(contentX, currentY, contentWidth, childHeight);
			currentY += childHeight;
			first = false;
		}

		height = Math.max(0, currentY + DesignTokens.CARD_PADDING.bottom() - y);
	}

	private void syncFromWidget() {
		if (boundWidget == null || syncing) {
			return;
		}

		syncing = true;
		try {
			visibilityToggle.setOnChange(null);
			scaleSlider.setOnChange(null);
			scaleSlider.setOnRelease(null);
			positionXField.setOnCommit(null);
			positionYField.setOnCommit(null);
			widthField.setOnCommit(null);
			heightField.setOnCommit(null);

			visibilityToggle.setOn(boundWidget.isVisible());
			scaleSlider.setValueSilent(boundWidget.getScale());
			widthRow.setVisible(boundWidget.isManuallyResizable());
			heightRow.setVisible(boundWidget.isManuallyResizable());

			if (!positionXField.isEditing()) {
				positionXField.applyCommittedValue(formatNumber(boundWidget.getPosition().offsetX()));
			}
			if (!positionYField.isEditing()) {
				positionYField.applyCommittedValue(formatNumber(boundWidget.getPosition().offsetY()));
			}
			if (!widthField.isEditing()) {
				widthField.applyCommittedValue(Integer.toString(boundWidget.getWidth()));
			}
			if (!heightField.isEditing()) {
				heightField.applyCommittedValue(Integer.toString(boundWidget.getHeight()));
			}

			wireHandlers();
		} finally {
			syncing = false;
		}
	}

	private void wireHandlers() {
		visibilityToggle.setOnChange(value -> {
			if (!syncing && boundWidget != null) {
				boundWidget.setVisible(value);
				WidgetManager.get().notifyLayoutChanged(boundWidget);
			}
		});

		scaleSlider.setOnChange(value -> {
			if (!syncing && boundWidget != null) {
				boundWidget.setScale(value);
				boundWidget.applyPreferredSize();
			}
		});
		scaleSlider.setOnRelease(value -> {
			if (!syncing && boundWidget != null) {
				syncFromWidget();
				WidgetManager.get().notifyLayoutChanged(boundWidget);
			}
		});

		positionXField.setOnCommit(value -> commitPosition(value, true));
		positionYField.setOnCommit(value -> commitPosition(value, false));
		widthField.setOnCommit(this::commitWidth);
		heightField.setOnCommit(this::commitHeight);
	}

	private void commitPosition(String value, boolean xAxis) {
		if (syncing || boundWidget == null) {
			return;
		}

		try {
			float parsed = Float.parseFloat(value.trim());
			WidgetPosition position = boundWidget.getPosition();
			boundWidget.setPosition(xAxis
					? position.withOffsets(parsed, position.offsetY())
					: position.withOffsets(position.offsetX(), parsed));
			WidgetManager.get().notifyLayoutChanged(boundWidget);
		} catch (NumberFormatException ignored) {
		}
	}

	private void commitWidth(String value) {
		if (syncing || boundWidget == null || !boundWidget.isManuallyResizable()) {
			return;
		}

		try {
			int parsedWidth = Integer.parseInt(value.trim());
			WidgetSize minimum = boundWidget.getScaledMinimumSize();
			boundWidget.setSize(Math.max(minimum.width(), parsedWidth), boundWidget.getHeight());
			WidgetManager.get().notifyLayoutChanged(boundWidget);
		} catch (NumberFormatException ignored) {
		}
	}

	private void commitHeight(String value) {
		if (syncing || boundWidget == null || !boundWidget.isManuallyResizable()) {
			return;
		}

		try {
			int parsedHeight = Integer.parseInt(value.trim());
			WidgetSize minimum = boundWidget.getScaledMinimumSize();
			boundWidget.setSize(boundWidget.getWidth(), Math.max(minimum.height(), parsedHeight));
			WidgetManager.get().notifyLayoutChanged(boundWidget);
		} catch (NumberFormatException ignored) {
		}
	}

	private static String formatNumber(float value) {
		if (Math.rint(value) == value) {
			return Integer.toString((int) value);
		}
		return Float.toString(value);
	}
}
