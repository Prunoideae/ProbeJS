package moe.wolfgirl.probejs.typescript.base;

import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.typescript.document.Types;
import moe.wolfgirl.probejs.typescript.document.base.Type;


public interface AliasRegistrar {
    void addInputAlias(ClassPath classPath, Type type);


    default void addInputAlias(Class<?> from, Type to) {
        addInputAlias(new ClassPath(from), to);
    }

    default void addInputAlias(ClassPath from, Class<?> to) {
        addInputAlias(from, Types.clazz(to));
    }

    default void addInputAlias(Class<?> from, Class<?> to) {
        addInputAlias(new ClassPath(from), Types.clazz(to).asInput());
    }

    // Prevent accidental messing up
    class Proxy implements AliasRegistrar {
        private final AliasRegistrar delegate;

        public Proxy(AliasRegistrar delegate) {
            this.delegate = delegate;
        }

        @Override
        public void addInputAlias(ClassPath classPath, Type type) {
            delegate.addInputAlias(classPath, type);
        }
    }
}
