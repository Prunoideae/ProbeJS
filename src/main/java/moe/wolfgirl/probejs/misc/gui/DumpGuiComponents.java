package moe.wolfgirl.probejs.misc.gui;

import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public final class DumpGuiComponents {
    private static final int DEFAULT_CONTROL_HEIGHT = 20;

    private DumpGuiComponents() {
    }

    public static Button createButton(int x, int y, int width, Component label, Button.OnPress onPress) {
        return Button.builder(label, onPress)
            .bounds(x, y, width, DEFAULT_CONTROL_HEIGHT)
            .build();
    }

    public static EditBox createTextBox(
        Font font,
        int x,
        int y,
        int width,
        Component label,
        String initialValue,
        Consumer<String> responder
    ) {
        EditBox editBox = new EditBox(font, x, y, width, DEFAULT_CONTROL_HEIGHT, label);
        editBox.setMaxLength(256);
        editBox.setValue(initialValue);
        editBox.setResponder(responder);
        return editBox;
    }
}