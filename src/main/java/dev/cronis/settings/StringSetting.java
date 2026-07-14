package dev.cronis.settings;

/**
 * Text setting with optional maximum length.
 */
public final class StringSetting extends Setting<String> {
	private final int maxLength;
	private boolean editing;
	private String draftValue;

	/**
	 * Creates an unbounded string setting.
	 *
	 * @param id           unique identifier
	 * @param displayName  user-facing label
	 * @param description  explanatory text
	 * @param defaultValue default value
	 */
	public StringSetting(String id, String displayName, String description, String defaultValue) {
		this(id, displayName, description, defaultValue, Integer.MAX_VALUE);
	}

	/**
	 * Creates a length-limited string setting.
	 *
	 * @param id           unique identifier
	 * @param displayName  user-facing label
	 * @param description  explanatory text
	 * @param defaultValue default value
	 * @param maxLength    maximum allowed length
	 */
	public StringSetting(String id, String displayName, String description, String defaultValue, int maxLength) {
		super(id, displayName, description, defaultValue);
		if (maxLength < 0) {
			throw new IllegalArgumentException("maxLength must not be negative");
		}
		this.maxLength = maxLength;

		ValidationResult defaultValidation = validateValue(defaultValue);
		if (!defaultValidation.valid()) {
			throw new IllegalArgumentException(defaultValidation.message());
		}
	}

	/**
	 * Returns the maximum allowed string length.
	 *
	 * @return maximum length
	 */
	public int getMaxLength() {
		return maxLength;
	}

	/**
	 * Returns whether this setting currently has an in-progress edit session.
	 *
	 * @return {@code true} while a text field is actively editing
	 */
	public boolean isEditing() {
		return editing;
	}

	/**
	 * Begins a draft edit session for live preview without committing.
	 */
	public void beginEditing() {
		editing = true;
		draftValue = getValue();
	}

	/**
	 * Updates the in-progress draft value shown while editing.
	 *
	 * @param draft current field text
	 */
	public void updateDraft(String draft) {
		if (!editing) {
			beginEditing();
		}
		this.draftValue = draft == null ? "" : draft;
	}

	/**
	 * Commits the edited value, restoring the default when blank.
	 *
	 * @param draft final field text
	 * @return {@code true} when the value was accepted
	 */
	public boolean commitDraft(String draft) {
		editing = false;
		draftValue = null;
		return trySetValue(normalizeCommittedValue(draft));
	}

	/**
	 * Discards the current draft without changing the committed value.
	 */
	public void cancelEditing() {
		editing = false;
		draftValue = null;
	}

	/**
	 * Returns the value that should be displayed by consumers while editing or committed.
	 * <p>
	 * During editing, the live draft is returned as-is, including empty text. After commit,
	 * blank persisted values fall back to the default.
	 *
	 * @return active display value
	 */
	public String getActiveValue() {
		if (editing) {
			return draftValue == null ? "" : draftValue;
		}

		String value = getValue();
		return value.isBlank() ? getDefaultValue() : value;
	}

	private String normalizeCommittedValue(String draft) {
		if (draft == null || draft.isBlank()) {
			return getDefaultValue();
		}
		return draft;
	}

	@Override
	public SettingType getType() {
		return SettingType.STRING;
	}

	@Override
	protected ValidationResult validateValue(String value) {
		if (value == null) {
			return ValidationResult.error("Value must not be null");
		}
		if (value.length() > maxLength) {
			return ValidationResult.error("Value must not exceed " + maxLength + " characters");
		}
		return ValidationResult.ok();
	}

	@Override
	public String serializeValue(String value) {
		return value;
	}

	@Override
	public String deserializeValue(String serialized) {
		return serialized;
	}
}
