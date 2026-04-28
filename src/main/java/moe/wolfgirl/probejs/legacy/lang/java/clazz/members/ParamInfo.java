package moe.wolfgirl.probejs.legacy.lang.java.clazz.members;

import dev.latvian.mods.rhino.type.TypeInfo;

public class ParamInfo {
    public String name;
    public TypeInfo type;
    public final boolean varArgs;

    public ParamInfo(String name, TypeInfo type, boolean varArgs) {
        this.name = name;
        this.type = type;
        this.varArgs = varArgs;
    }
}
