package moe.wolfgirl.probejs.next.typescript.base;

import moe.wolfgirl.probejs.next.typescript.document.base.Type;

@FunctionalInterface
public interface AliasRegistrar {
    void addInputAlias(Class<?> clazz, Type type);

    // Prevent accidental messing up
    class Proxy implements AliasRegistrar {
        private final AliasRegistrar delegate;

        public Proxy(AliasRegistrar delegate) {
            this.delegate = delegate;
        }

        @Override
        public void addInputAlias(Class<?> clazz, Type type) {
            delegate.addInputAlias(clazz, type);
        }
    }
}
