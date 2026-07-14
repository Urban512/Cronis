package dev.cronis.client;

import dev.cronis.client.command.CronisClientCommands;
import dev.cronis.client.hud.WidgetRenderHook;
import net.fabricmc.api.ClientModInitializer;

public class CronisClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		CronisClientCommands.register();
		WidgetRenderHook.register();
	}
}