package moe.wolfgirl.probejs.features.repl;

import dev.latvian.mods.kubejs.script.ScriptPack;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Scriptable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Evaluator {
    private final Context context;
    private final Scriptable topLevelScope;


    public Evaluator(ScriptPack scriptPack) {
        this.context = scriptPack.manager.context;
        this.topLevelScope = scriptPack.scope;
    }

    public Object evaluate(String command) {
        return context.evaluateString(
                topLevelScope,
                command,
                "<console>",
                1,
                null
        );
    }

    public List<String> getTopLevelVariables() {
        return Arrays.stream(topLevelScope.getIds(null))
                .filter(s -> s instanceof String)
                .map(s -> (String) s)
                .collect(Collectors.toList());
    }

    public Context getContext() {
        return context;
    }

    public Scriptable getTopLevelScope() {
        return topLevelScope;
    }
}
