package moe.wolfgirl.probejs.misc.gui;

import moe.wolfgirl.probejs.ProbeJS;
import net.minecraft.resources.ResourceLocation;

public final class DumpGuiTextures {
    public static final int CHECKBOX_TEXTURE_WIDTH = 10;
    public static final int CHECKBOX_TEXTURE_HEIGHT = 10;
    public static final int PROGRESS_BAR_TEXTURE_WIDTH = 100;
    public static final int PROGRESS_BAR_TEXTURE_HEIGHT = 9;
    public static final int PROGRESS_BAR_PADDING = 2;

    // These paths are placeholders so the widgets can be wired before the real textures are added.
    public static final ResourceLocation CHECKBOX_OFF = id("textures/gui/dump/checkbox_off.png");
    public static final ResourceLocation CHECKBOX_ON = id("textures/gui/dump/checkbox_on.png");
    public static final ResourceLocation PROGRESS_BAR_BORDER = id("textures/gui/dump/progress_bar_border.png");
    public static final ResourceLocation PROGRESS_BAR_FILL = id("textures/gui/dump/progress_bar_fill.png");

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ProbeJS.MOD_ID, path);
    }
}