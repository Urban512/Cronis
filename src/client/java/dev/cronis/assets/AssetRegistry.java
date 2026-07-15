package dev.cronis.assets;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which Cronis assets are available in the active resource pack.
 */
public final class AssetRegistry {
	private static final Set<Identifier> AVAILABLE = ConcurrentHashMap.newKeySet();
	private static final Set<Identifier> MISSING = ConcurrentHashMap.newKeySet();

	private AssetRegistry() {
	}

	public static void clear() {
		AVAILABLE.clear();
		MISSING.clear();
	}

	public static boolean isAvailable(Identifier identifier) {
		if (AVAILABLE.contains(identifier)) {
			return true;
		}
		if (MISSING.contains(identifier)) {
			return false;
		}

		boolean present = Minecraft.getInstance().getResourceManager().getResource(identifier).isPresent();
		if (present) {
			AVAILABLE.add(identifier);
		} else {
			MISSING.add(identifier);
		}
		return present;
	}
}
