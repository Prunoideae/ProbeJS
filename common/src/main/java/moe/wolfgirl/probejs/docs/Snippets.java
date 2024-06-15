package moe.wolfgirl.probejs.docs;

import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.snippet.SnippetDump;
import moe.wolfgirl.probejs.snippet.parts.Variable;

public class Snippets extends ProbeJSPlugin {
    @Override
    public void addVSCodeSnippets(SnippetDump dump) {
        dump.snippet("uuid")
                .prefix("#uuid")
                .description("Generates a random version 4 UUID.")
                .literal("\"")
                .variable(Variable.UUID)
                .literal("\"");

        defineHeader(dump, "priority", "0");
        defineHeader(dump, "ignored", "false");
        defineHeader(dump, "packmode", null);
        defineHeader(dump, "requires", null);
    }

    private static void defineHeader(SnippetDump dump, String symbol, String defaultValue) {
        dump.snippet(symbol)
                .prefix("#" + symbol)
                .description("Creates the file header for %s".formatted(symbol))
                .literal("// %s: ".formatted(symbol))
                .tabStop(0, defaultValue);
    }
}
