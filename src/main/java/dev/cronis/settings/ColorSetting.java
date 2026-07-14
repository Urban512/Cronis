package dev.cronis.settings;

/**
 * Packed ARGB color setting.
 * <p>
 * Colors use the same 32-bit ARGB format as the Cronis rendering engine.
 */
public final class ColorSetting extends Setting<Integer> {
	/**
	 * Creates a color setting.
	 *
	 * @param id           unique identifier
	 * @param displayName  user-facing label
	 * @param description  explanatory text
	 * @param defaultValue default packed ARGB color
	 */
	public ColorSetting(String id, String displayName, String description, int defaultValue) {
		super(id, displayName, description, defaultValue);
	}

	@Override
	public SettingType getType() {
		return SettingType.COLOR;
	}

	@Override
	protected ValidationResult validateValue(Integer value) {
		return value == null ? ValidationResult.error("Value must not be null") : ValidationResult.ok();
	}

	@Override
	public String serializeValue(Integer value) {
		return String.format("%08X", value);
	}

	@Override
	public Integer deserializeValue(String serialized) {
		return (int) Long.parseLong(serialized, 16);
	}
}
