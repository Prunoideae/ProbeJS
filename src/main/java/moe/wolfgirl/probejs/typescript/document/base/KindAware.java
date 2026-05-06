package moe.wolfgirl.probejs.typescript.document.base;

public interface KindAware {
    void setKind(Kind kind);

    boolean shouldAppear(Kind kind);

    enum Kind {
        CLASS, INTERFACE, NAMESPACE
    }
}
