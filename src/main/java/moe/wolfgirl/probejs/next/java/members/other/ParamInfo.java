package moe.wolfgirl.probejs.next.java.members.other;

import dev.latvian.mods.rhino.CachedExecutableInfo;
import dev.latvian.mods.rhino.CachedParameters;
import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.next.java.members.ClassInfo;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public record ParamInfo(String name, TypeInfo typeInfo, boolean varArgs) implements ClassProvider {

    public static List<ParamInfo> resolve(CachedExecutableInfo executableInfo, Map<String, TypeInfo> typeRemap) {
        Executable original = executableInfo.getCached();

        Parameter[] parameters = original.getParameters();
        CachedParameters cachedParameters = executableInfo.getParameters();

        int start = 0, offset = 0;
        if (cachedParameters.firstArgContext()) {
            start = 1;
            offset = 1;
        }

        List<ParamInfo> result = new ArrayList<>();
        for (int i = start; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            TypeInfo typeInfo = cachedParameters.typeInfos().get(i - offset);
            typeInfo = ClassInfo.remapType(typeInfo, typeRemap);
            result.add(new ParamInfo(parameter.getName(), typeInfo, parameter.isVarArgs()));
        }

        return result;
    }

    @Override
    public Collection<Class<?>> getReferredClasses() {
        return typeInfo.getContainedComponentClasses();
    }
}
