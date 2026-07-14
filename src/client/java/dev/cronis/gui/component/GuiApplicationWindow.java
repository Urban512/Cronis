package dev.cronis.gui.component;

import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.render.RoundedRenderer;
import dev.cronis.gui.render.ShadowRenderer;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Full application window composing header, sidebar, and scrollable content.
 */
public class GuiApplicationWindow extends GuiWindow {
	private final GuiHeader header = new GuiHeader();
	private final GuiSidebar sidebar = new GuiSidebar();
	private final GuiContentPanel contentPanel = new GuiContentPanel();
	private final dev.cronis.gui.animation.FadeAnimation openAnimation = new dev.cronis.gui.animation.FadeAnimation(8f);

	public GuiApplicationWindow() {
		super("", GuiWindowStyle.defaults(), new dev.cronis.gui.render.BlurRenderer());
		addChild(header);
		addChild(sidebar);
		addChild(contentPanel);
		openAnimation.setImmediate(0f);
		openAnimation.setTarget(1f);
	}

	@Override
	public void layout(int screenWidth, int screenHeight) {
		super.layout(screenWidth, screenHeight);
		layoutSections();
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		openAnimation.update(delta);
		header.update(delta, mouseX, mouseY);
		sidebar.update(delta, mouseX, mouseY);
		contentPanel.update(delta, mouseX, mouseY);
	}

	@Override
	public void render(GuiGraphicsExtractor context, Font font) {
		if (width <= 0 || height <= 0) {
			return;
		}

		float fade = openAnimation.getValue();
		if (fade <= 0f) {
			return;
		}

		var theme = ThemeManager.get();
		GuiWindowStyle style = getStyle();

		ShadowRenderer.draw(
				context,
				x,
				y,
				width,
				height,
				style.cornerRadius(),
				style.shadowRadius(),
				style.shadowOpacity() * fade,
				theme.shadow()
		);

		int background = ColorUtil.withAlpha(theme.windowBackground(), fade);
		RoundedRenderer.fill(context, x, y, width, height, style.cornerRadius(), background);
		RoundedRenderer.outline(context, x, y, width, height, style.cornerRadius(), 1, ColorUtil.withAlpha(theme.windowBorder(), fade));

		header.render(context, font);
		sidebar.render(context, font);
		contentPanel.render(context, font);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return header.mouseClicked(mouseX, mouseY, button)
				|| sidebar.mouseClicked(mouseX, mouseY, button)
				|| contentPanel.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		return sidebar.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
				|| contentPanel.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	private void layoutSections() {
		header.layoutHeader(x, y, width);
		sidebar.layoutSidebar(x, y + GuiHeader.HEIGHT, height - GuiHeader.HEIGHT);
		contentPanel.layoutContent(
				x + GuiSidebar.WIDTH,
				y + GuiHeader.HEIGHT,
				width - GuiSidebar.WIDTH,
				height - GuiHeader.HEIGHT
		);
	}
}
