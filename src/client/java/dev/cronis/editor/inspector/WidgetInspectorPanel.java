package dev.cronis.editor.inspector;

import dev.cronis.gui.component.GuiComponent;
import dev.cronis.gui.component.GuiDivider;
import dev.cronis.gui.component.GuiDropdown;
import dev.cronis.gui.component.GuiLabel;
import dev.cronis.gui.component.GuiScrollPanel;
import dev.cronis.gui.component.GuiTextField;
import dev.cronis.gui.component.GuiToggle;
import dev.cronis.gui.layout.Padding;
import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.layout.VerticalLayout;
import dev.cronis.gui.render.RoundedRenderer;
import dev.cronis.gui.render.ShadowRenderer;
import dev.cronis.gui.theme.ThemeManager;
import dev.cronis.settings.BooleanSetting;
import dev.cronis.settings.DoubleSetting;
import dev.cronis.settings.EnumSetting;
import dev.cronis.settings.FloatSetting;
import dev.cronis.settings.IntSetting;
import dev.cronis.settings.Setting;
import dev.cronis.settings.SettingGroup;
import dev.cronis.settings.StringSetting;
import dev.cronis.widget.Widget;
import dev.cronis.widget.WidgetManager;
import dev.cronis.widget.WidgetPosition;
import dev.cronis.widget.WidgetSize;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

/**
 * Floating inspector panel for editing the currently selected HUD widget.
 */
public final class WidgetInspectorPanel extends GuiComponent {
	public static final int WIDTH = 280;
	private static final int PANEL_MARGIN = Spacing.LG;
	private static final int CORNER_RADIUS = 14;

	private final GuiScrollPanel scrollPanel = new GuiScrollPanel(new VerticalLayout(Spacing.MD));
	private final InspectorHeader header = new InspectorHeader();
	private final GuiDivider headerDivider = new GuiDivider();
	private final InspectorRow visibilityRow = new InspectorRow("Visible", new GuiToggle(true));
	private final InspectorRow positionXRow = new InspectorRow("Position X", new InspectorNumericField(InspectorNumericField.Mode.SIGNED_FLOAT));
	private final InspectorRow positionYRow = new InspectorRow("Position Y", new InspectorNumericField(InspectorNumericField.Mode.SIGNED_FLOAT));
	private final InspectorRow widthRow = new InspectorRow("Width", new InspectorNumericField(InspectorNumericField.Mode.POSITIVE_INT));
	private final InspectorRow heightRow = new InspectorRow("Height", new InspectorNumericField(InspectorNumericField.Mode.POSITIVE_INT));
	private final GuiDivider settingsDivider = new GuiDivider();
	private final InspectorSection settingsSection = new InspectorSection();
	private final List<GuiComponent> propertyRows = new ArrayList<>();

	private final GuiToggle visibilityToggle = (GuiToggle) visibilityRow.control();
	private final InspectorNumericField positionXField = (InspectorNumericField) positionXRow.control();
	private final InspectorNumericField positionYField = (InspectorNumericField) positionYRow.control();
	private final InspectorNumericField widthField = (InspectorNumericField) widthRow.control();
	private final InspectorNumericField heightField = (InspectorNumericField) heightRow.control();

	private Widget boundWidget;
	private boolean syncing;
	private Runnable onClose;
	private Runnable onLayoutChanged;

	public WidgetInspectorPanel() {
		scrollPanel.setContentPadding(Padding.all(Spacing.LG));
		addChild(scrollPanel);

		propertyRows.add(headerDivider);
		propertyRows.add(visibilityRow);
		propertyRows.add(positionXRow);
		propertyRows.add(positionYRow);
		propertyRows.add(widthRow);
		propertyRows.add(heightRow);

		scrollPanel.addChild(header);
		for (GuiComponent row : propertyRows) {
			scrollPanel.addChild(row);
		}

		settingsDivider.setVisible(false);
		settingsSection.setVisible(false);
		scrollPanel.addChild(settingsDivider);
		scrollPanel.addChild(settingsSection);

		header.setOnClose(() -> {
			if (onClose != null) {
				onClose.run();
			}
		});
		wirePropertyHandlers();
	}

	public void setOnClose(Runnable onClose) {
		this.onClose = onClose;
	}

	public void setOnLayoutChanged(Runnable onLayoutChanged) {
		this.onLayoutChanged = onLayoutChanged;
	}

	/**
	 * Binds the inspector to the provided widget selection.
	 *
	 * @param widget selected widget
	 */
	public void bind(Widget widget) {
		if (boundWidget == widget) {
			return;
		}

		boundWidget = widget;
		if (widget == null) {
			settingsSection.clearChildren();
			settingsDivider.setVisible(false);
			settingsSection.setVisible(false);
			return;
		}

		header.setTitle(widget.getDisplayName());
		rebuildSettingsSection();
		syncFromWidget();
	}

	/**
	 * Refreshes inspector controls from the bound widget state.
	 */
	public void syncFromWidget() {
		if (boundWidget == null || syncing) {
			return;
		}

		syncing = true;
		try {
			clearPropertyHandlers();
			visibilityToggle.setOn(boundWidget.isVisible());
			syncLayoutFieldsFromWidget();
			syncSettingControls(boundWidget.getSettings());
			wirePropertyHandlers();
		} finally {
			syncing = false;
		}
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		refreshSettingsVisibility();
		scrollPanel.setBounds(x, y, width, height);
		scrollPanel.update(delta, mouseX, mouseY);
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		var theme = ThemeManager.get();
		ShadowRenderer.draw(context, x, y, width, height, CORNER_RADIUS, 8, 0.28f, theme.cardShadow());
		RoundedRenderer.fill(context, x, y, width, height, CORNER_RADIUS, theme.cardBackground());
		RoundedRenderer.outline(context, x, y, width, height, CORNER_RADIUS, 1, theme.cardBorder());
	}

	@Override
	protected void renderChildren(GuiGraphicsExtractor context, Font font) {
		scrollPanel.render(context, font);
	}

	/**
	 * Lays out the floating panel against the provided screen bounds.
	 *
	 * @param screenWidth  screen width in GUI pixels
	 * @param screenHeight screen height in GUI pixels
	 */
	public void layoutFloating(int screenWidth, int screenHeight) {
		int panelHeight = Math.max(0, screenHeight - PANEL_MARGIN * 2);
		setBounds(screenWidth - WIDTH - PANEL_MARGIN, PANEL_MARGIN, WIDTH, panelHeight);
	}

	private void syncLayoutFieldsFromWidget() {
		if (boundWidget == null) {
			return;
		}

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
	}

	private void clearPropertyHandlers() {
		visibilityToggle.setOnChange(null);
		positionXField.setOnCommit(null);
		positionYField.setOnCommit(null);
		widthField.setOnCommit(null);
		heightField.setOnCommit(null);
	}

	private void wirePropertyHandlers() {
		visibilityToggle.setOnChange(value -> {
			if (!syncing && boundWidget != null) {
				boundWidget.setVisible(value);
				publishLayoutChanged();
			}
		});

		positionXField.setOnCommit(value -> commitPosition(value, true));
		positionYField.setOnCommit(value -> commitPosition(value, false));
		widthField.setOnCommit(this::commitWidth);
		heightField.setOnCommit(this::commitHeight);
	}

	private void publishLayoutChanged() {
		if (boundWidget == null) {
			return;
		}

		WidgetManager.get().notifyLayoutChanged(boundWidget);
		if (onLayoutChanged != null) {
			onLayoutChanged.run();
		}
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
			publishLayoutChanged();
		} catch (NumberFormatException ignored) {
		}
	}

	private void commitWidth(String value) {
		if (syncing || boundWidget == null) {
			return;
		}

		try {
			int parsedWidth = Integer.parseInt(value.trim());
			WidgetSize minimum = boundWidget.getMinimumSize();
			boundWidget.setSize(Math.max(minimum.width(), parsedWidth), boundWidget.getHeight());
			publishLayoutChanged();
		} catch (NumberFormatException ignored) {
		}
	}

	private void commitHeight(String value) {
		if (syncing || boundWidget == null) {
			return;
		}

		try {
			int parsedHeight = Integer.parseInt(value.trim());
			WidgetSize minimum = boundWidget.getMinimumSize();
			boundWidget.setSize(boundWidget.getWidth(), Math.max(minimum.height(), parsedHeight));
			publishLayoutChanged();
		} catch (NumberFormatException ignored) {
		}
	}

	private void refreshSettingsVisibility() {
		if (boundWidget == null) {
			return;
		}

		for (GuiComponent child : settingsSection.getChildren()) {
			if (!(child instanceof InspectorRow row)) {
				continue;
			}

			Setting<?> setting = findSetting(boundWidget.getSettings(), row.settingId());
			if (setting == null) {
				continue;
			}

			row.setVisible(setting.isVisible());
			row.control().setEnabled(setting.isEnabled());
		}
	}

	private void rebuildSettingsSection() {
		settingsSection.clearChildren();
		if (boundWidget == null) {
			settingsDivider.setVisible(false);
			settingsSection.setVisible(false);
			return;
		}

		appendSettingsGroup(boundWidget.getSettings());
		boolean hasSettings = !settingsSection.getChildren().isEmpty();
		settingsDivider.setVisible(hasSettings);
		settingsSection.setVisible(hasSettings);
	}

	private void appendSettingsGroup(SettingGroup group) {
		for (Setting<?> setting : group.visibleSettings()) {
			InspectorRow row = SettingControlFactory.create(setting);
			if (row != null) {
				settingsSection.addChild(row);
			}
		}

		for (SettingGroup child : group.groups()) {
			appendSettingsGroup(child);
		}
	}

	private void syncSettingControls(SettingGroup group) {
		for (GuiComponent child : settingsSection.getChildren()) {
			if (!(child instanceof InspectorRow row)) {
				continue;
			}

			Setting<?> setting = findSetting(group, row.settingId());
			if (setting == null) {
				continue;
			}

			syncSettingControl(setting, row.control());
		}
	}

	private static Setting<?> findSetting(SettingGroup group, String settingId) {
		if (settingId == null) {
			return null;
		}

		for (Setting<?> setting : group.visibleSettings()) {
			if (settingId.equals(setting.getId())) {
				return setting;
			}
		}

		for (SettingGroup child : group.groups()) {
			Setting<?> nested = findSetting(child, settingId);
			if (nested != null) {
				return nested;
			}
		}

		return null;
	}

	private static void syncSettingControl(Setting<?> setting, GuiComponent control) {
		switch (setting) {
			case BooleanSetting booleanSetting when control instanceof GuiToggle toggle ->
					toggle.setOn(booleanSetting.getValue());
			case IntSetting intSetting when control instanceof GuiTextField field ->
					field.setText(Integer.toString(intSetting.getValue()));
			case FloatSetting floatSetting when control instanceof GuiTextField field ->
					field.setText(formatNumber(floatSetting.getValue()));
			case DoubleSetting doubleSetting when control instanceof GuiTextField field ->
					field.setText(formatNumber(doubleSetting.getValue()));
			case StringSetting stringSetting when control instanceof GuiTextField field -> {
				if (!field.isFocused()) {
					field.applyCommittedValue(stringSetting.getValue());
				}
			}
			case EnumSetting<?> enumSetting when control instanceof GuiDropdown dropdown ->
					dropdown.setSelectedIndex(enumSetting.getValue().ordinal());
			default -> {
			}
		}
	}

	private static String formatNumber(float value) {
		if (Math.rint(value) == value) {
			return Integer.toString((int) value);
		}
		return Float.toString(value);
	}

	private static String formatNumber(double value) {
		if (Math.rint(value) == value) {
			return Integer.toString((int) value);
		}
		return Double.toString(value);
	}
}
