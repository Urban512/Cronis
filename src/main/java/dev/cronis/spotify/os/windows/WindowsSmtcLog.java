package dev.cronis.spotify.os.windows;

import dev.cronis.Cronis;

/**
 * Structured debug logging for the Windows SMTC backend.
 */
final class WindowsSmtcLog {
	private static final String PREFIX = "[Windows SMTC] ";

	private WindowsSmtcLog() {
	}

	static void info(String message) {
		Cronis.LOGGER.info("{}{}", PREFIX, message);
	}

	static void info(String message, Object arg) {
		Cronis.LOGGER.info("{}{}", PREFIX, message, arg);
	}

	static void warn(String message) {
		Cronis.LOGGER.warn("{}{}", PREFIX, message);
	}

	static void warn(String message, Throwable throwable) {
		Cronis.LOGGER.warn("{}{}", PREFIX, message, throwable);
	}

	static void stage(String stage, String detail) {
		if (detail == null || detail.isBlank()) {
			Cronis.LOGGER.info("{}Stage: {}", PREFIX, stage);
			return;
		}

		Cronis.LOGGER.info("{}Stage: {} — {}", PREFIX, stage, detail);
	}

	static void failure(String stage, String reason) {
		Cronis.LOGGER.warn("{}Failed at stage '{}' — {}", PREFIX, stage, reason);
	}
}
