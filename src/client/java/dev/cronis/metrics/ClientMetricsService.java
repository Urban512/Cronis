package dev.cronis.metrics;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;

import java.time.LocalTime;

/**
 * Client-side metrics sampled once per HUD frame.
 * <p>
 * Widgets read cached values from this service instead of querying Minecraft directly.
 */
public final class ClientMetricsService {
	private static final ClientMetricsService INSTANCE = new ClientMetricsService();
	private static final float TPS_SMOOTHING = 0.92f;
	private static final float MAX_TPS = 20.0f;

	private int fps;
	private int ping = -1;
	private float tps = MAX_TPS;

	private ClientMetricsService() {
	}

	public static ClientMetricsService get() {
		return INSTANCE;
	}

	/**
	 * Samples client metrics for the current frame.
	 *
	 * @param deltaSeconds elapsed real time since the previous HUD frame
	 */
	public void update(float deltaSeconds) {
		Minecraft client = Minecraft.getInstance();
		fps = client.getFps();
		ping = resolvePing(client);
		tps = estimateTps(deltaSeconds);
	}

	public int fps() {
		return fps;
	}

	/**
	 * Returns the local player latency in milliseconds, or {@code -1} when unavailable.
	 */
	public int ping() {
		return ping;
	}

	/**
	 * Returns a smoothed client-side TPS estimate.
	 */
	public float tps() {
		return tps;
	}

	public LocalTime localTime() {
		return LocalTime.now();
	}

	private static int resolvePing(Minecraft client) {
		ClientPacketListener connection = client.getConnection();
		LocalPlayer player = client.player;
		if (connection == null || player == null) {
			return -1;
		}

		var playerInfo = connection.getPlayerInfo(player.getUUID());
		if (playerInfo == null) {
			return -1;
		}

		return playerInfo.getLatency();
	}

	private float estimateTps(float deltaSeconds) {
		if (deltaSeconds <= 0f) {
			return tps;
		}

		float instantTps = Math.min(MAX_TPS, 1.0f / deltaSeconds);
		return tps * TPS_SMOOTHING + instantTps * (1.0f - TPS_SMOOTHING);
	}
}
