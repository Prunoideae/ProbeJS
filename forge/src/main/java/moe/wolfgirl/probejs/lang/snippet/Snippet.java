package moe.wolfgirl.probejs.lang.snippet;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import moe.wolfgirl.probejs.lang.snippet.parts.*;
import moe.wolfgirl.probejs.utils.JsonUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Snippet {

    public final String name;
    final List<String> prefixes = new ArrayList<>();
    final List<List<SnippetPart>> allParts = new ArrayList<>();
    String description;

    public Snippet(String name) {
        this.name = name;
        allParts.add(new ArrayList<>());
    }

    public Snippet prefix(String prefix) {
        prefixes.add(prefix);
        return this;
    }

    public Snippet newline() {
        allParts.add(new ArrayList<>());
        return this;
    }

    public Snippet description(String description) {
        this.description = description;
        return this;
    }

    public Snippet literal(String content) {
        getRecent().add(new Literal(content));
        return this;
    }

    public Snippet tabStop() {
        return tabStop(-1);
    }

    public Snippet tabStop(int enumeration) {
        return tabStop(enumeration, null);
    }

    public Snippet tabStop(int enumeration, String defaultValue) {
        var stop = new TabStop(defaultValue);
        stop.enumeration = enumeration;
        getRecent().add(stop);
        return this;
    }


    public Snippet choices(Collection<String> choices) {
        return choices(-1, choices);
    }

    public Snippet choices(int enumeration, Collection<String> choices) {
        var choice = new Choice(choices);
        choice.enumeration = enumeration;
        getRecent().add(choice);
        return this;
    }

    public Snippet variable(Variable variable) {
        getRecent().add(variable);
        return this;
    }

    public <T> Snippet registry(ResourceKey<Registry<T>> registry) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return this;
        RegistryAccess access = server.registryAccess();

        var reg = access.registry(registry).orElse(null);
        if (reg == null) return this;
        return choices(reg.keySet().stream().map(ResourceLocation::toString).collect(Collectors.toSet()));
    }

    private List<SnippetPart> getRecent() {
        return allParts.get(allParts.size() - 1);
    }

    public JsonObject compile() {

        // Reindex everything
        Set<Integer> indexes = new IntArraySet(256);
        List<Enumerable> toBeIndexed = new ArrayList<>(64);

        for (List<SnippetPart> parts : allParts) {
            for (SnippetPart part : parts) {
                if (part instanceof Enumerable enumerable) {
                    if (enumerable.enumeration == -1) toBeIndexed.add(enumerable);
                    else indexes.add(enumerable.enumeration);
                }
            }
        }

        int start = 1;
        for (Enumerable enumerable : toBeIndexed) {
            while (indexes.contains(start)) start++;
            enumerable.enumeration = start;
            start++;
        }

        List<String> lines = new ArrayList<>();
        for (List<SnippetPart> parts : allParts) {
            StringBuilder content = new StringBuilder();
            for (SnippetPart part : parts) {
                content.append(part.format());
            }
            lines.add(content.toString());
        }


        // Append the $0 at the end if no other end is found
        if (!indexes.contains(0)) {
            int last = lines.size() - 1;
            lines.set(last, lines.get(last) + "$0");
        }

        JsonObject object = new JsonObject();
        if (prefixes.isEmpty()) {
            throw new RuntimeException("Must have at least one prefix for the snippet %s!".formatted(name));
        }

        object.add("prefix", JsonUtils.asStringArray(prefixes));
        object.add("body", JsonUtils.asStringArray(lines));
        if (description != null) object.addProperty("description", description);

        return object;
    }

    public List<String> getPrefixes() {
        return prefixes;
    }
}
