package moe.wolfgirl.probejs.next.typescript.document;

import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.typescript.document.base.CommentableCode;
import moe.wolfgirl.probejs.next.typescript.document.base.Type;

import java.util.List;
import java.util.Set;

// export Identifier_ = TypeInfo;
public class AliasDecl extends CommentableCode {
    public final ClassPath identifier;
    public final Type typeInfo;

    public AliasDecl(ClassPath identifier, Type typeInfo) {
        this.identifier = identifier;
        this.typeInfo = typeInfo;
    }


    @Override
    public Set<ClassPath> getImports() {
        return typeInfo.getImports();
    }

    @Override
    public List<String> format(int indent) {
        return List.of("%sexport type %s_ = %s;".formatted(" ".repeat(indent), identifier.getClassName(), typeInfo.first()));
    }
}
