package moe.wolfgirl.probejs.typescript.base;

import moe.wolfgirl.probejs.ClassPath;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.Type;

public interface DocumentRegistrar extends AliasRegistrar {
    void addGlobal(ClassPath classPath, Code code);

    void addDocument(ClassPath classPath, Code code);

    class Proxy implements DocumentRegistrar {
        private final DocumentRegistrar delegate;

        public Proxy(DocumentRegistrar delegate) {
            this.delegate = delegate;
        }

        @Override
        public void addGlobal(ClassPath classPath, Code code) {
            delegate.addGlobal(classPath, code);
        }

        @Override
        public void addDocument(ClassPath classPath, Code code) {
            delegate.addDocument(classPath, code);
        }

        @Override
        public void addInputAlias(ClassPath classPath, Type type) {
            delegate.addInputAlias(classPath, type);
        }
    }
}
