package moe.wolfgirl.probejs.legacy.lang.java.clazz.members;

import dev.latvian.mods.rhino.CachedConstructorInfo;
import dev.latvian.mods.rhino.CachedParameters;
import dev.latvian.mods.rhino.type.TypeInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.*;

public class ConstructorInfo extends TypeVariableHolder {

    public final List<ParamInfo> params;

    public ConstructorInfo(CachedConstructorInfo constructor, Constructor<?> original) {
        super(original.getTypeParameters(), original.getAnnotations());
        this.params = new ArrayList<>();

        CachedParameters parameters = constructor.getParameters();
        Parameter[] originalParams = original.getParameters();

        if (parameters.firstArgContext()) {
            for (int i = 1; i < originalParams.length; i++) {
                Parameter parameter = originalParams[i];
                TypeInfo typeInfo = parameters.typeInfos().get(i - 1);
                params.add(new ParamInfo(parameter.getName(), typeInfo, parameter.isVarArgs()));
            }
        } else {
            for (int i = 0; i < originalParams.length; i++) {
                Parameter parameter = originalParams[i];
                TypeInfo typeInfo = parameters.typeInfos().get(i);
                params.add(new ParamInfo(parameter.getName(), typeInfo, parameter.isVarArgs()));
            }
        }
    }

}
