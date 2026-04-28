package moe.wolfgirl.probejs.next.typescript.document;

import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.typescript.document.base.CommentableCode;
import moe.wolfgirl.probejs.next.typescript.document.base.Type;

import java.util.List;
import java.util.Set;

// export type Identifier = TypeInfo;
public class TypeDecl extends CommentableCode {
    public final ClassPath identifier;
    public final Type typeInfo;
    public final boolean export;

    public TypeDecl(ClassPath identifier, Type typeInfo, boolean export) {
        this.identifier = identifier;
        this.typeInfo = typeInfo;
        this.export = export;
    }

    public TypeDecl(ClassPath identifier, Type typeInfo) {
        this(identifier, typeInfo, true);
    }


    @Override
    public Set<ClassPath> getImports() {
        return typeInfo.getImports();
    }

    @Override
    public List<String> format(int indent) {
        return List.of("%s%s type %s = %s;".formatted(
                " ".repeat(indent),
                export ? "export " : "",
                identifier.getClassName(), typeInfo.first()
        ));
    }
}
