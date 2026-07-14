package dev.cronis.gui.component;

import dev.cronis.gui.util.GuiBounds;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Root type for every reusable element in the Cronis GUI tree.
 */
public abstract class GuiComponent {
	private final List<GuiComponent> children = new ArrayList<>();

	protected int x;
	protected int y;
	protected int width;
	protected int height;
	protected boolean visible = true;
	protected boolean enabled = true;

	protected GuiComponent() {
	}

	public void addChild(GuiComponent child) {
		children.add(Objects.requireNonNull(child, "child"));
		child.parent = this;
	}

	public List<GuiComponent> getChildren() {
		return Collections.unmodifiableList(children);
	}

	protected GuiComponent parent;

	public GuiBounds getBounds() {
		return new GuiBounds(x, y, width, height);
	}

	public void setBounds(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getPreferredHeight(int availableWidth) {
		return height;
	}

	public int getPreferredWidth(int availableHeight) {
		return width;
	}

	protected boolean contains(int mouseX, int mouseY) {
		return visible && mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
	}

	public void update(float delta, int mouseX, int mouseY) {
		for (GuiComponent child : children) {
			child.update(delta, mouseX, mouseY);
		}
	}

	public void render(GuiGraphicsExtractor context, Font font) {
		if (!visible) {
			return;
		}

		renderComponent(context, font);
		renderChildren(context, font);
	}

	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
	}

	protected void renderChildren(GuiGraphicsExtractor context, Font font) {
		for (GuiComponent child : children) {
			child.render(context, font);
		}
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!visible || !enabled) {
			return false;
		}

		for (int index = children.size() - 1; index >= 0; index--) {
			if (children.get(index).mouseClicked(mouseX, mouseY, button)) {
				return true;
			}
		}

		return handleMouseClicked(mouseX, mouseY, button);
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (!visible) {
			return false;
		}

		for (int index = children.size() - 1; index >= 0; index--) {
			if (children.get(index).mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
				return true;
			}
		}

		return handleMouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	protected boolean handleMouseClicked(double mouseX, double mouseY, int button) {
		return false;
	}

	protected boolean handleMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		return false;
	}
}
