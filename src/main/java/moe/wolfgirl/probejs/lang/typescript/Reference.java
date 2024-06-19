package moe.wolfgirl.probejs.lang.typescript;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;

public record Reference(ClassPath classPath, String original, String input) {
    public String getImport() {
        String importOriginal = original.equals(classPath.getName()) ? original : "%s as %s".formatted(classPath.getName(), original);
        String exportedInput = Declaration.INPUT_TEMPLATE.formatted(classPath.getName());
        String importInput = input.equals(exportedInput) ? input : "%s as %s".formatted(exportedInput, input);

        // Underscores can be recognized by using a global export
        return "import {%s, %s} from \"packages/%s\"".formatted(
                importOriginal, importInput, classPath.getTypeScriptPath()
        );
    }
}
