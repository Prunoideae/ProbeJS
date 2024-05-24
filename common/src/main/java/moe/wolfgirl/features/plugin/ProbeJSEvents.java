package moe.wolfgirl.features.plugin;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.Extra;
import dev.latvian.mods.kubejs.typings.desc.TypeDescJS;
import dev.latvian.mods.kubejs.util.UtilsJS;
import dev.latvian.mods.rhino.regexp.RegExp;

import java.util.regex.Pattern;

public interface ProbeJSEvents {

    EventGroup GROUP = EventGroup.of("ProbeJSEvents");
    EventHandler DOC_GEN = GROUP.server("generateDoc", () -> DocGenerationEventJS.class);

    Extra GLOB_PATTERN = new Extra()
            .describeType(ctx -> TypeDescJS.STRING)
            .identity()
            .transformer(o -> {
                if (o instanceof RegExp regExp) {
                    Pattern pattern = UtilsJS.parseRegex(regExp);
                    if (pattern == null) throw new AssertionError("Unable to parse regex to pattern, ???");
                    return UtilsJS.toRegexString(pattern);
                } else {
                    return VSCodeFileSavedEventJS.FILE_SYSTEM.getPathMatcher("glob:%s".formatted(o));
                }
            });

    EventHandler VSC_FILE_SAVED = GROUP.server("fileSaved", () -> VSCodeFileSavedEventJS.class).extra(GLOB_PATTERN);
}
