package dev.cronis.client.hud;

import dev.cronis.Cronis;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

/**
 * Fabric HUD integration for the Cronis widget framework.
 * <p>
 * Uses {@link HudElementRegistry} — the supported Fabric 26.1.2 HUD API.
 * {@code HudRenderCallback} was removed in 26.1; layer registration is the
 * replacement and provides deterministic ordering relative to vanilla HUD elements.
 * <p>
 * Cronis attaches after {@link VanillaHudElements#SUBTITLES}, the final vanilla
 * layer, so widgets render above the complete vanilla HUD while inheriting the
 * same {@code Options.hideGui} render condition as subtitles.
 */
public final class WidgetRenderHook {
	private WidgetRenderHook() {
	}

	/**
	 * Registers the Cronis widget HUD layer with Fabric.
	 */
	public static void register() {
		HudElementRegistry.attachElementAfter(
				VanillaHudElements.SUBTITLES,
				Cronis.id("widgets"),
				CronisHudRenderer.get()
		);

		Cronis.LOGGER.debug("Registered Cronis widget HUD layer after vanilla subtitles.");
	}
}
