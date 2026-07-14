package dev.cronis.settings;

/**
 * Receives notifications when a setting value changes.
 *
 * @param <T> setting value type
 */
@FunctionalInterface
public interface SettingChangeListener<T> {
	/**
	 * Called after a setting value has changed.
	 *
	 * @param setting  setting that changed
	 * @param oldValue previous value
	 * @param newValue current value
	 */
	void onChanged(Setting<T> setting, T oldValue, T newValue);
}
