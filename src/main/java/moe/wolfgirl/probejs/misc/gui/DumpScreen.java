package moe.wolfgirl.probejs.misc.gui;

import moe.wolfgirl.probejs.DumpState;
import moe.wolfgirl.probejs.GameStates;
import moe.wolfgirl.probejs.ProbeConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import moe.wolfgirl.probejs.misc.RequireToLoadClass;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DumpScreen extends Screen {
    private static final int PANEL_WIDTH = 236;
    private static final int PANEL_HEIGHT = 180;
    private static final int CONTENT_MARGIN = 18;
    private static final int DUMP_BUTTON_WIDTH = 60;
    private static final int CONVERT_BUTTON_WIDTH = 50;
    private static final int CONTROL_HEIGHT = 20;
    private static final int PANEL_OUTER_COLOR = 0xFF000000;
    private static final int PANEL_INNER_COLOR = 0xFF303030;
    private static final int PANEL_HEADER_COLOR = 0xFF6F6F6F;

    private EditBox textBox;
    private TexturedCheckbox fullDumpCheckbox;
    private TexturedCheckbox beansCheckbox;
    private TexturedCheckbox hintsForLLMCheckbox;
    private TexturedProgressBar progressBar;
    private Button dumpButton;
    private Button convertButton;
    private List<String> modListFilter = ProbeConfig.INSTANCE.fullScanMods.get();
    private boolean fullDumpEnabled = ProbeConfig.INSTANCE.complete.get();
    private boolean beansEnabled = ProbeConfig.INSTANCE.beans.get();
    private boolean hintsForLLMEnabled = ProbeConfig.INSTANCE.hintsForLLM.get();
    private int progress = 0;
    private float maxProgress = 100F;
    private Component statusMessage = Component.literal("Ready");

    public static void open() {
        DumpScreen screen = new DumpScreen();
        Minecraft.getInstance().setScreen(screen);
        GameStates.DUMP_SCREEN = screen;
        screen.initFromState();
    }

    public void initFromState() {
        if (GameStates.DUMP_STATE != null) {
            this.setMaxProgress(GameStates.DUMP_STATE.maxProgress);
            this.setProgress(GameStates.DUMP_STATE.progress);
            this.setStatus(GameStates.DUMP_STATE.status);
            this.dumpButton.active = false;
        }
    }

    private DumpScreen() {
        super(Component.literal("ProbeJS"));
    }

    @Override
    public void onClose() {
        super.onClose();
        GameStates.DUMP_SCREEN = null;
    }

    @Override
    protected void init() {
        int left = this.panelLeft();
        int top = this.panelTop();
        int contentWidth = PANEL_WIDTH - CONTENT_MARGIN * 2;

        this.fullDumpCheckbox = this.addRenderableWidget(
                new TexturedCheckbox(
                        left + CONTENT_MARGIN,
                        top + 24,
                        contentWidth - CONVERT_BUTTON_WIDTH - 4,
                        Component.literal("Full Dump"),
                        this.fullDumpEnabled,
                        selected -> {
                            this.fullDumpEnabled = selected;
                            ProbeConfig.INSTANCE.complete.set(selected);
                        },
                        Component.literal("Dumps more classes, now can handle ATM10!")
                )
        );

        this.convertButton = this.addRenderableWidget(
                DumpGuiComponents.createButton(
                        left + PANEL_WIDTH - CONTENT_MARGIN - CONVERT_BUTTON_WIDTH,
                        top + 24,
                        CONVERT_BUTTON_WIDTH,
                        Component.literal("Convert"),
                        button -> RequireToLoadClass.convertScripts()
                )
        );

        this.beansCheckbox = this.addRenderableWidget(
                new TexturedCheckbox(
                        left + CONTENT_MARGIN,
                        top + 50,
                        65,
                        Component.literal("Beans"),
                        this.beansEnabled,
                        selected -> {
                            this.beansEnabled = selected;
                            ProbeConfig.INSTANCE.beans.set(selected);
                        },
                        Component.literal("Generate beans for classes for easier access.")
                )
        );

        this.hintsForLLMCheckbox = this.addRenderableWidget(
                new TexturedCheckbox(
                        left + CONTENT_MARGIN + 65 + 4,
                        top + 50,
                        contentWidth - 65 - 4,
                        Component.literal("LLM support"),
                        this.hintsForLLMEnabled,
                        selected -> {
                            this.hintsForLLMEnabled = selected;
                            ProbeConfig.INSTANCE.hintsForLLM.set(selected);
                        },
                        Component.literal("Generate hints and agents related files.")
                )
        );

        this.textBox = this.addRenderableWidget(
                DumpGuiComponents.createTextBox(
                        this.font,
                        left + CONTENT_MARGIN,
                        top + 90,
                        contentWidth,
                        Component.literal("Full Scan Mods"),
                        String.join(", ", this.modListFilter),
                        this::onModListChanged
                )
        );
        this.textBox.setCanLoseFocus(true);

        this.progressBar = this.addRenderableWidget(
                new TexturedProgressBar(
                        left + CONTENT_MARGIN,
                        top + 120,
                        contentWidth,
                        CONTROL_HEIGHT,
                        Component.literal("Dump progress")
                )
        );
        this.progressBar.setProgress(this.getPercentProgress());

        this.dumpButton = this.addRenderableWidget(
                DumpGuiComponents.createButton(
                        left + PANEL_WIDTH - CONTENT_MARGIN - DUMP_BUTTON_WIDTH,
                        top + 148,
                        DUMP_BUTTON_WIDTH,
                        Component.literal("Dump"),
                        button -> this.onDumpPressed()
                )
        );

        this.setInitialFocus(this.textBox);
    }

    @Override
    protected void repositionElements() {
        if (this.textBox == null || this.fullDumpCheckbox == null || this.beansCheckbox == null
                || this.hintsForLLMCheckbox == null
                || this.progressBar == null || this.dumpButton == null || this.convertButton == null) {
            return;
        }

        int left = this.panelLeft();
        int top = this.panelTop();
        int contentWidth = PANEL_WIDTH - CONTENT_MARGIN * 2;

        this.fullDumpCheckbox.setX(left + CONTENT_MARGIN);
        this.fullDumpCheckbox.setY(top + 24);
        this.fullDumpCheckbox.setWidth(contentWidth - CONVERT_BUTTON_WIDTH - 4);
        this.fullDumpCheckbox.setHeight(CONTROL_HEIGHT);

        this.convertButton.setX(left + PANEL_WIDTH - CONTENT_MARGIN - CONVERT_BUTTON_WIDTH);
        this.convertButton.setY(top + 24);
        this.convertButton.setWidth(CONVERT_BUTTON_WIDTH);
        this.convertButton.setHeight(CONTROL_HEIGHT);

        this.beansCheckbox.setX(left + CONTENT_MARGIN);
        this.beansCheckbox.setY(top + 50);
        this.beansCheckbox.setWidth(65);
        this.beansCheckbox.setHeight(CONTROL_HEIGHT);

        this.hintsForLLMCheckbox.setX(left + CONTENT_MARGIN + 65 + 4);
        this.hintsForLLMCheckbox.setY(top + 50);
        this.hintsForLLMCheckbox.setWidth(contentWidth - 65 - 4);
        this.hintsForLLMCheckbox.setHeight(CONTROL_HEIGHT);

        this.textBox.setX(left + CONTENT_MARGIN);
        this.textBox.setY(top + 90);
        this.textBox.setWidth(contentWidth);
        this.textBox.setHeight(CONTROL_HEIGHT);

        this.progressBar.setX(left + CONTENT_MARGIN);
        this.progressBar.setY(top + 120);
        this.progressBar.setWidth(contentWidth);
        this.progressBar.setHeight(CONTROL_HEIGHT);

        this.dumpButton.setX(left + PANEL_WIDTH - CONTENT_MARGIN - DUMP_BUTTON_WIDTH);
        this.dumpButton.setY(top + 148);
        this.dumpButton.setWidth(DUMP_BUTTON_WIDTH);
        this.dumpButton.setHeight(CONTROL_HEIGHT);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int left = this.panelLeft();
        int top = this.panelTop();
        guiGraphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, PANEL_OUTER_COLOR);
        guiGraphics.fill(left + 1, top + 1, left + PANEL_WIDTH - 1, top + PANEL_HEIGHT - 1, PANEL_INNER_COLOR);
        guiGraphics.fill(left + 1, top + 1, left + PANEL_WIDTH - 1, top + 18, PANEL_HEADER_COLOR);
        guiGraphics.renderOutline(left, top, PANEL_WIDTH, PANEL_HEIGHT, 0xFF667A8F);
        guiGraphics.drawCenteredString(this.font, this.title, left + 28, top + 6, 0xFFFFFFFF);

        guiGraphics.drawString(this.font, Component.literal("Full Scan Mods (, Separated)"), left + CONTENT_MARGIN, top + 78, 0xFFD7E0E8, false);

        int statusY = top + 148 + (CONTROL_HEIGHT - this.font.lineHeight) / 2;
        guiGraphics.drawString(this.font, this.statusMessage, left + CONTENT_MARGIN, statusY, 0xFFB8C9D9, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void onModListChanged(String value) {
        // We only apply changes when the dump button is pressed, to avoid too many writes during user typing
    }

    private void onDumpPressed() {
        String value = this.textBox.getValue();
        this.modListFilter = Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        ProbeConfig.INSTANCE.fullScanMods.set(this.modListFilter);
        this.dumpButton.active = false; // Disable until dump is finished
        new Thread(DumpState::startDump).start();
    }

    public void onDumpFinished() {
        this.dumpButton.active = true;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        float actualProgress = Math.clamp(this.progress / this.maxProgress, 0.0F, 1.0F);
        if (this.progressBar != null) {
            this.progressBar.setProgress(actualProgress);
        }
    }

    public void setStatus(Component status) {
        this.statusMessage = status;
    }

    private int panelLeft() {
        return (this.width - PANEL_WIDTH) / 2;
    }

    private int panelTop() {
        return (this.height - PANEL_HEIGHT) / 2;
    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
    }

    public void increaseProgress(int increment) {
        this.setProgress(this.progress + increment);
    }

    public float getPercentProgress() {
        return Math.clamp(this.progress / this.maxProgress, 0.0F, 1.0F);
    }

}