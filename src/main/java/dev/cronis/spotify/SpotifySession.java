package dev.cronis.spotify;

import java.util.Objects;

/**
 * Authentication and session state abstraction for Spotify connectivity.
 * <p>
 * OAuth, token refresh and account switching will be implemented against this
 * type without exposing transport details to widgets or the GUI.
 */
public final class SpotifySession {
	/**
	 * Session lifecycle states.
	 */
	public enum State {
		DISCONNECTED,
		CONNECTING,
		CONNECTED,
		EXPIRED,
		OFFLINE,
		ERROR
	}

	private volatile State state = State.DISCONNECTED;
	private volatile String accountId = "";
	private volatile String displayName = "";

	/**
	 * Returns the current session state.
	 *
	 * @return session state
	 */
	public State getState() {
		return state;
	}

	/**
	 * Returns the active account identifier when connected.
	 *
	 * @return account id, or empty when unavailable
	 */
	public String getAccountId() {
		return accountId;
	}

	/**
	 * Returns the active account display name when connected.
	 *
	 * @return display name, or empty when unavailable
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Returns whether the session can provide playback data.
	 *
	 * @return {@code true} when connected or offline-cached data is available
	 */
	public boolean isActive() {
		return state == State.CONNECTED || state == State.OFFLINE;
	}

	void update(State state, String accountId, String displayName) {
		this.state = Objects.requireNonNull(state, "state");
		this.accountId = Objects.requireNonNullElse(accountId, "");
		this.displayName = Objects.requireNonNullElse(displayName, "");
	}

	void reset() {
		update(State.DISCONNECTED, "", "");
	}
}
