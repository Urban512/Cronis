package dev.cronis.widget;

import dev.cronis.gui.layout.Padding;
import dev.cronis.gui.theme.GuiMetrics;
import dev.cronis.gui.render.ProgressBarRenderer;
import dev.cronis.gui.render.RenderUtil;
import dev.cronis.gui.theme.GuiTheme;
import dev.cronis.settings.BooleanSetting;
import dev.cronis.settings.SettingGroup;
import dev.cronis.settings.StringSetting;
import dev.cronis.spotify.SpotifyPlaybackState;
import dev.cronis.spotify.SpotifyService;
import dev.cronis.spotify.SpotifyTrack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.joml.Matrix3x2fStack;

import java.util.Optional;

/**
 * Production HUD widget that displays the current Spotify playback state.
 */
public final class SpotifyWidget extends Widget {
	private static final String WIDGET_ID = "spotify";
	private static final String DISPLAY_NAME = "Spotify";
	private static final String UNAVAILABLE_TITLE = "Spotify";
	private static final String UNAVAILABLE_STATUS = "Not Playing";
	private static final String DEFAULT_HEADER_TEXT = "Listening";
	private static final String ELLIPSIS = "...";

	private static final int MAX_CONTENT_WIDTH = 220;
	private static final int MIN_CONTENT_WIDTH = 96;
	private static final int FALLBACK_LINE_HEIGHT = 9;
	private static final int MIN_CONTENT_HEIGHT = FALLBACK_LINE_HEIGHT * 2 + GuiMetrics.WIDGET_LINE_GAP;

	private static final float STATUS_SCALE = GuiMetrics.TEXT_SCALE_CAPTION;
	private static final float TITLE_SCALE = GuiMetrics.TEXT_SCALE_BODY;
	private static final float ARTIST_SCALE = GuiMetrics.TEXT_SCALE_VALUE;

	private static final int STATUS_TO_TITLE_GAP = GuiMetrics.WIDGET_SECTION_GAP;
	private static final int TITLE_TO_ARTIST_GAP = GuiMetrics.WIDGET_LINE_GAP;
	private static final int ARTIST_TO_PROGRESS_GAP = GuiMetrics.WIDGET_SECTION_GAP;
	private static final int PROGRESS_HEIGHT = GuiMetrics.PROGRESS_HEIGHT;

	private static final Padding PADDING = GuiMetrics.PADDING_WIDGET;

	private final SpotifyService spotifyService = SpotifyService.get();

	private boolean playbackAvailable;
	private boolean showStatusRow;
	private boolean showProgressBar;
	private String statusRowText = "";
	private String playbackIndicator = "";
	private String titleRowText = UNAVAILABLE_STATUS;
	private String artistRowText = "";
	private String layoutSignature = "";

	private WidgetSize cachedPreferredSize;
	private WidgetSize cachedMinimumSize;
	private float lastAppliedScale = Float.NaN;

	public SpotifyWidget() {
		super(WIDGET_ID, DISPLAY_NAME, WidgetCategory.MEDIA, WidgetSurfaceStyle.CARD);
		setAnchor(WidgetAnchor.TOP_LEFT);
		setPosition(new WidgetPosition(GuiMetrics.SCREEN_MARGIN, GuiMetrics.SCREEN_MARGIN + 80));
	}

	@Override
	protected void onRegistered() {
		lastAppliedScale = getScale();
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
		SpotifyPlaybackState currentPlaybackState = spotifyService.getPlaybackState();
		boolean available = track.isPresent() && currentPlaybackState != SpotifyPlaybackState.STOPPED;

		boolean showStatusText = getSettings()
				.get("showStatusText", BooleanSetting.class)
				.map(BooleanSetting::getValue)
				.orElse(true);
		String statusText = resolveStatusText();
		String titleText = available ? track.get().title() : UNAVAILABLE_STATUS;
		String artistText = available ? track.get().artistLine() : "";
		boolean durationAvailable = available && track.get().durationMs() > 0L;

		String signature = buildLayoutSignature(
				available,
				showStatusText,
				statusText,
				titleText,
				artistText,
				currentPlaybackState,
				durationAvailable
		);
		if (!signature.equals(layoutSignature)) {
			layoutSignature = signature;
			playbackAvailable = available;
			applyDisplayContent(
					context.font(),
					available,
					showStatusText,
					statusText,
					titleText,
					artistText,
					currentPlaybackState,
					durationAvailable
			);
			recalculateContentSizes(context.font());
		} else if (getScale() != lastAppliedScale && cachedPreferredSize != null) {
			applyScaledSize(cachedPreferredSize);
		}
	}

	@Override
	public void render(WidgetContext context) {
		WidgetBounds bounds = resolveBounds(context);
		GuiGraphicsExtractor graphics = context.graphics();
		Font font = context.font();
		GuiTheme theme = context.theme();
		float scale = getScale();

		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(bounds.x(), bounds.y());
		if (scale != 1.0f) {
			pose.scale(scale, scale);
		}

		int unscaledWidth = cachedPreferredSize != null ? cachedPreferredSize.width() : Math.round(bounds.width() / scale);
		int unscaledHeight = cachedPreferredSize != null ? cachedPreferredSize.height() : Math.round(bounds.height() / scale);

		WidgetSurface.render(graphics, 0, 0, unscaledWidth, unscaledHeight, theme);

		int contentX = PADDING.left();
		int contentWidth = unscaledWidth - PADDING.horizontal();
		int textY = PADDING.top();

		if (showStatusRow && !statusRowText.isEmpty()) {
			textY = drawTextRow(
					graphics,
					font,
					contentX,
					textY,
					contentWidth,
					statusRowText,
					theme.textMuted(),
					STATUS_SCALE
			);
			textY += STATUS_TO_TITLE_GAP;
		}

		int titleColor = playbackAvailable ? theme.textPrimary() : theme.textMuted();
		textY = drawTitleRow(graphics, font, contentX, textY, contentWidth, titleColor);

		if (!artistRowText.isEmpty()) {
			textY += TITLE_TO_ARTIST_GAP;
			textY = drawTextRow(
					graphics,
					font,
					contentX,
					textY,
					contentWidth,
					artistRowText,
					theme.textSecondary(),
					ARTIST_SCALE
			);
		}

		if (showProgressBar) {
			float progress = spotifyService.getCurrentTrack()
					.map(SpotifyTrack::progressRatio)
					.orElse(0f);
			int progressY = textY + ARTIST_TO_PROGRESS_GAP;
			drawProgressBar(graphics, contentX, progressY, contentWidth, progress, theme);
		}

		pose.popMatrix();
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
			Font font,
			boolean available,
			boolean showStatusText,
			String statusText,
			String titleText,
			String artistText,
			SpotifyPlaybackState currentPlaybackState,
			boolean durationAvailable
	) {
		if (available) {
			playbackIndicator = PlaybackIndicator.forState(currentPlaybackState).glyph();
			titleRowText = ellipsize(font, titleText, titleMaxWidth(font), TITLE_SCALE);
			artistRowText = artistText.isEmpty()
					? ""
					: ellipsize(font, artistText, MAX_CONTENT_WIDTH, ARTIST_SCALE);
			showStatusRow = showStatusText && !statusText.isEmpty();
			statusRowText = showStatusRow ? ellipsize(font, statusText, MAX_CONTENT_WIDTH, STATUS_SCALE) : "";
			showProgressBar = durationAvailable;
			return;
		}

		playbackIndicator = "";
		titleRowText = UNAVAILABLE_STATUS;
		artistRowText = "";
		showStatusRow = showStatusText;
		statusRowText = showStatusText
				? ellipsize(font, UNAVAILABLE_TITLE, MAX_CONTENT_WIDTH, STATUS_SCALE)
				: "";
		showProgressBar = false;
	}

	private void recalculateContentSizes(Font font) {
		WidgetSize measured = measureContentSize(font);
		cachedMinimumSize = measured;
		cachedPreferredSize = measured;
		applyScaledSize(measured);
	}

	private void applyScaledSize(WidgetSize unscaled) {
		WidgetSize scaled = scaleSize(unscaled);
		setSize(scaled.width(), scaled.height());
		lastAppliedScale = getScale();
	}

	private WidgetSize measureContentSize(Font font) {
		int statusWidth = showStatusRow ? scaledTextWidth(font, statusRowText, STATUS_SCALE) : 0;
		int titleWidth = titleRowWidth(font);
		int artistWidth = artistRowText.isEmpty() ? 0 : scaledTextWidth(font, artistRowText, ARTIST_SCALE);
		int contentWidth = Math.max(MIN_CONTENT_WIDTH, Math.max(statusWidth, Math.max(titleWidth, artistWidth)));

		int contentHeight = PADDING.top() + PADDING.bottom();
		if (showStatusRow && !statusRowText.isEmpty()) {
			contentHeight += scaledLineHeight(font, STATUS_SCALE) + STATUS_TO_TITLE_GAP;
		}
		contentHeight += scaledLineHeight(font, TITLE_SCALE);
		if (!artistRowText.isEmpty()) {
			contentHeight += TITLE_TO_ARTIST_GAP + scaledLineHeight(font, ARTIST_SCALE);
		}
		if (showProgressBar) {
			contentHeight += ARTIST_TO_PROGRESS_GAP + PROGRESS_HEIGHT;
		}

		int height = Math.max(MIN_CONTENT_HEIGHT + PADDING.vertical(), contentHeight);
		return new WidgetSize(PADDING.horizontal() + contentWidth, height);
	}

	private int titleRowWidth(Font font) {
		int indicatorWidth = playbackIndicator.isEmpty() ? 0 : scaledTextWidth(font, playbackIndicator, TITLE_SCALE);
		int spacing = indicatorWidth == 0 ? 0 : scaledTextWidth(font, " ", TITLE_SCALE);
		return indicatorWidth + spacing + scaledTextWidth(font, titleRowText, TITLE_SCALE);
	}

	private int titleMaxWidth(Font font) {
		int reserved = 0;
		if (!playbackIndicator.isEmpty()) {
			reserved += scaledTextWidth(font, playbackIndicator, TITLE_SCALE);
			reserved += scaledTextWidth(font, " ", TITLE_SCALE);
		}
		return Math.max(0, MAX_CONTENT_WIDTH - reserved);
	}

	private int drawTitleRow(
			GuiGraphicsExtractor graphics,
			Font font,
			int x,
			int y,
			int width,
			int color
	) {
		int cursorX = x;
		if (!playbackIndicator.isEmpty()) {
			cursorX = drawScaledText(graphics, font, playbackIndicator, cursorX, y, color, TITLE_SCALE);
			cursorX += scaledTextWidth(font, " ", TITLE_SCALE);
		}

		int titleX = cursorX;
		int titleWidth = Math.max(0, width - (titleX - x));
		RenderUtil.scissor(graphics, x, y, width, scaledLineHeight(font, TITLE_SCALE), () ->
				drawScaledText(graphics, font, titleRowText, titleX, y, color, TITLE_SCALE)
		);
		return y + scaledLineHeight(font, TITLE_SCALE);
	}

	private static int drawTextRow(
			GuiGraphicsExtractor graphics,
			Font font,
			int x,
			int y,
			int width,
			String text,
			int color,
			float scale
	) {
		int lineHeight = scaledLineHeight(font, scale);
		RenderUtil.scissor(graphics, x, y, width, lineHeight, () ->
				drawScaledText(graphics, font, text, x, y, color, scale)
		);
		return y + lineHeight;
	}

	private static void drawProgressBar(
			GuiGraphicsExtractor graphics,
			int x,
			int y,
			int width,
			float progress,
			GuiTheme theme
	) {
		ProgressBarRenderer.draw(graphics, x, y, width, progress, theme.sliderTrack(), theme.sliderFill());
	}

	private static int drawScaledText(
			GuiGraphicsExtractor graphics,
			Font font,
			String text,
			int x,
			int y,
			int color,
			float scale
	) {
		if (text.isEmpty()) {
			return x;
		}

		if (scale == TITLE_SCALE) {
			graphics.text(font, text, x, y, color, false);
			return x + font.width(text);
		}

		Matrix3x2fStack pose = graphics.pose();
		pose.pushMatrix();
		pose.translate(x, y);
		pose.scale(scale, scale);
		graphics.text(font, text, 0, 0, color, false);
		pose.popMatrix();
		return x + scaledTextWidth(font, text, scale);
	}

	private static String ellipsize(Font font, String text, int maxWidth, float scale) {
		if (text.isEmpty() || maxWidth <= 0) {
			return text;
		}
		if (scaledTextWidth(font, text, scale) <= maxWidth) {
			return text;
		}

		int ellipsisWidth = scaledTextWidth(font, ELLIPSIS, scale);
		int targetWidth = Math.max(0, maxWidth - ellipsisWidth);
		for (int end = text.length(); end > 0; end--) {
			String candidate = text.substring(0, end);
			if (scaledTextWidth(font, candidate, scale) <= targetWidth) {
				return candidate + ELLIPSIS;
			}
		}

		return ELLIPSIS;
	}

	private static int scaledLineHeight(Font font, float scale) {
		return Math.max(1, Math.round(font.lineHeight * scale));
	}

	private static int scaledTextWidth(Font font, String text, float scale) {
		if (text.isEmpty()) {
			return 0;
		}
		return Math.round(font.width(text) * scale);
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

	private static String buildLayoutSignature(
			boolean available,
			boolean showStatusText,
			String statusText,
			String titleText,
			String artistText,
			SpotifyPlaybackState currentPlaybackState,
			boolean durationAvailable
	) {
		return available
				+ "|" + showStatusText
				+ "|" + statusText
				+ "|" + titleText
				+ "|" + artistText
				+ "|" + currentPlaybackState
				+ "|" + durationAvailable;
	}

	/**
	 * Playback glyphs shown before the track title.
	 */
	private enum PlaybackIndicator {
		PLAYING("\u25B6"),
		PAUSED("\u275A\u275A"),
		BUFFERING("\u2026"),
		NONE("");

		private final String glyph;

		PlaybackIndicator(String glyph) {
			this.glyph = glyph;
		}

		String glyph() {
			return glyph;
		}

		static PlaybackIndicator forState(SpotifyPlaybackState state) {
			return switch (state) {
				case PLAYING -> PLAYING;
				case PAUSED -> PAUSED;
				case BUFFERING -> BUFFERING;
				case STOPPED -> NONE;
			};
		}
	}
}
