package dev.cronis.assets;

import dev.cronis.Cronis;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cached access to Cronis asset identifiers.
 */
public final class AssetManager {
	private static final String ICONS = "icons/";
	private static final String LOGOS = "logos/";
	private static final String TEXTURES = "textures/";
	private static final String ILLUSTRATIONS = "illustrations/";

	private static final Map<String, Identifier> CACHE = new ConcurrentHashMap<>();

	private AssetManager() {
	}

	public static void initialize() {
		CACHE.clear();
	}

	public static Identifier icon(String fileName) {
		return resolve(ICONS + normalize(fileName));
	}

	public static Identifier logo(String fileName) {
		return resolve(LOGOS + normalize(fileName));
	}

	public static Identifier texture(String fileName) {
		return resolve(TEXTURES + normalize(fileName));
	}

	public static Identifier illustration(String fileName) {
		return resolve(ILLUSTRATIONS + normalize(fileName));
	}

	public static Identifier get(String path) {
		return resolve(normalize(path));
	}

	private static Identifier resolve(String path) {
		return CACHE.computeIfAbsent(path, AssetManager::createIdentifier);
	}

	private static Identifier createIdentifier(String path) {
		return Cronis.id(path);
	}

	private static String normalize(String fileName) {
		if (fileName == null || fileName.isBlank()) {
			throw new IllegalArgumentException("Asset file name must not be blank");
		}

		String normalized = fileName.replace('\\', '/');
		while (normalized.startsWith("/")) {
			normalized = normalized.substring(1);
		}
		return normalized;
	}
}
