package moe.wolfgirl.probejs;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import moe.wolfgirl.probejs.events.SnippetGenerationEventJS;
import moe.wolfgirl.probejs.events.TypeAssignmentEventJS;
import moe.wolfgirl.probejs.events.TypingModificationEventJS;

public interface ProbeEvents {
    EventGroup GROUP = EventGroup.of("ProbeEvents");
    EventHandler ASSIGN_TYPE = ProbeEvents.GROUP.client("assignType", () -> TypeAssignmentEventJS.class);
    EventHandler MODIFY_DOC = ProbeEvents.GROUP.client("modifyClass", () -> TypingModificationEventJS.class);
    EventHandler SNIPPETS = ProbeEvents.GROUP.client("snippets", () -> SnippetGenerationEventJS.class);
}
