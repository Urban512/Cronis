package dev.cronis.spotify.os;

import java.util.Objects;

/**
 * Identifies an operating-system media session discovered by a platform provider.
 *
 * @param sessionId       stable session identifier from the platform
 * @param applicationId   application identifier such as {@code Spotify.exe}
 * @param applicationName user-facing application name
 */
public record MediaSession(
		String sessionId,
		String applicationId,
		String applicationName
) {
	public MediaSession {
		Objects.requireNonNull(sessionId, "sessionId");
		Objects.requireNonNull(applicationId, "applicationId");
		Objects.requireNonNull(applicationName, "applicationName");
	}

	/**
	 * Returns whether this session belongs to Spotify.
	 *
	 * @return {@code true} for Spotify desktop or store sessions
	 */
	public boolean isSpotify() {
		String normalizedId = applicationId.toLowerCase();
		String normalizedName = applicationName.toLowerCase();
		return normalizedId.contains("spotify")
				|| normalizedName.contains("spotify");
	}
}
