package moe.wolfgirl.probejs.lang.java.clazz.members;

import dev.latvian.mods.rhino.CachedMethodInfo;
import dev.latvian.mods.rhino.CachedParameters;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.lang.java.base.TypeVariableHolder;
import moe.wolfgirl.probejs.lang.java.TypeAdapter;

import java.lang.reflect.*;
import java.util.*;

public class MethodInfo extends TypeVariableHolder {
    public final String name;
    public final List<ParamInfo> params;
    public TypeInfo returnType;
    public final MethodAttributes attributes;

    public MethodInfo(String name, CachedMethodInfo methodInfo, Method original, Map<String, TypeInfo> remapper) {
        super(original.getTypeParameters(), original.getAnnotations());

        this.attributes = new MethodAttributes(original);
        this.name = name;

        Parameter[] parameters = original.getParameters();
        CachedParameters cachedParameters = methodInfo.getParameters();

        this.params = new ArrayList<>(parameters.length);
        if (cachedParameters.firstArgContext()) {
            for (int i = 1; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                TypeInfo typeInfo = cachedParameters.typeInfos().get(i - 1);
                this.params.add(new ParamInfo(parameter.getName(), typeInfo, parameter.isVarArgs()));
            }
        } else {
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                TypeInfo typeInfo = cachedParameters.typeInfos().get(i);
                this.params.add(new ParamInfo(parameter.getName(), typeInfo, parameter.isVarArgs()));
            }
        }

        this.returnType = methodInfo.getReturnType();

        for (Map.Entry<String, TypeInfo> entry : remapper.entrySet()) {
            String symbol = entry.getKey();
            TypeInfo replacement = entry.getValue();

            for (ParamInfo param : this.params) {
                param.type = TypeAdapter.consolidateType(param.type, symbol, replacement);
            }
            this.returnType = TypeAdapter.consolidateType(this.returnType, symbol, replacement);
        }
    }

    public static class MethodAttributes {
        public final boolean isStatic;
        /**
         * When this appears in a class, remember to translate its type variables because it is from an interface.
         */
        public final boolean isDefault;
        public final boolean isAbstract;

        public MethodAttributes(Method method) {
            int modifiers = method.getModifiers();
            this.isStatic = Modifier.isStatic(modifiers);
            this.isDefault = method.isDefault();
            this.isAbstract = Modifier.isAbstract(modifiers);
        }
    }
}
