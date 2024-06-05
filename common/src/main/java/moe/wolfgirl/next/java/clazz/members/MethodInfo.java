package moe.wolfgirl.next.java.clazz.members;

import moe.wolfgirl.next.java.base.ClassPathProvider;
import moe.wolfgirl.next.java.base.TypeVariableHolder;
import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.java.type.TypeAdapter;
import moe.wolfgirl.next.java.type.TypeDescriptor;
import moe.wolfgirl.next.java.type.impl.VariableType;
import dev.latvian.mods.rhino.JavaMembers;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

public class MethodInfo extends TypeVariableHolder implements ClassPathProvider {
    public final String name;
    public final List<ParamInfo> params;
    public final TypeDescriptor returnType;
    public final MethodAttributes attributes;

    public MethodInfo(JavaMembers.MethodInfo methodInfo) {
        super(methodInfo.method.getTypeParameters(), methodInfo.method.getAnnotations());
        Method method = methodInfo.method;
        this.name = methodInfo.name;
        this.params = Arrays.stream(method.getParameters()).map(ParamInfo::new).collect(Collectors.toList());
        this.returnType = TypeAdapter.getTypeDescription(method.getAnnotatedReturnType());
        this.attributes = new MethodAttributes(method);
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
