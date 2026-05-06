package moe.wolfgirl.probejs.snippet;

public interface SnippetRegisterer {
    void register(Snippet snippet);

    default Snippet snippet(String name) {
        Snippet snippet = new Snippet(name);
        register(snippet);
        return snippet;
    }
}
