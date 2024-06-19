package moe.wolfgirl.probejs.lang.linter;

import dev.latvian.mods.kubejs.KubeJSPaths;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.lang.linter.rules.NoNamespacePollution;
import moe.wolfgirl.probejs.lang.linter.rules.RespectPriority;
import moe.wolfgirl.probejs.lang.linter.rules.Rule;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Linter {

    public static final Supplier<Linter> SERVER_SCRIPT = () ->
            new Linter(KubeJSPaths.SERVER_SCRIPTS)
                    .defaultRules();
    public static final Supplier<Linter> CLIENT_SCRIPT = () ->
            new Linter(KubeJSPaths.CLIENT_SCRIPTS)
                    .defaultRules();
    public static final Supplier<Linter> STARTUP_SCRIPT = () ->
            new Linter(KubeJSPaths.STARTUP_SCRIPTS)
                    .defaultRules();

    @FunctionalInterface
    public interface RuleFactory {
        Rule get();
    }

    public static final RuleFactory[] RULES = new RuleFactory[]{
            RespectPriority::new,
            NoNamespacePollution::new
    };

    public final Path scriptPath;
    public final List<Rule> rules = new ArrayList<>();

    public Linter(Path scriptPath) {
        this.scriptPath = scriptPath;
    }

    public Linter defaultRules() {
        for (RuleFactory rule : RULES) {
            rules.add(rule.get());
        }
        return this;
    }

    public Linter exclude(Class<?>... rule) {
        for (Class<?> aClass : rule) {
            rules.removeIf(aClass::isInstance);
        }
        return this;
    }

    public List<LintingWarning> lint() throws IOException {
        ArrayList<LintingWarning> warnings = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(scriptPath)) {
            for (Path path : (Iterable<? extends Path>) stream::iterator) {
                if (!Files.isRegularFile(path)) continue;
                if (!path.toString().endsWith("js")) continue;
                List<String> content = Files.readAllLines(path);
                for (Rule rule : rules) {
                    rule.acceptFile(path, content);
                }
            }
        }

        for (Rule rule : rules) {
            warnings.addAll(rule.lint(scriptPath));
        }
        return warnings;
    }


    public static void defaultLint(Consumer<Component> report) {
        try {
            List<Component> warnings = new ArrayList<>();

            var startup = Linter.STARTUP_SCRIPT.get();
            for (LintingWarning lintingWarning : startup.lint()) {
                warnings.add(lintingWarning.defaultFormatting(startup.scriptPath));
            }

            var server = Linter.SERVER_SCRIPT.get();
            for (LintingWarning lintingWarning : server.lint()) {
                warnings.add(lintingWarning.defaultFormatting(server.scriptPath));
            }
            var client = Linter.CLIENT_SCRIPT.get();
            for (LintingWarning lintingWarning : client.lint()) {
                warnings.add(lintingWarning.defaultFormatting(client.scriptPath));
            }

            for (Component warning : warnings) {
                report.accept(warning);
            }
            if (warnings.isEmpty())
                report.accept(Component.translatable("probejs.lint_passed")
                        .kjs$green());
        } catch (Throwable e) {
            ProbeJS.LOGGER.error(e.getMessage());
        }
    }
}
