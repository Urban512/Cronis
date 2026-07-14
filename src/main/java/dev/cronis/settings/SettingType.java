package dev.cronis.settings;

/**
 * Identifies the persisted type of a setting value.
 * <p>
 * Used by future serializers to select the correct codec without reflection.
 */
public enum SettingType {
	BOOLEAN,
	INT,
	FLOAT,
	DOUBLE,
	ENUM,
	STRING,
	COLOR,
	KEYBIND
}
