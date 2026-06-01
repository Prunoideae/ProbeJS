package moe.wolfgirl.probejs.typescript.document.base;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class CommentableCode extends Code {
    private final List<String> comments = new ArrayList<>();

    public void addComments(String... comments) {
        for (String comment : comments) {
            this.comments.addAll(List.of(comment.strip().split("\\n")));
        }
    }

    public void addComments(List<String> comments) {
        for (String comment : comments) {
            this.comments.addAll(List.of(comment.strip().split("\\n")));
        }
    }

    public boolean hasComments() {
        return !comments.isEmpty();
    }

    // //...
    // //...
    public List<String> formatDoubleDash(int indent) {
        List<String> formatted = new ArrayList<>();
        for (String comment : comments) {
            formatted.add("%s// %s".formatted(" ".repeat(indent), comment));
        }
        formatted.addAll(format(indent));
        return formatted;
    }

    // /**
    //  * ...
    //  * ...
    //  */
    public List<String> formatSlashStar(int indent) {
        List<String> formatted = new ArrayList<>();
        if (!comments.isEmpty()) {
            formatted.add("%s/**".formatted(" ".repeat(indent)));
            for (String comment : comments) {
                formatted.add("%s * %s".formatted(" ".repeat(indent), comment));
            }
            formatted.add("%s */".formatted(" ".repeat(indent)));
        }
        formatted.addAll(format(indent));
        return formatted;
    }

    public static List<String> format(Code code, int indent) {
        if (code instanceof CommentableCode commentableCode) {
            return commentableCode.formatSlashStar(indent);
        } else {
            return code.format(indent);
        }
    }

    /**
     * Streaming version of {@link #format(Code, int)}: writes JSDoc comments (if any)
     * followed by the formatted code lines directly to the writer.
     */
    public static void writeTo(Code code, BufferedWriter writer, int indent) throws IOException {
        if (code instanceof CommentableCode commentableCode && commentableCode.hasComments()) {
            String pad = " ".repeat(indent);
            writer.write(pad + "/**");
            writer.write("\n");
            for (String comment : commentableCode.comments) {
                writer.write(pad + " * " + comment);
                writer.write("\n");
            }
            writer.write(pad + " */");
            writer.write("\n");
        }
        code.writeTo(writer, indent);
    }
}
