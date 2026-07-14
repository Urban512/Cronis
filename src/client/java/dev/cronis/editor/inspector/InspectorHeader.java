package dev.cronis.editor.inspector;

import dev.cronis.gui.component.GuiComponent;
import dev.cronis.gui.component.GuiLabel;
import dev.cronis.gui.layout.Spacing;
import dev.cronis.gui.render.ColorUtil;
import dev.cronis.gui.render.RoundedRenderer;
import dev.cronis.gui.theme.ThemeManager;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Inspector panel header with title and close control.
 */
final class InspectorHeader extends GuiComponent {
	private static final int HEIGHT = 28;
	private static final int CLOSE_SIZE = 22;

	private final GuiLabel titleLabel = GuiLabel.heading("Widget");
	private Runnable onClose;
	private boolean closeHovered;

	public InspectorHeader() {
		height = HEIGHT;
	}

	public void setTitle(String title) {
		titleLabel.setText(title);
	}

	public void setOnClose(Runnable onClose) {
		this.onClose = onClose;
	}

	@Override
	public int getPreferredHeight(int availableWidth) {
		return HEIGHT;
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		int closeX = x + width - CLOSE_SIZE;
		int closeY = y + (height - CLOSE_SIZE) / 2;
		closeHovered = mouseX >= closeX && mouseX < closeX + CLOSE_SIZE
				&& mouseY >= closeY && mouseY < closeY + CLOSE_SIZE;
		super.update(delta, mouseX, mouseY);
	}

	@Override
	protected boolean handleMouseClicked(double mouseX, double mouseY, int button) {
		if (!closeHovered) {
			return false;
		}

		if (onClose != null) {
			onClose.run();
		}

		return true;
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		titleLabel.setBounds(x, y + Spacing.XS, width - CLOSE_SIZE - Spacing.SM, height);
		titleLabel.render(context, font);

		var theme = ThemeManager.get();
		int closeX = x + width - CLOSE_SIZE;
		int closeY = y + (height - CLOSE_SIZE) / 2;
		if (closeHovered) {
			int hover = ColorUtil.withAlpha(theme.controlHover(), 0.65f);
			RoundedRenderer.fill(context, closeX, closeY, CLOSE_SIZE, CLOSE_SIZE, 6, hover);
		}

		int centerX = closeX + CLOSE_SIZE / 2;
		int centerY = closeY + CLOSE_SIZE / 2;
		int color = theme.textSecondary();
		context.fill(centerX - 4, centerY - 4, centerX + 4, centerY - 3, color);
		context.fill(centerX - 4, centerY + 3, centerX + 4, centerY + 4, color);
		context.fill(centerX - 3, centerY - 3, centerX - 2, centerY + 3, color);
		context.fill(centerX + 2, centerY - 3, centerX + 3, centerY + 3, color);
	}
}
