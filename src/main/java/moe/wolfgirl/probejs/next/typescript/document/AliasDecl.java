package moe.wolfgirl.probejs.next.typescript.document;

import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.document.base.CommentableCode;
import moe.wolfgirl.probejs.next.typescript.document.base.InputAliased;

import java.util.List;
import java.util.Set;

// export Identifier_ = TypeInfo;
public class AliasDecl extends CommentableCode {
    public final String identifier;
    public final Code typeInfo;

    public AliasDecl(String identifier, InputAliased typeInfo) {
        this.identifier = identifier;
        this.typeInfo = typeInfo;
    }


    @Override
    public Set<ClassPath> getImports() {
        return typeInfo.getImports();
    }

    @Override
    public List<String> format(int indent) {
        return List.of("%sexport %s = %s;".formatted(" ".repeat(indent), identifier, typeInfo.first()));
    }
}
