package moe.wolfgirl.probejs.legacy.events;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.EventTargetType;
import dev.latvian.mods.kubejs.event.TargetedEventHandler;
import dev.latvian.mods.rhino.type.TypeInfo;

public interface ProbeEvents {
    EventGroup GROUP = EventGroup.of("ProbeEvents");
    EventHandler ASSIGN_TYPE = ProbeEvents.GROUP.client("assignType", () -> TypeAssignmentEventJS.class);
    EventHandler MODIFY_DOC = ProbeEvents.GROUP.client("modifyClass", () -> TypingModificationEventJS.class);
    EventHandler SNIPPETS = ProbeEvents.GROUP.client("snippets", () -> SnippetGenerationEventJS.class);

    EventTargetType<String> SCRIPT_NAME = EventTargetType.create(String.class).transformer(o -> {
                if (o == null) return null;
                var s = o.toString();
                return s.isBlank() ? null : s;
            }).describeType(TypeInfo.STRING)
            .validator(o -> {
                if (o == null) return false;
                var s = o.toString();
                return !s.isBlank() && s.matches("^[a-z0-9_-]+$");
            });

    TargetedEventHandler<String> CODEGEN = ProbeEvents.GROUP.client("codegen", () -> CodeGenerationEventJS.class)
            .requiredTarget(SCRIPT_NAME);
}
