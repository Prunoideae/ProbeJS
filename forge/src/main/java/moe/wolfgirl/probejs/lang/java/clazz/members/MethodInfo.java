package moe.wolfgirl.probejs.lang.java.clazz.members;

import moe.wolfgirl.probejs.lang.java.base.ClassPathProvider;
import moe.wolfgirl.probejs.lang.java.base.TypeVariableHolder;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.java.type.TypeAdapter;
import moe.wolfgirl.probejs.lang.java.type.TypeDescriptor;
import moe.wolfgirl.probejs.lang.java.type.impl.VariableType;
import dev.latvian.mods.rhino.JavaMembers;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.stream.Collectors;

public class MethodInfo extends TypeVariableHolder implements ClassPathProvider {
    public final String name;
    public final List<ParamInfo> params;
    public TypeDescriptor returnType;
    public final MethodAttributes attributes;

    public MethodInfo(JavaMembers.MethodInfo methodInfo, Map<TypeVariable<?>, Type> remapper) {
        super(methodInfo.method.getTypeParameters(), methodInfo.method.getAnnotations());
        Method method = methodInfo.method;
        this.attributes = new MethodAttributes(method);
        this.name = methodInfo.name;
        this.params = Arrays.stream(method.getParameters())
                .map(ParamInfo::new)
                .collect(Collectors.toList());
        this.returnType = TypeAdapter.getTypeDescription(method.getAnnotatedReturnType());

        for (Map.Entry<TypeVariable<?>, Type> entry : remapper.entrySet()) {
            TypeVariable<?> symbol = entry.getKey();
            TypeDescriptor replacement = TypeAdapter.getTypeDescription(entry.getValue());

            for (ParamInfo param : this.params) {
                param.type = TypeAdapter.consolidateType(param.type, symbol.getName(), replacement);
            }
            this.returnType = TypeAdapter.consolidateType(this.returnType, symbol.getName(), replacement);
        }
    }

    @Override
    public Collection<ClassPath> getClassPaths() {
        Set<ClassPath> paths = new HashSet<>();
        for (ParamInfo param : params) {
            paths.addAll(param.getClassPaths());
        }
        paths.addAll(returnType.getClassPaths());
        for (VariableType variableType : variableTypes) {
            paths.addAll(variableType.getClassPaths());
        }
        return paths;
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
