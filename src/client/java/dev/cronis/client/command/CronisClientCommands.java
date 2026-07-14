package dev.cronis.client.command;

import com.mojang.brigadier.Command;
import dev.cronis.gui.screen.CronisScreen;
import dev.cronis.gui.screen.HudEditorScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;

public final class CronisClientCommands {
	private CronisClientCommands() {
	}

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
					ClientCommands.literal("cronis")
							.executes(context -> {
								Minecraft client = context.getSource().getClient();
								client.execute(() -> client.setScreen(new CronisScreen()));
								return Command.SINGLE_SUCCESS;
							})
							.then(
									ClientCommands.literal("hud")
											.executes(context -> {
												Minecraft client = context.getSource().getClient();
												client.execute(() -> client.setScreen(new HudEditorScreen()));
												return Command.SINGLE_SUCCESS;
											})
							)
			);
		});
	}
}
