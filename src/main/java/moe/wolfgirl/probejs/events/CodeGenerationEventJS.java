package moe.wolfgirl.probejs.events;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CodeGenerationEventJS implements KubeEvent {
    private final List<String> content = new ArrayList<>();

    public void add(String... lines) {
        content.addAll(List.of(lines));
    }

    @HideFromJS
    public List<String> getContent() {
        return content;
    }

    public static class ScriptArgument implements ArgumentType<String> {
        public static final SimpleCommandExceptionType NO_SCRIPT_FOUND = new SimpleCommandExceptionType(Component.translatable("argument.script.notfound"));

        private static List<String> findScriptTargets() {
            List<String> eventTargets = new ArrayList<>();
            ProbeEvents.CODEGEN.forEachListener(ScriptType.CLIENT, container -> {
                eventTargets.add(container.target.toString());
            });

            return eventTargets;
        }

        @Override
        public String parse(StringReader reader) throws CommandSyntaxException {
            List<String> targets = findScriptTargets();
            var curr = reader.getString();
            if (!targets.contains(curr)) throw NO_SCRIPT_FOUND.create();
            return curr;
        }

        @Override
        public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
            if (context.getSource() instanceof SharedSuggestionProvider suggestionProvider) {
                for (String script : findScriptTargets()) {
                    if (script.startsWith(builder.getRemaining())) {
                        builder.suggest(script);
                    }
                }
                return builder.buildFuture();
            } else {
                return Suggestions.empty();
            }
        }
    }
}
