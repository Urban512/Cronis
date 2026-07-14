package dev.cronis.editor.inspector;

import dev.cronis.gui.component.GuiComponent;
import dev.cronis.gui.component.GuiLabel;
import dev.cronis.gui.layout.Spacing;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Inspector row with a left-aligned label and right-aligned control.
 */
public final class InspectorRow extends GuiComponent {
	private static final int LABEL_WIDTH_RATIO = 45;

	private final GuiLabel label;
	private final GuiComponent control;
	private final String settingId;

	public InspectorRow(String labelText, GuiComponent control) {
		this(labelText, control, null);
	}

	public InspectorRow(String labelText, GuiComponent control, String settingId) {
		this.label = GuiLabel.secondary(labelText);
		this.control = control;
		this.settingId = settingId;
		addChild(label);
		addChild(control);
	}

	public String settingId() {
		return settingId;
	}

	public GuiComponent control() {
		return control;
	}

	@Override
	public int getPreferredHeight(int availableWidth) {
		int labelHeight = label.getPreferredHeight(labelWidth(availableWidth));
		int controlHeight = control.getPreferredHeight(controlWidth(availableWidth));
		return Math.max(labelHeight, controlHeight);
	}

	@Override
	public void update(float delta, int mouseX, int mouseY) {
		layoutRow();
		super.update(delta, mouseX, mouseY);
	}

	@Override
	protected void renderComponent(GuiGraphicsExtractor context, Font font) {
		layoutRow();
	}

	@Override
	protected void renderChildren(GuiGraphicsExtractor context, Font font) {
		label.render(context, font);
		control.render(context, font);
	}

	private void layoutRow() {
		int labelWidth = labelWidth(width);
		int controlWidth = controlWidth(width);
		int rowHeight = getPreferredHeight(width);
		label.setBounds(x, y + verticalInset(label, rowHeight), labelWidth, rowHeight);
		control.setBounds(
				x + labelWidth + Spacing.SM,
				y + verticalInset(control, rowHeight),
				controlWidth,
				control.getPreferredHeight(controlWidth)
		);
	}

	private static int labelWidth(int availableWidth) {
		return Math.max(72, availableWidth * LABEL_WIDTH_RATIO / 100);
	}

	private int controlWidth(int availableWidth) {
		return Math.max(48, availableWidth - labelWidth(availableWidth) - Spacing.SM);
	}

	private static int verticalInset(GuiComponent component, int rowHeight) {
		int componentHeight = component.getPreferredHeight(rowHeight);
		return Math.max(0, (rowHeight - componentHeight) / 2);
	}
}
