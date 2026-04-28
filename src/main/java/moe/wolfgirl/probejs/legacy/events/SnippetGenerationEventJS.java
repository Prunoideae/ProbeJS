package moe.wolfgirl.probejs.legacy.events;

import dev.latvian.mods.kubejs.event.KubeEvent;
import moe.wolfgirl.probejs.next.snippet.Snippet;
import moe.wolfgirl.probejs.legacy.lang.snippet.SnippetDump;

import java.util.function.Consumer;

public class SnippetGenerationEventJS implements KubeEvent {

    private final SnippetDump dump;

    public SnippetGenerationEventJS(SnippetDump dump) {
        this.dump = dump;
    }

    public void create(String name, Consumer<Snippet> handler) {
        handler.accept(dump.snippet(name));
    }
}
