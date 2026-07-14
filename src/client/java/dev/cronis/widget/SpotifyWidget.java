package dev.cronis.widget;

import dev.cronis.gui.layout.Padding;
import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.render.RoundedRenderer;
import dev.cronis.gui.theme.GuiTheme;
import dev.cronis.settings.BooleanSetting;
import dev.cronis.settings.SettingGroup;
import dev.cronis.settings.StringSetting;
import dev.cronis.spotify.SpotifyPlaybackState;
import dev.cronis.spotify.SpotifyService;
import dev.cronis.spotify.SpotifyTrack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.Optional;

/**
 * Production HUD widget that displays the current Spotify playback state.
 */
public final class SpotifyWidget extends Widget {
	private static final String WIDGET_ID = "spotify";
	private static final String DISPLAY_NAME = "Spotify";
	private static final String UNAVAILABLE_TITLE = "Spotify";
	private static final String UNAVAILABLE_STATUS = "Not Playing";
	private static final String TITLE_PREFIX = "\u266A ";
	private static final String DEFAULT_HEADER_TEXT = "Listening";

	private static final int CORNER_RADIUS = 10;
	private static final int LINE_GAP = Spacing.XS;
	private static final int FALLBACK_LINE_HEIGHT = 9;
	private static final int MIN_CONTENT_WIDTH = 96;
	private static final int MIN_CONTENT_HEIGHT = FALLBACK_LINE_HEIGHT * 2 + LINE_GAP;
	private static final Padding PADDING = Padding.symmetric(Spacing.MD, Spacing.LG);

	private final SpotifyService spotifyService = SpotifyService.get();

	private boolean playbackAvailable;
	private boolean showStatusRow;
	private String statusRowText = "";
	private String titleRowText = UNAVAILABLE_STATUS;
	private String artistRowText = "";
	private String contentSignature = "";

	private WidgetSize cachedPreferredSize;
	private WidgetSize cachedMinimumSize;

	public SpotifyWidget() {
		super(WIDGET_ID, DISPLAY_NAME, WidgetCategory.INFORMATION);
		setAnchor(WidgetAnchor.TOP_LEFT);
		setPosition(new WidgetPosition(Spacing.LG, Spacing.LG + 80));
	}

	@Override
	protected SettingGroup buildSettingsGroup() {
		SettingGroup group = new SettingGroup("spotify.settings", "Spotify", "Spotify widget options");

		BooleanSetting showStatusText = new BooleanSetting(
				"showStatusText",
				"Show Status Text",
				"Shows custom status text above track info when playback is active",
				true
		);
		StringSetting statusText = new StringSetting(
				"headerText",
				"Status Text",
				"Custom label shown above track info when playback is active",
				DEFAULT_HEADER_TEXT,
				64
		);
		statusText.setVisibilityPredicate(setting -> showStatusText.getValue());
		group.add(showStatusText);
		group.add(statusText);
		return group;
	}

	@Override
	public void update(WidgetContext context) {
		Optional<SpotifyTrack> track = spotifyService.getCurrentTrack();
		SpotifyPlaybackState playbackState = spotifyService.getPlaybackState();
		boolean available = track.isPresent() && playbackState != SpotifyPlaybackState.STOPPED;

		boolean showStatusText = getSettings()
				.get("showStatusText", BooleanSetting.class)
				.map(BooleanSetting::getValue)
				.orElse(true);
		String statusText = resolveStatusText();
		String titleText = available ? TITLE_PREFIX + track.get().title() : UNAVAILABLE_STATUS;
		String artistText = available ? track.get().artistLine() : "";

		String signature = buildContentSignature(available, showStatusText, statusText, titleText, artistText);
		if (!signature.equals(contentSignature)) {
			contentSignature = signature;
			playbackAvailable = available;
			applyDisplayContent(available, showStatusText, statusText, titleText, artistText);
			recalculateContentSizes(context.font());
		}
	}

	@Override
	public void render(WidgetContext context) {
		WidgetBounds bounds = resolveBounds(context);
		GuiGraphicsExtractor graphics = context.graphics();
		Font font = context.font();
		GuiTheme theme = context.theme();

		RoundedRenderer.fill(
				graphics,
				bounds.x(),
				bounds.y(),
				bounds.width(),
				bounds.height(),
				CORNER_RADIUS,
				theme.cardBackground()
		);
		RoundedRenderer.outline(
				graphics,
				bounds.x(),
				bounds.y(),
				bounds.width(),
				bounds.height(),
				CORNER_RADIUS,
				1,
				theme.cardBorder()
		);

		int contentX = bounds.x() + PADDING.left();
		int textY = bounds.y() + PADDING.top();

		if (showStatusRow && !statusRowText.isEmpty()) {
			textY = drawLine(graphics, font, contentX, textY, statusRowText, theme.textSecondary());
		}

		int titleColor = playbackAvailable ? theme.textPrimary() : theme.textMuted();
		textY = drawLine(graphics, font, contentX, textY, titleRowText, titleColor);

		if (!artistRowText.isEmpty()) {
			drawLine(graphics, font, contentX, textY, artistRowText, theme.textMuted());
		}
	}

	@Override
	public WidgetSize getPreferredSize() {
		if (cachedPreferredSize != null) {
			return cachedPreferredSize;
		}
		return fallbackContentSize();
	}

	@Override
	public WidgetSize getMinimumSize() {
		if (cachedMinimumSize != null) {
			return cachedMinimumSize;
		}
		return fallbackContentSize();
	}

	private void applyDisplayContent(
			boolean available,
			boolean showStatusText,
			String statusText,
			String titleText,
			String artistText
	) {
		if (available) {
			titleRowText = titleText;
			artistRowText = artistText;
			showStatusRow = showStatusText && !statusText.isEmpty();
			statusRowText = showStatusRow ? statusText : "";
			return;
		}

		titleRowText = UNAVAILABLE_STATUS;
		artistRowText = "";
		showStatusRow = showStatusText;
		statusRowText = showStatusText ? UNAVAILABLE_TITLE : "";
	}

	private void recalculateContentSizes(Font font) {
		WidgetSize measured = measureContentSize(font);
		cachedMinimumSize = measured;
		cachedPreferredSize = measured;
		setSize(measured.width(), measured.height());
	}

	private WidgetSize measureContentSize(Font font) {
		int statusWidth = showStatusRow ? font.width(statusRowText) : 0;
		int titleWidth = font.width(titleRowText);
		int artistWidth = artistRowText.isEmpty() ? 0 : font.width(artistRowText);
		int contentWidth = Math.max(MIN_CONTENT_WIDTH, Math.max(statusWidth, Math.max(titleWidth, artistWidth)));

		int lineCount = resolveDisplayLineCount();
		int contentHeight = font.lineHeight * lineCount + LINE_GAP * Math.max(0, lineCount - 1);
		int height = Math.max(MIN_CONTENT_HEIGHT + PADDING.vertical(), PADDING.vertical() + contentHeight);

		return new WidgetSize(PADDING.horizontal() + contentWidth, height);
	}

	private int resolveDisplayLineCount() {
		int lineCount = 1;

		if (showStatusRow && !statusRowText.isEmpty()) {
			lineCount++;
		}
		if (!artistRowText.isEmpty()) {
			lineCount++;
		}

		return lineCount;
	}

	private String resolveStatusText() {
		return getSettings()
				.get("headerText", StringSetting.class)
				.map(StringSetting::getActiveValue)
				.orElse(DEFAULT_HEADER_TEXT);
	}

	private static WidgetSize fallbackContentSize() {
		int width = PADDING.horizontal() + Math.max(MIN_CONTENT_WIDTH, 120);
		int height = PADDING.vertical() + MIN_CONTENT_HEIGHT;
		return new WidgetSize(width, height);
	}

	private static int drawLine(
			GuiGraphicsExtractor graphics,
			Font font,
			int x,
			int y,
			String text,
			int color
	) {
		graphics.text(font, text, x, y, color, false);
		return y + font.lineHeight + LINE_GAP;
	}

	private static String buildContentSignature(
			boolean available,
			boolean showStatusText,
			String statusText,
			String titleText,
			String artistText
	) {
		return available + "|" + showStatusText + "|" + statusText + "|" + titleText + "|" + artistText;
	}
}
