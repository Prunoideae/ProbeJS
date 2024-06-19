package moe.wolfgirl.probejs.lang.linter;

import dev.latvian.mods.rhino.mod.util.color.Color;
import dev.latvian.mods.rhino.mod.wrapper.ColorWrapper;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;

public record LintingWarning(Path file, Level level, int line, int column, String message) {
    public enum Level {
        INFO(ColorWrapper.BLUE),
        WARNING(ColorWrapper.GOLD),
        ERROR(ColorWrapper.RED);

        public final Color color;

        Level(Color color) {
            this.color = color;
        }
    }

    public Component defaultFormatting(Path relativeBase) {
        Path stripped = relativeBase.getParent().relativize(file);

        return Component.literal("[")
                .append(Component.literal(level().name()).kjs$color(level().color))
                .append(Component.literal("] "))
                .append(Component.literal(stripped.toString()))
                .append(Component.literal(":%d:%d: %s".formatted(line, column, message)));
    }
}
