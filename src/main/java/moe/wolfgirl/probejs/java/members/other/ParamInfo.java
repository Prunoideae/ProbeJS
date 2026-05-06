package moe.wolfgirl.probejs.java.members.other;

import dev.latvian.mods.rhino.CachedExecutableInfo;
import dev.latvian.mods.rhino.CachedParameters;
import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.java.members.ClassInfo;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.lang.reflect.TypeVariable;
import java.util.*;

public record ParamInfo(String name, TypeInfo typeInfo, boolean varArgs) implements ClassProvider {

    public static List<ParamInfo> resolve(CachedExecutableInfo executableInfo, Map<String, TypeInfo> typeRemap) {
        Executable original = executableInfo.getCached();

        // Prevents type variables of the current method from being remapped
        var typeRemapNoLocal = new HashMap<>(typeRemap);
        for (TypeVariable<?> typeVariable : original.getTypeParameters()) {
            typeRemapNoLocal.remove(typeVariable.getName());
        }

        Parameter[] parameters = original.getParameters();
        CachedParameters cachedParameters = executableInfo.getParameters();

        int start = 0, offset = 0;
        if (cachedParameters.firstArgContext()) {
            start = 1;
            offset = 1;
        } else if (parameters.length - cachedParameters.count() == 1) {
            // This is mostly because the use of non-static inner class,
            // which has a hidden first parameter for the outer class instance
            start = 1;
            offset = 1;
        }

        List<ParamInfo> result = new ArrayList<>();
        for (int i = start; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            TypeInfo typeInfo = cachedParameters.typeInfos().get(i - offset);
            typeInfo = ClassInfo.remapType(typeInfo, typeRemapNoLocal);
            result.add(new ParamInfo(parameter.getName(), typeInfo, parameter.isVarArgs()));
        }

        return result;
    }

    @Override
    public Collection<Class<?>> getReferredClasses() {
        return typeInfo.getContainedComponentClasses();
    }
}
