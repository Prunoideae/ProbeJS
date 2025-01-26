package moe.wolfgirl.probejs.lang.java.clazz.members;

import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.lang.java.base.TypeVariableHolder;
import moe.wolfgirl.probejs.lang.java.TypeAdapter;
import dev.latvian.mods.rhino.JavaMembers;

import java.lang.reflect.*;
import java.util.*;

public class MethodInfo extends TypeVariableHolder {
    public final String name;
    public final List<ParamInfo> params;
    public TypeInfo returnType;
    public final MethodAttributes attributes;

    public MethodInfo(JavaMembers.MethodInfo methodInfo, Map<String, TypeInfo> remapper) {
        super(methodInfo.method.getTypeParameters(), methodInfo.method.getAnnotations());
        Method method = methodInfo.method;
        this.attributes = new MethodAttributes(method);
        this.name = methodInfo.name;

        Parameter[] parameters = method.getParameters();
        this.params = new ArrayList<>(parameters.length);
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (i == 0 && Context.class.isAssignableFrom(parameter.getType())) continue;
            this.params.add(new ParamInfo(parameter));
        }

        this.returnType = TypeInfo.of(method.getGenericReturnType());

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
