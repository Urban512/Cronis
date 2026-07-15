package dev.cronis.client.hud;

import dev.cronis.metrics.ClientMetricsService;
import dev.cronis.widget.WidgetContext;
import dev.cronis.widget.WidgetManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;

/**
 * Bridges Minecraft HUD rendering to the Cronis widget framework.
 * <p>
 * Registered as a {@link HudElement} via {@link WidgetRenderHook}. Each frame
 * this renderer builds one viewport {@link WidgetContext}, then delegates to
 * {@link WidgetManager} for separate update and render passes.
 * <p>
 * Viewport dimensions, GUI scale, and window position are resolved inside
 * {@link WidgetContext#create}, which reads the active {@link net.minecraft.client.Minecraft}
 * window every frame. That keeps fullscreen toggles, resizes, and multi-monitor
 * moves accurate without additional listeners.
 */
public final class CronisHudRenderer implements HudElement {
	private static final CronisHudRenderer INSTANCE = new CronisHudRenderer();
	private static final float SECONDS_PER_TICK = 1.0f / 20.0f;

	private final WidgetManager widgetManager = WidgetManager.get();

	private CronisHudRenderer() {
	}

	/**
	 * Returns the shared HUD renderer instance registered with Fabric.
	 *
	 * @return HUD renderer
	 */
	public static CronisHudRenderer get() {
		return INSTANCE;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		Minecraft client = Minecraft.getInstance();
		if (!isGameplayActive(client)) {
			return;
		}

		float deltaSeconds = deltaTracker.getRealtimeDeltaTicks() * SECONDS_PER_TICK;
		ClientMetricsService.get().update(deltaSeconds);
		WidgetContext context = WidgetContext.create(graphics, client.font, deltaSeconds);

		widgetManager.update(context);
		widgetManager.render(context);
	}

	/**
	 * Returns whether widgets should participate in the current frame.
	 * <p>
	 * Fabric inherits {@code Options.hideGui} from {@code VanillaHudElements.SUBTITLES}
	 * for our layer, so hide-GUI is handled by the registry. This guard only excludes
	 * frames where no in-world gameplay HUD is expected.
	 *
	 * @param client active client
	 * @return {@code true} during normal in-world gameplay
	 */
	private static boolean isGameplayActive(Minecraft client) {
		return client.player != null && client.level != null && client.screen == null;
	}
}
