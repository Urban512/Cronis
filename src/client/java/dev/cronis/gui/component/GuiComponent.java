package dev.cronis.gui.component;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Root type for every reusable element in the Cronis GUI tree.
 * <p>
 * Components form a hierarchical structure that is laid out, themed, animated,
 * and rendered by dedicated framework layers. Concrete widgets extend this type.
 */
public abstract class GuiComponent {
	private final List<GuiComponent> children = new ArrayList<>();

	protected GuiComponent() {
	}

	/**
	 * Adds a child component to this container.
	 *
	 * @param child child component
	 */
	public void addChild(GuiComponent child) {
		children.add(Objects.requireNonNull(child, "child"));
	}

	/**
	 * Returns an unmodifiable view of the child components.
	 *
	 * @return child components
	 */
	public List<GuiComponent> getChildren() {
		return Collections.unmodifiableList(children);
	}

	/**
	 * Renders this component.
	 *
	 * @param context the draw context
	 * @param font    font used for text rendering
	 */
	public void render(GuiGraphicsExtractor context, Font font) {
	}

	/**
	 * Renders all child components.
	 *
	 * @param context the draw context
	 * @param font    font used for text rendering
	 */
	protected void renderChildren(GuiGraphicsExtractor context, Font font) {
		for (GuiComponent child : children) {
			child.render(context, font);
		}
	}
}
