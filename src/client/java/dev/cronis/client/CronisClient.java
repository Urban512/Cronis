package dev.cronis.client;

import dev.cronis.client.command.CronisClientCommands;
import dev.cronis.client.hud.WidgetRenderHook;
import dev.cronis.spotify.SpotifyService;
import dev.cronis.widget.DemoWidget;
import dev.cronis.widget.SpotifyWidget;
import dev.cronis.widget.WidgetManager;
import net.fabricmc.api.ClientModInitializer;

public class CronisClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		CronisClientCommands.register();
		WidgetRenderHook.register();

		WidgetManager widgetManager = WidgetManager.get();
		widgetManager.load();
		widgetManager.register(new DemoWidget());
		widgetManager.register(new SpotifyWidget());

		SpotifyService.get().start();
	}
}