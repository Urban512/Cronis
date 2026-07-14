package dev.cronis.editor.inspector;

import dev.cronis.gui.component.GuiComponent;
import dev.cronis.gui.component.GuiDropdown;
import dev.cronis.gui.component.GuiToggle;
import dev.cronis.gui.component.GuiTextField;
import dev.cronis.settings.BooleanSetting;
import dev.cronis.settings.DoubleSetting;
import dev.cronis.settings.EnumSetting;
import dev.cronis.settings.FloatSetting;
import dev.cronis.settings.IntSetting;
import dev.cronis.settings.Setting;
import dev.cronis.settings.SettingType;
import dev.cronis.settings.StringSetting;

import java.util.Locale;

/**
 * Builds generic inspector rows from {@link Setting} instances.
 */
public final class SettingControlFactory {
	private SettingControlFactory() {
	}

	/**
	 * Creates an inspector row for the provided setting, if supported.
	 *
	 * @param setting setting to expose
	 * @return inspector row, or {@code null} when unsupported
	 */
	public static InspectorRow create(Setting<?> setting) {
		return switch (setting.getType()) {
			case BOOLEAN -> createBoolean((BooleanSetting) setting);
			case INT -> createInt((IntSetting) setting);
			case FLOAT -> createFloat((FloatSetting) setting);
			case DOUBLE -> createDouble((DoubleSetting) setting);
			case STRING -> createString((StringSetting) setting);
			case ENUM -> createEnum((EnumSetting<?>) setting);
			case COLOR, KEYBIND -> null;
		};
	}

	private static InspectorRow createBoolean(BooleanSetting setting) {
		GuiToggle toggle = new GuiToggle(setting.getValue());
		toggle.setOnChange(value -> setting.trySetValue(value));
		return new InspectorRow(setting.getDisplayName(), toggle, setting.getId());
	}

	private static InspectorRow createInt(IntSetting setting) {
		GuiTextField field = new GuiTextField(Integer.toString(setting.getValue()), 16);
		field.setText(Integer.toString(setting.getValue()));
		field.setOnChange(value -> parseInt(value, setting));
		return new InspectorRow(setting.getDisplayName(), field, setting.getId());
	}

	private static InspectorRow createFloat(FloatSetting setting) {
		GuiTextField field = new GuiTextField(formatNumber(setting.getValue()), 16);
		field.setOnChange(value -> parseFloat(value, setting));
		return new InspectorRow(setting.getDisplayName(), field, setting.getId());
	}

	private static InspectorRow createDouble(DoubleSetting setting) {
		GuiTextField field = new GuiTextField(formatNumber(setting.getValue()), 16);
		field.setOnChange(value -> parseDouble(value, setting));
		return new InspectorRow(setting.getDisplayName(), field, setting.getId());
	}

	private static InspectorRow createString(StringSetting setting) {
		GuiTextField field = new GuiTextField(setting.getDefaultValue(), setting.getMaxLength());
		field.applyCommittedValue(setting.getValue());
		field.setOnEditBegin(setting::beginEditing);
		field.setOnChange(setting::updateDraft);
		field.setOnCommit(draft -> {
			setting.commitDraft(draft);
			field.applyCommittedValue(setting.getValue());
		});
		field.setOnEditCancel(() -> {
			setting.cancelEditing();
			field.applyCommittedValue(setting.getValue());
		});
		return new InspectorRow(setting.getDisplayName(), field, setting.getId());
	}

	private static InspectorRow createEnum(EnumSetting<?> setting) {
		Enum<?>[] values = setting.getValues();
		String[] labels = new String[values.length];
		for (int index = 0; index < values.length; index++) {
			labels[index] = formatEnum(values[index]);
		}

		GuiDropdown dropdown = new GuiDropdown(labels);
		dropdown.setSelectedIndex(setting.getValue().ordinal());
		dropdown.setOnChange(index -> setEnumValue(setting, values, index));
		return new InspectorRow(setting.getDisplayName(), dropdown, setting.getId());
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private static void setEnumValue(EnumSetting<?> setting, Enum<?>[] values, int index) {
		if (index >= 0 && index < values.length) {
			EnumSetting rawSetting = setting;
			rawSetting.trySetValue(values[index]);
		}
	}

	private static void parseInt(String value, IntSetting setting) {
		if (value == null || value.isBlank()) {
			return;
		}

		try {
			setting.trySetValue(Integer.parseInt(value.trim()));
		} catch (NumberFormatException ignored) {
		}
	}

	private static void parseFloat(String value, FloatSetting setting) {
		if (value == null || value.isBlank()) {
			return;
		}

		try {
			setting.trySetValue(Float.parseFloat(value.trim()));
		} catch (NumberFormatException ignored) {
		}
	}

	private static void parseDouble(String value, DoubleSetting setting) {
		if (value == null || value.isBlank()) {
			return;
		}

		try {
			setting.trySetValue(Double.parseDouble(value.trim()));
		} catch (NumberFormatException ignored) {
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

	private static String formatEnum(Enum<?> value) {
		String raw = value.name().toLowerCase(Locale.ROOT).replace('_', ' ');
		StringBuilder formatted = new StringBuilder(raw.length());
		boolean capitalizeNext = true;
		for (int index = 0; index < raw.length(); index++) {
			char character = raw.charAt(index);
			if (character == ' ') {
				capitalizeNext = true;
				formatted.append(character);
				continue;
			}

			formatted.append(capitalizeNext ? Character.toUpperCase(character) : character);
			capitalizeNext = false;
		}
		return formatted.toString();
	}
}
