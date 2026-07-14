package dev.cronis.gui.settings;

import dev.cronis.editor.inspector.InspectorRow;
import dev.cronis.editor.inspector.SettingControlFactory;
import dev.cronis.gui.component.GuiCard;
import dev.cronis.gui.component.GuiComponent;
import dev.cronis.gui.component.GuiDropdown;
import dev.cronis.gui.component.GuiTextField;
import dev.cronis.gui.component.GuiToggle;
import dev.cronis.gui.layout.Spacing;
import dev.cronis.settings.BooleanSetting;
import dev.cronis.settings.DoubleSetting;
import dev.cronis.settings.EnumSetting;
import dev.cronis.settings.FloatSetting;
import dev.cronis.settings.IntSetting;
import dev.cronis.settings.Setting;
import dev.cronis.settings.SettingGroup;
import dev.cronis.settings.StringSetting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable settings panel that binds controls to a {@link SettingGroup}.
 */
public final class SettingGroupPanel extends GuiComponent {
	private final List<InspectorRow> rows = new ArrayList<>();
	private SettingGroup boundGroup;

	/**
	 * Binds the panel to the provided settings group and rebuilds its controls.
	 *
	 * @param group settings group to expose
	 */
	public void bind(SettingGroup group) {
		boundGroup = group;
		clearChildren();
		rows.clear();

		if (group == null || group.isEmpty()) {
			return;
		}

		if (!group.getDescription().isBlank()) {
			addChild(new GuiCard(group.getDisplayName(), group.getDescription()));
		}

		appendSettingsGroup(group);
		syncFromSettings();
	}

	@Override
	public int getPreferredHeight(int availableWidth) {
		int totalHeight = 0;
		boolean first = true;

		for (GuiComponent child : getChildren()) {
			if (!child.isVisible()) {
				continue;
			}

			if (!first) {
				totalHeight += Spacing.MD;
			}

			totalHeight += child.getPreferredHeight(availableWidth);
			first = false;
		}

		return totalHeight;
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		refreshRowVisibility();
		layoutChildren();
		super.update(delta, mouseX, mouseY);
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		layoutChildren();
	}

	@Override
	protected void renderChildren(GuiGraphicsExtractor context, Font font) {
		for (GuiComponent child : getChildren()) {
			child.render(context, font);
		}
	}

	private void appendSettingsGroup(SettingGroup group) {
		for (Setting<?> setting : group.visibleSettings()) {
			InspectorRow row = SettingControlFactory.create(setting);
			if (row != null) {
				rows.add(row);
				addChild(row);
			}
		}

		for (SettingGroup child : group.groups()) {
			appendSettingsGroup(child);
		}
	}

	private void syncFromSettings() {
		if (boundGroup == null) {
			return;
		}

		for (InspectorRow row : rows) {
			Setting<?> setting = findSetting(boundGroup, row.settingId());
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

	private void refreshRowVisibility() {
		if (boundGroup == null) {
			return;
		}

		for (InspectorRow row : rows) {
			Setting<?> setting = findSetting(boundGroup, row.settingId());
			if (setting == null) {
				continue;
			}

			row.setVisible(setting.isVisible());
			row.control().setEnabled(setting.isEnabled());
		}
	}

	private void layoutChildren() {
		int currentY = y;
		boolean first = true;

		for (GuiComponent child : getChildren()) {
			if (!child.isVisible()) {
				continue;
			}

			if (!first) {
				currentY += Spacing.MD;
			}

			int childHeight = child.getPreferredHeight(width);
			child.setBounds(x, currentY, width, childHeight);
			currentY += childHeight;
			first = false;
		}

		height = Math.max(0, currentY - y);
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
