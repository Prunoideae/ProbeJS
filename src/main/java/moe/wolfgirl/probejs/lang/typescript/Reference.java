package moe.wolfgirl.probejs.lang.typescript;

import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.code.ImportInfo;

import java.util.EnumSet;
import java.util.stream.Collectors;

public record Reference(ClassPath classPath,
                        String symbol,
                        EnumSet<ImportInfo.Type> types) {
    public String getImport() {
        // FIXME: make the implementation correct
        if (types.contains(ImportInfo.Type.TYPE)) {
            types.add(ImportInfo.Type.ORIGINAL);
        }
        String original = classPath.getName();
        String names = types().stream().map(op -> original.equals(symbol) ?
                op.applyTemplate(symbol) :
                "%s as %s".formatted(
                        op.applyTemplate(original),
                        op.applyTemplate(symbol)
                )).collect(Collectors.joining(", "));

        // Underscores can be recognized by using a global export
        return "import {%s} from %s".formatted(
                names, ProbeJS.GSON.toJson(classPath.getTypeScriptPath())
        );
    }
}
