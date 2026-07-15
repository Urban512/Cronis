package dev.cronis.client;

import dev.cronis.client.command.CronisClientCommands;
import dev.cronis.assets.AssetManager;
import dev.cronis.assets.AssetRegistry;
import dev.cronis.client.hud.WidgetRenderHook;
import dev.cronis.spotify.SpotifyService;
import dev.cronis.widget.SpotifyWidget;
import dev.cronis.widget.WidgetManager;
import dev.cronis.widget.performance.ClockWidget;
import dev.cronis.widget.performance.FpsWidget;
import dev.cronis.widget.performance.PingWidget;
import dev.cronis.widget.performance.TpsWidget;
import net.fabricmc.api.ClientModInitializer;

public class CronisClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		AssetManager.initialize();
		AssetRegistry.clear();

		CronisClientCommands.register();
		WidgetRenderHook.register();

		WidgetManager widgetManager = WidgetManager.get();
		widgetManager.load();
		widgetManager.register(new FpsWidget());
		widgetManager.register(new PingWidget());
		widgetManager.register(new TpsWidget());
		widgetManager.register(new ClockWidget());
		widgetManager.register(new SpotifyWidget());

		SpotifyService.get().start();
	}
}