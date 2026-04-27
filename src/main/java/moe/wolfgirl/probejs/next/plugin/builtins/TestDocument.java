package moe.wolfgirl.probejs.next.plugin.builtins;

import dev.latvian.mods.kubejs.script.ScriptType;
import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.next.typescript.Documents;
import moe.wolfgirl.probejs.next.typescript.base.DocumentRegistrar;
import moe.wolfgirl.probejs.next.typescript.document.Types;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.document.members.MethodDecl;
import moe.wolfgirl.probejs.next.typescript.document.members.ParamDecl;

import java.util.List;
import java.util.Set;

public class TestDocument extends ProbeJSPlugin {
    @Override
    public void transformClass(Documents.ClassDocument document) {
        var params = document.document().members.stream()
                .filter(c -> c instanceof MethodDecl)
                .map(c -> (MethodDecl) c)
                .findFirst()
                .map(methodDecl -> methodDecl.params)
                .orElse(null);
        if (params == null || params.isEmpty()) return;
        params.set(0, new ParamDecl("foo", Types.clazz(ClassPath.special("foo.Foo"))));
    }

    @Override
    public void addSpecialDocuments(DocumentRegistrar registrar) {
        registrar.addDocument(ClassPath.special("foo.Foo"), new Foo());
        registrar.addGlobal(ClassPath.special("bar.Bar"), new Bar());
    }


    static class Foo extends Code {

        @Override
        public Set<ClassPath> getImports() {
            return Set.of();
        }

        @Override
        public List<String> format(int indent) {
            return List.of("export class Foo {}");
        }
    }

    static class Bar extends Code {

        @Override
        public Set<ClassPath> getImports() {
            return Set.of();
        }

        @Override
        public List<String> format(int indent) {
            return List.of("function bar(): void;");
        }
    }
}
