package moe.wolfgirl.probejs.lang.linter.rules;

import moe.wolfgirl.probejs.lang.linter.LintingWarning;

import java.nio.file.Path;
import java.util.List;

public abstract class Rule {

    /**
     * Accepts a file loaded in the linter.
     *
     * @param path    the **absolute** path of the script file
     * @param content the file content
     */
    public abstract void acceptFile(Path path, List<String> content);

    /**
     * Get potential problems / info returned by this rule
     *
     * @return a list of warnings
     */
    public abstract List<LintingWarning> lint(Path basePath);
}
