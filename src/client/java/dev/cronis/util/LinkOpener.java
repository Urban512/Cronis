package dev.cronis.util;

import dev.cronis.Cronis;
import net.minecraft.util.Util;

import java.awt.Desktop;
import java.net.URI;

/**
 * Opens external project links using the platform APIs available to Minecraft.
 */
public final class LinkOpener {
	private LinkOpener() {
	}

	/**
	 * Opens an HTTP or HTTPS URL in the user's default browser.
	 *
	 * @param url URL to open
	 */
	public static void open(String url) {
		if (url == null || url.isBlank()) {
			return;
		}

		try {
			URI uri = URI.create(url.trim());
			if (!isHttpUri(uri)) {
				Cronis.LOGGER.warn("Refused to open unsupported URI scheme: {}", uri.getScheme());
				return;
			}

			Util.getPlatform().openUri(uri);
		} catch (RuntimeException primaryFailure) {
			Cronis.LOGGER.warn("Failed to open link with Minecraft platform API: {}", url, primaryFailure);
			openWithDesktopFallback(url);
		}
	}

	private static void openWithDesktopFallback(String url) {
		if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
			Cronis.LOGGER.warn("Desktop browse is not supported; could not open link: {}", url);
			return;
		}

		try {
			URI uri = URI.create(url.trim());
			if (!isHttpUri(uri)) {
				return;
			}

			Desktop.getDesktop().browse(uri);
		} catch (Exception fallbackFailure) {
			Cronis.LOGGER.error("Failed to open link: {}", url, fallbackFailure);
		}
	}

	private static boolean isHttpUri(URI uri) {
		String scheme = uri.getScheme();
		return "https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme);
	}
}
