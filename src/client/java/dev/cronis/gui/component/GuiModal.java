package dev.cronis.gui.component;

import dev.cronis.gui.animation.FadeAnimation;
import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.render.RenderUtil;
import dev.cronis.gui.render.RoundedRenderer;
import dev.cronis.gui.render.ShadowRenderer;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

/**
 * Modal dialog overlay with scrim, title, message, and optional content.
 */
public class GuiModal extends GuiComponent {
	private static final int DIALOG_WIDTH = 360;
	private static final int DIALOG_MIN_HEIGHT = 180;
	private static final int CORNER_RADIUS = 14;
	private static final int BUTTON_WIDTH = 96;

	private final String title;
	private final String message;
	private final FadeAnimation openAnimation = new FadeAnimation(10f);
	private final GuiButton confirmButton = new GuiButton("Confirm");
	private final GuiButton cancelButton = new GuiButton("Cancel");
	private GuiComponent content;
	private Runnable onClose;
	private Runnable onConfirm;
	private boolean open;

	public GuiModal(String title, String message) {
		this.title = title;
		this.message = message;
		confirmButton.setOnClick(this::confirm);
		cancelButton.setOnClick(this::close);
		addChild(confirmButton);
		addChild(cancelButton);
		setVisible(false);
		openAnimation.setImmediate(0f);
	}

	public void setContent(GuiComponent content) {
		this.content = content;
		if (content != null) {
			content.setFocusManager(getFocusManager());
		}
	}

	public boolean isOpen() {
		return open;
	}

	public void open() {
		open = true;
		setVisible(true);
		openAnimation.setTarget(1f);
	}

	public void close() {
		open = false;
		openAnimation.setTarget(0f);
		if (onClose != null) {
			onClose.run();
		}
	}

	public GuiModal setOnClose(Runnable onClose) {
		this.onClose = onClose;
		return this;
	}

	public GuiModal setOnConfirm(Runnable onConfirm) {
		this.onConfirm = onConfirm;
		return this;
	}

	public GuiModal setConfirmLabel(String label) {
		confirmButton.setLabel(label);
		return this;
	}

	public GuiModal setCancelLabel(String label) {
		cancelButton.setLabel(label);
		return this;
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		openAnimation.update(delta);
		if (!open && openAnimation.getValue() <= 0f) {
			setVisible(false);
		}

		if (open) {
			layoutDialog(Minecraft.getInstance().font);
			confirmButton.update(delta, mouseX, mouseY);
			cancelButton.update(delta, mouseX, mouseY);
			if (content != null) {
				content.update(delta, mouseX, mouseY);
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!open || openAnimation.getValue() <= 0f) {
			return false;
		}

		if (confirmButton.mouseClicked(mouseX, mouseY, button)
				|| cancelButton.mouseClicked(mouseX, mouseY, button)
				|| (content != null && content.mouseClicked(mouseX, mouseY, button))) {
			return true;
		}

		if (!isInsideDialog((int) mouseX, (int) mouseY, Minecraft.getInstance().font)) {
			close();
		}
		return true;
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (!open) {
			return false;
		}

		if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
			close();
			return true;
		}
		return confirmButton.keyPressed(event)
				|| cancelButton.keyPressed(event)
				|| (content != null && content.keyPressed(event));
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		if (!open && openAnimation.getValue() <= 0f) {
			return;
		}

		float alpha = openAnimation.getValue();
		var theme = ThemeManager.get();
		int scrim = ColorUtil.withAlpha(theme.modalOverlay(), alpha * 0.85f);
		context.fill(x, y, x + width, y + height, scrim);

		int dialogHeight = dialogHeight(font);
		int dialogX = x + RenderUtil.centerX(width, DIALOG_WIDTH);
		int dialogY = y + RenderUtil.centerY(height, dialogHeight);
		int dialogBackground = ColorUtil.withAlpha(theme.modalBackground(), alpha);
		int dialogBorder = ColorUtil.withAlpha(theme.modalBorder(), alpha);

		ShadowRenderer.draw(context, dialogX, dialogY, DIALOG_WIDTH, dialogHeight, CORNER_RADIUS, 12, 0.35f * alpha, theme.shadow());
		RoundedRenderer.fill(context, dialogX, dialogY, DIALOG_WIDTH, dialogHeight, CORNER_RADIUS, dialogBackground);
		RoundedRenderer.outline(context, dialogX, dialogY, DIALOG_WIDTH, dialogHeight, CORNER_RADIUS, 1, dialogBorder);

		int textX = dialogX + Spacing.LG;
		int titleY = dialogY + Spacing.LG;
		context.text(font, title, textX, titleY, ColorUtil.withAlpha(theme.textPrimary(), alpha), false);
		context.text(font, message, textX, titleY + font.lineHeight + Spacing.SM, ColorUtil.withAlpha(theme.textSecondary(), alpha), false);

		if (content != null) {
			content.render(context, font);
		}

		confirmButton.render(context, font);
		cancelButton.render(context, font);
	}

	@Override
	protected void renderChildren(GuiGraphicsExtractor context, Font font) {
	}

	private void confirm() {
		if (onConfirm != null) {
			onConfirm.run();
		}
		close();
	}

	private void layoutDialog(Font font) {
		int dialogHeight = dialogHeight(font);
		int dialogX = x + RenderUtil.centerX(width, DIALOG_WIDTH);
		int dialogY = y + RenderUtil.centerY(height, dialogHeight);
		int buttonY = dialogY + dialogHeight - Spacing.LG - confirmButton.getPreferredHeight(width);
		int cancelX = dialogX + DIALOG_WIDTH - Spacing.LG - BUTTON_WIDTH;
		int confirmX = cancelX - Spacing.SM - BUTTON_WIDTH;
		cancelButton.setBounds(cancelX, buttonY, BUTTON_WIDTH, confirmButton.getPreferredHeight(width));
		confirmButton.setBounds(confirmX, buttonY, BUTTON_WIDTH, confirmButton.getPreferredHeight(width));

		if (content != null) {
			int contentY = dialogY + Spacing.LG + font.lineHeight * 2 + Spacing.MD;
			int contentHeight = buttonY - contentY - Spacing.MD;
			content.setBounds(dialogX + Spacing.LG, contentY, DIALOG_WIDTH - Spacing.LG * 2, Math.max(0, contentHeight));
		}
	}

	private int dialogHeight(Font font) {
		int base = DIALOG_MIN_HEIGHT;
		if (content != null) {
			base += content.getPreferredHeight(DIALOG_WIDTH - Spacing.LG * 2) + Spacing.MD;
		}
		return base + font.lineHeight;
	}

	private boolean isInsideDialog(int mouseX, int mouseY, Font font) {
		int dialogHeight = dialogHeight(font);
		int dialogX = x + RenderUtil.centerX(width, DIALOG_WIDTH);
		int dialogY = y + RenderUtil.centerY(height, dialogHeight);
		return mouseX >= dialogX && mouseX < dialogX + DIALOG_WIDTH && mouseY >= dialogY && mouseY < dialogY + dialogHeight;
	}
}
