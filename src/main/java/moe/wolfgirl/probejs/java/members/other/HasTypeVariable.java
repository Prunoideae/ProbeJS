package moe.wolfgirl.probejs.java.members.other;

import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.type.VariableTypeInfo;

import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.List;

public interface HasTypeVariable {
    TypeVariable<?>[] getTypeVariables();

    default List<VariableTypeInfo> asVariableTypeInfos() {
        return Arrays.stream(getTypeVariables()).map(TypeInfo::of).toList();
    }
}
