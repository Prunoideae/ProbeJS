package moe.wolfgirl.probejs.misc.gui;

import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class TexturedCheckbox extends AbstractButton {
    private static final int LABEL_GAP = 4;

    private final int boxWidth;
    private final int boxHeight;
    private final ResourceLocation uncheckedTexture;
    private final ResourceLocation checkedTexture;
    private final Component tooltipMessage;
    private Consumer<Boolean> onValueChanged;
    private boolean selected;

    public TexturedCheckbox(int x, int y, int width, Component message, boolean selected, Consumer<Boolean> onValueChanged) {
        this(
            x,
            y,
            width,
            Math.max(20, DumpGuiTextures.CHECKBOX_TEXTURE_HEIGHT),
            DumpGuiTextures.CHECKBOX_TEXTURE_WIDTH,
            DumpGuiTextures.CHECKBOX_TEXTURE_HEIGHT,
            message,
            selected,
            DumpGuiTextures.CHECKBOX_OFF,
            DumpGuiTextures.CHECKBOX_ON,
            onValueChanged,
            null
        );
    }

    public TexturedCheckbox(int x, int y, int width, Component message, boolean selected, Consumer<Boolean> onValueChanged, Component tooltip) {
        this(
            x,
            y,
            width,
            Math.max(20, DumpGuiTextures.CHECKBOX_TEXTURE_HEIGHT),
            DumpGuiTextures.CHECKBOX_TEXTURE_WIDTH,
            DumpGuiTextures.CHECKBOX_TEXTURE_HEIGHT,
            message,
            selected,
            DumpGuiTextures.CHECKBOX_OFF,
            DumpGuiTextures.CHECKBOX_ON,
            onValueChanged,
            tooltip
        );
    }

    public TexturedCheckbox(
        int x,
        int y,
        int width,
        int height,
        int boxWidth,
        int boxHeight,
        Component message,
        boolean selected,
        ResourceLocation uncheckedTexture,
        ResourceLocation checkedTexture,
        Consumer<Boolean> onValueChanged,
        Component tooltip
    ) {
        super(x, y, width, height, message);
        this.boxWidth = boxWidth;
        this.boxHeight = boxHeight;
        this.uncheckedTexture = uncheckedTexture;
        this.checkedTexture = checkedTexture;
        this.tooltipMessage = tooltip;
        this.onValueChanged = onValueChanged;
        this.selected = selected;
    }

    @Override
    public void onPress() {
        this.setSelected(!this.selected, true);
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setSelected(boolean selected) {
        this.setSelected(selected, false);
    }

    public void setSelected(boolean selected, boolean notify) {
        if (this.selected == selected) {
            return;
        }

        this.selected = selected;
        if (notify && this.onValueChanged != null) {
            this.onValueChanged.accept(this.selected);
        }
    }

    public void setOnValueChanged(Consumer<Boolean> onValueChanged) {
        this.onValueChanged = onValueChanged;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        ResourceLocation texture = this.selected ? this.checkedTexture : this.uncheckedTexture;
        int iconY = this.getY() + (this.height - this.boxHeight) / 2 - 1;
        int textX = this.getX() + this.boxWidth + LABEL_GAP;
        int textY = this.getY() + (this.height - font.lineHeight) / 2;
        int textColor = this.active ? 0xFFF2F5F7 : 0xFF8A9199;

        guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        guiGraphics.blit(texture, this.getX(), iconY, 0, 0.0F, 0.0F, this.boxWidth, this.boxHeight, this.boxWidth, this.boxHeight);
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (this.isHoveredOrFocused()) {
            int outlineColor = this.isFocused() ? 0xFFFFFFFF : 0x80FFFFFF;
            guiGraphics.renderOutline(this.getX() - 1, iconY - 1, this.boxWidth + 2, this.boxHeight + 2, outlineColor);
        }

        guiGraphics.drawString(font, this.getMessage(), textX, textY, textColor, false);

        if (this.isHovered() && this.tooltipMessage != null && !this.tooltipMessage.getString().isEmpty()) {
            guiGraphics.renderTooltip(font, this.tooltipMessage, mouseX, mouseY);
        }
    }

    @Override
    protected @NotNull MutableComponent createNarrationMessage() {
        return Component.literal(this.getMessage().getString() + ", " + (this.selected ? "on" : "off"));
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
        narrationElementOutput.add(NarratedElementType.HINT, Component.literal("Press to toggle"));
    }
}