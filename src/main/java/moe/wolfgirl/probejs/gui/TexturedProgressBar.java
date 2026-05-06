package moe.wolfgirl.probejs.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class TexturedProgressBar extends AbstractWidget {
    private final ResourceLocation borderTexture;
    private final ResourceLocation fillTexture;
    private final int fillPadding;
    private float progress;

    public TexturedProgressBar(int x, int y, int width, int height, Component message) {
        this(
            x,
            y,
            width,
            height,
            message,
            DumpGuiTextures.PROGRESS_BAR_BORDER,
            DumpGuiTextures.PROGRESS_BAR_FILL,
            DumpGuiTextures.PROGRESS_BAR_PADDING,
            0.0F
        );
    }

    public TexturedProgressBar(
        int x,
        int y,
        int width,
        int height,
        Component message,
        ResourceLocation borderTexture,
        ResourceLocation fillTexture,
        int fillPadding,
        float progress
    ) {
        super(x, y, width, height, message);
        this.borderTexture = borderTexture;
        this.fillTexture = fillTexture;
        this.fillPadding = Math.max(0, fillPadding);
        this.setProgress(progress);
    }

    public float getProgress() {
        return this.progress;
    }

    public void setProgress(float progress) {
        this.progress = Mth.clamp(progress, 0.0F, 1.0F);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Font font = Minecraft.getInstance().font;
        int innerX = this.getX() + this.fillPadding;
        int innerY = this.getY() + this.fillPadding;
        int innerWidth = Math.max(0, this.width - this.fillPadding * 2);
        int innerHeight = Math.max(0, this.height - this.fillPadding * 2);
        int filledWidth = Mth.clamp(Mth.floor(innerWidth * this.progress), 0, innerWidth);
        int textY = this.getY() + (this.height - font.lineHeight) / 2 + 1;
        Component percentLabel = Component.literal(Math.round(this.progress * 100.0F) + "%");

        guiGraphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        guiGraphics.blit(this.borderTexture, this.getX(), this.getY(), 0, 0.0F, 0.0F, this.width, this.height, this.width, this.height);

        if (filledWidth > 0 && innerHeight > 0) {
            guiGraphics.enableScissor(innerX, innerY, innerX + filledWidth, innerY + innerHeight);
            guiGraphics.blit(this.fillTexture, innerX, innerY, 0, 0.0F, 0.0F, innerWidth, innerHeight, innerWidth, innerHeight);
            guiGraphics.disableScissor();
        }

        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.drawCenteredString(font, percentLabel, this.getX() + this.width / 2, textY, 0xFFF2F5F7);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(
            NarratedElementType.TITLE,
            Component.literal(this.getMessage().getString() + ": " + Math.round(this.progress * 100.0F) + "%")
        );
        narrationElementOutput.add(NarratedElementType.HINT, Component.literal("Read only"));
    }
}