package moe.wolfgirl.probejs.misc.llm;

import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.typescript.document.base.Code;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Represents a @note_to_llm comment block in the source code.
 * <br>
 * Example:
 * <pre>
 * class Example {
 *     /**
 *      * @note_to_llm To use this class, use `createExample` from ExampleUtils bindings instead of the constructor.
 *      *      *\/
 *     public Example() {
 *     }
 * }
 * </pre>
 */
public class CommentBlock extends Code {
    private final List<String> lines; // Not commented lines, need to add comment syntax, also first line need a @note_to_llm prefix

    public CommentBlock(List<String> lines) {
        this.lines = lines;
    }

    @Override
    public Set<ClassPath> getImports() {
        return Set.of();
    }

    @Override
    public List<String> format(int indent) {
        String indentStr = " ".repeat(indent);
        List<String> formatted = new ArrayList<>();
        formatted.add(indentStr + "/**");
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (i == 0) {
                formatted.add(indentStr + " * @note_to_llm " + line);
            } else {
                formatted.add(indentStr + " * " + line);
            }
        }
        formatted.add(indentStr + " */");
        return formatted;
    }
}
