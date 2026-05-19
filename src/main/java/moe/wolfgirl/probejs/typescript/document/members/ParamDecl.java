package moe.wolfgirl.probejs.typescript.document.members;

import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.Type;
import moe.wolfgirl.probejs.utils.NameUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

// A parameter declaration in method or constructor
// param1: Type1
// ...args: Type2[]
public class ParamDecl extends Code {

    public String name;
    public Type typeInfo;
    public boolean isRest;
    public boolean optional = false;

    public ParamDecl(String name, Type typeInfo, boolean isRest) {
        this.name = name;
        this.typeInfo = typeInfo;
        this.isRest = isRest;
    }

    public ParamDecl(String name, Type typeInfo, boolean isRest, boolean optional) {
        this.name = name;
        this.typeInfo = typeInfo;
        this.isRest = isRest;
        this.optional = optional;
    }

    public ParamDecl(String name, Type typeInfo) {
        this(name, typeInfo, false);
    }

    @Override
    public Set<ClassPath> getImports() {
        return typeInfo.getImports();
    }

    @Override
    public List<String> format(int indent) {
        var name = NameUtils.isNameSafe(this.name) ? this.name : "_" + this.name;
        return List.of("%s%s%s: %s".formatted(isRest ? "..." : "", name, optional ? "?" : "", typeInfo.first()));
    }

    @Override
    public void setResolvedSymbols(Map<ClassPath, String> resolvedSymbols) {
        super.setResolvedSymbols(resolvedSymbols);
        typeInfo.setResolvedSymbols(resolvedSymbols);
    }
}
