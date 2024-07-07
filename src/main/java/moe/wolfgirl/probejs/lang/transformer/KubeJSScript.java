package moe.wolfgirl.probejs.lang.transformer;

import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.ContextFactory;
import dev.latvian.mods.rhino.Parser;
import dev.latvian.mods.rhino.ast.*;
import moe.wolfgirl.probejs.ProbeConfig;
import moe.wolfgirl.probejs.utils.GameUtils;
import moe.wolfgirl.probejs.utils.NameUtils;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class KubeJSScript {
    private static final String PLACEHOLDER = "!@#$%^"; // placeholder to not mutate original string length

    private static final Supplier<Parser> PARSER = () -> {
        ContextFactory factory = new ContextFactory();
        Context context = factory.enter();
        return new Parser(context);
    };

    public final Set<String> exportedSymbols;
    public List<String> lines;

    public KubeJSScript(List<String> lines) {
        this.lines = new ArrayList<>(lines);
        this.exportedSymbols = new HashSet<>();
    }

    // process the const require
    public void processRequire() {
        String joined = String.join("\n", lines);
        AstRoot root = PARSER.get().parse(String.join("\n", lines), "script.js", 0);
        List<Integer[]> cuts = new ArrayList<>();

        for (AstNode statement : root.getStatements()) {
            // declaring
            if (statement instanceof VariableDeclaration declaration) {
                var variables = declaration.getVariables();
                for (VariableInitializer variable : variables) {
                    if (variable.getInitializer() instanceof FunctionCall call &&
                            call.getTarget() instanceof Name name) {
                        // used require()
                        if (name.getIdentifier().equals("require")) {
                            var loaded = call.getArguments().getFirst();
                            if (loaded instanceof StringLiteral literal) {
                                // is java package, transform if it's const
                                if (literal.getValue().startsWith("packages/")) {
                                    if (declaration.isConst()) {
                                        joined = NameUtils.replaceRegion(joined,
                                                statement.getPosition(),
                                                statement.getPosition() + statement.getLength(),
                                                "const ",
                                                PLACEHOLDER
                                        );
                                    }
                                } else {
                                    // not java package, cut it
                                    cuts.add(new Integer[]{statement.getPosition(), statement.getPosition() + statement.getLength()});
                                }
                            }
                        }
                    }
                }
            }
        }

        cuts.sort(Comparator.comparing(p -> p[0]));
        joined = NameUtils.cutOffStartEnds(joined, cuts);

        joined = joined.replace(PLACEHOLDER, "let ");
        lines = new ArrayList<>(List.of(joined.split("\\n")));
    }

    // scans for the export function/let/var/const
    public void processExport() {
        for (int i = 0; i < lines.size(); i++) {
            String tLine = lines.get(i).trim();
            if (tLine.startsWith("export")) {
                tLine = tLine.substring(6).trim();
                String[] parts = tLine.split(" ", 2);

                var identifier = switch (parts[0]) {
                    case "function" -> parts[1].split("\\(")[0];
                    case "var", "let", "const" -> parts[1].split(" ")[0];
                    default -> null;
                };

                if (identifier == null) continue;
                exportedSymbols.add(identifier);
            }
            lines.set(i, tLine);
        }
    }

    // Wraps the code in let {...} = (()=>{...;return {...};})()
    public void wrapScope() {
        String exported = exportedSymbols.stream()
                .map(s -> "%s: %s".formatted(s, s))
                .collect(Collectors.joining(", "));
        String destructed = String.join(", ", exportedSymbols);
        lines.addFirst("const {%s} = (()=>{".formatted(destructed));
        lines.add("return {%s};})()".formatted(exported));
    }

    public String[] transform() {
        try {
            processExport();
            processRequire();
            // If there's no symbol to be exported, it will be global mode
            if (ProbeConfig.INSTANCE.isolatedScopes.get() && !exportedSymbols.isEmpty())
                wrapScope();
        } catch (Throwable t) {
            GameUtils.logException(t);
        }

        return lines.toArray(String[]::new);
    }
}
