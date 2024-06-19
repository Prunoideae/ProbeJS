package moe.wolfgirl.probejs.lang.snippet.parts;

public class Literal implements SnippetPart {
    private final String content;

    public Literal(String content) {
        this.content = content;
    }

    @Override
    public String format() {
        return content;
    }
}
