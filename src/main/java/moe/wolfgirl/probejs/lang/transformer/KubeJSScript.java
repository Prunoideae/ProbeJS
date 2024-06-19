package moe.wolfgirl.probejs.lang.transformer;

import moe.wolfgirl.probejs.ProbeConfig;
import moe.wolfgirl.probejs.utils.NameUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class KubeJSScript {
    public final Set<String> exportedSymbols;
    public final List<String> lines;

    public KubeJSScript(List<String> lines) {
        this.lines = new ArrayList<>(lines);
        this.exportedSymbols = new HashSet<>();
    }

    // process the import / const require
    public void processRequire() {
        for (int i = 0; i < lines.size(); i++) {
            String tLine = lines.get(i).trim();

            List<String> parts = new ArrayList<>();
            for (String s : tLine.split(";")) {
                if (s.startsWith("import")) {
                    Matcher match = NameUtils.MATCH_IMPORT.matcher(s.trim());
                    if (match.matches()) {
                        String names = match.group(1).trim();
                        String classPath = match.group(2).trim();
                        if (classPath.startsWith("\"packages")) { // package import
                            s = "let {%s} = require(%s)".formatted(names, classPath);
                        } else {
                            s = "";
                        }
                    }
                } else if (s.startsWith("const {") && s.contains("require")) {
                    Matcher matcher = NameUtils.MATCH_CONST_REQUIRE.matcher(s.trim());
                    if (matcher.matches()) {
                        String names = matcher.group(1).trim();
                        String classPath = matcher.group(2).trim();
                        if (classPath.startsWith("\"packages")) { // package import
                            s = "let {%s} = require(%s)".formatted(names, classPath);
                        } else {
                            s = "";
                        }
                    }
                }

                parts.add(s);
            }

            lines.set(i, String.join(";", parts));
        }
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
        lines.add(0, "const {%s} = (()=>{".formatted(destructed));
        lines.add("return {%s};})()".formatted(exported));
    }

    public String[] transform() {
        processRequire();
        processExport();
        // If there's no symbol to be exported, it will be global mode
        if (ProbeConfig.INSTANCE.isolatedScopes.get() && !exportedSymbols.isEmpty())
            wrapScope();

        return lines.toArray(String[]::new);
    }
}
