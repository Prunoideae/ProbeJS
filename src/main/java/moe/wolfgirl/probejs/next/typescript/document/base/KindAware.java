package moe.wolfgirl.probejs.next.typescript.document.base;

public interface KindAware {
    void setKind(Kind kind);

    enum Kind {
        CLASS, INTERFACE, NAMESPACE
    }
}
