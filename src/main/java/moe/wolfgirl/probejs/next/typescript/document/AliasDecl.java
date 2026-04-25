package moe.wolfgirl.probejs.next.typescript.document;

import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.document.base.InputAlias;

import java.util.List;

// export Identifier_ = TypeInfo;
public class AliasDecl extends Code {
    public final String identifier;
    public final InputAlias typeInfo;

    public AliasDecl(String identifier, InputAlias typeInfo) {
        this.identifier = identifier;
        this.typeInfo = typeInfo;
    }


    @Override
    public List<ClassPath> getImports() {
        return typeInfo.getImports();
    }

    @Override
    public List<String> format(int indent) {
        return List.of("export type %s_ = %s;".formatted(identifier, typeInfo.first()));
    }
}
