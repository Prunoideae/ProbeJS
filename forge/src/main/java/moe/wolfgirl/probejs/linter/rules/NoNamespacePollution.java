package moe.wolfgirl.probejs.linter.rules;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import moe.wolfgirl.probejs.ProbeConfig;
import moe.wolfgirl.probejs.linter.LintingWarning;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class NoNamespacePollution extends Rule {

    final Multimap<String, Pair<Integer, Path>> identifiers = ArrayListMultimap.create();

    @Override
    public void acceptFile(Path path, List<String> content) {
        if (ProbeConfig.INSTANCE.isolatedScopes.get()) {
            // scans for export ...
            for (int i = 0; i < content.size(); i++) {
                String s = content.get(i).trim();
                if (s.startsWith("export")) {
                    s = s.substring(6).trim();
                    String[] parts = s.split(" ", 2);

                }
            }
        } else {
            // or let/const/var / function
            for (int i = 0; i < content.size(); i++) {
                String s = content.get(i).trim();
                if (s.startsWith("var") || s.startsWith("let") || s.startsWith("const") || s.startsWith("function")) {

                }
            }
        }
    }

    @Override
    public List<LintingWarning> lint(Path basePath) {
        ArrayList<LintingWarning> warnings = new ArrayList<>();

        for (Map.Entry<String, Collection<Pair<Integer, Path>>> entry : identifiers.asMap().entrySet()) {
            String identifier = entry.getKey();
            Collection<Pair<Integer, Path>> paths = entry.getValue();

            if (paths.size() > 1) {
                for (Pair<Integer, Path> path : paths) {
                    warnings.add(new LintingWarning(
                            path.getSecond(),
                            LintingWarning.Level.ERROR,
                            path.getFirst(),
                            0,
                            "Duplicated declaration of %s".formatted(identifier)));
                }
            }
        }

        return warnings;
    }
}
