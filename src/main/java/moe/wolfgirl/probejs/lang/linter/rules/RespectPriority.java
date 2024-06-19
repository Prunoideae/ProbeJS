package moe.wolfgirl.probejs.lang.linter.rules;

import com.mojang.datafixers.util.Pair;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.lang.linter.LintingWarning;
import moe.wolfgirl.probejs.utils.NameUtils;

import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;

public class RespectPriority extends Rule {
    private final Map<Path, ScriptFile> files = new HashMap<>();

    @Override
    public void acceptFile(Path path, List<String> content) {
        // seek for //priority: x
        int priority = 0; // default
        for (String s : content) {
            s = s.trim();
            if (s.startsWith("//") && s.contains("priority")) {
                try {
                    priority = Integer.parseInt(s.split(":", 2)[1].trim());
                    break;
                } catch (Throwable ignore) {
                }
            }
        }

        // seek for import {} from "" or require("")
        List<Pair<Integer, Path>> depends = new ArrayList<>();

        for (int i = 0; i < content.size(); i++) {
            var s = content.get(i).trim();
            if (s.endsWith(";")) s = s.substring(0, s.length() - 1);
            if (s.startsWith("import")) {
                Matcher matcher = NameUtils.MATCH_IMPORT.matcher(s);
                if (matcher.matches()) {
                    String dependsOn = ProbeJS.GSON.fromJson(matcher.group(2), String.class);
                    if (dependsOn.startsWith("package")) continue;
                    depends.add(new Pair<>(i, path.getParent().resolve(dependsOn + ".js").toAbsolutePath().normalize()));
                }
            } else if (s.contains("require")) {
                Matcher matcher = NameUtils.MATCH_ANY_REQUIRE.matcher(s);
                if (matcher.matches()) {
                    String dependsOn = ProbeJS.GSON.fromJson(matcher.group(2), String.class);
                    if (dependsOn.startsWith("package")) continue;
                    depends.add(new Pair<>(i, path.getParent().resolve(dependsOn + ".js").toAbsolutePath().normalize()));
                }
            }
        }

        files.put(path, new ScriptFile(path, priority, content, depends));
    }

    @Override
    public List<LintingWarning> lint(Path basePath) {
        List<LintingWarning> warnings = new ArrayList<>();

        for (Map.Entry<Path, ScriptFile> entry : files.entrySet()) {
            Path path = entry.getKey();
            ScriptFile scriptFile = entry.getValue();

            for (Pair<Integer, Path> pair : scriptFile.dependencies) {
                int line = pair.getFirst();
                Path dependency = pair.getSecond();
                ScriptFile dependencyFile = files.get(dependency);
                if (dependencyFile == null) {
                    ProbeJS.LOGGER.info(path);
                    ProbeJS.LOGGER.info(dependency);
                    ProbeJS.LOGGER.info(files);
                    warnings.add(new LintingWarning(
                            path, LintingWarning.Level.WARNING,
                            line, 0,
                            "Unknown dependency: %s".formatted(basePath.relativize(dependency)))
                    );
                    continue;
                }

                if (scriptFile.compareTo(dependencyFile)) {
                    warnings.add(new LintingWarning(
                            path, LintingWarning.Level.ERROR,
                            line, 0,
                            "Required %s before it loads!".formatted(basePath.relativize(dependency))));
                }
            }
        }
        return warnings;
    }

    record ScriptFile(Path path, int priority, List<String> content,
                      List<Pair<Integer, Path>> dependencies) {

        public boolean compareTo(ScriptFile o2) {
            int priority = -Integer.compare(this.priority, o2.priority);
            if (priority == 0) priority = this.path.compareTo(o2.path);
            return priority < 0;
        }
    }
}
