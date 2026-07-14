package dev.cronis.settings;

import java.util.Objects;

/**
 * Enum setting backed by a fixed {@link Enum} type.
 *
 * @param <E> enum type
 */
public final class EnumSetting<E extends Enum<E>> extends Setting<E> {
	private final Class<E> enumType;

	/**
	 * Creates an enum setting.
	 *
	 * @param id           unique identifier
	 * @param displayName  user-facing label
	 * @param description  explanatory text
	 * @param defaultValue default value
	 */
	public EnumSetting(String id, String displayName, String description, E defaultValue) {
		super(id, displayName, description, defaultValue);
		this.enumType = defaultValue.getDeclaringClass();
	}

	/**
	 * Returns the enum type for this setting.
	 *
	 * @return enum class
	 */
	public Class<E> getEnumType() {
		return enumType;
	}

	/**
	 * Returns all allowed enum constants in declaration order.
	 *
	 * @return enum constants
	 */
	public E[] getValues() {
		return enumType.getEnumConstants();
	}

	@Override
	public SettingType getType() {
		return SettingType.ENUM;
	}

	@Override
	protected ValidationResult validateValue(E value) {
		if (value == null) {
			return ValidationResult.error("Value must not be null");
		}
		if (value.getDeclaringClass() != enumType) {
			return ValidationResult.error("Value must be a " + enumType.getSimpleName());
		}
		return ValidationResult.ok();
	}

	@Override
	public String serializeValue(E value) {
		return value.name();
	}

	@Override
	public E deserializeValue(String serialized) {
		Objects.requireNonNull(serialized, "serialized");
		return Enum.valueOf(enumType, serialized);
	}
}
