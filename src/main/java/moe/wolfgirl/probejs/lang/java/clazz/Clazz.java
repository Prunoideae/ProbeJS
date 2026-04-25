package moe.wolfgirl.probejs.lang.java.clazz;

import dev.latvian.mods.rhino.CachedClassInfo;
import dev.latvian.mods.rhino.CachedClassStorage;
import dev.latvian.mods.rhino.type.TypeInfo;
import dev.latvian.mods.rhino.type.VariableTypeInfo;
import dev.latvian.mods.rhino.util.HideFromJS;
import moe.wolfgirl.probejs.lang.java.clazz.members.TypeVariableHolder;
import moe.wolfgirl.probejs.lang.java.clazz.members.ConstructorInfo;
import moe.wolfgirl.probejs.lang.java.clazz.members.FieldInfo;
import moe.wolfgirl.probejs.lang.java.clazz.members.MethodInfo;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

public class Clazz extends TypeVariableHolder {

    @HideFromJS
    public final Class<?> original;
    public final ClassPath classPath;
    public final List<ConstructorInfo> constructors;
    public final List<FieldInfo> fields;
    public final List<MethodInfo> methods;
    @Nullable
    public final TypeInfo superClass;
    public final List<TypeInfo> interfaces;
    public final ClassAttribute attribute;
    public int recursionDepth;

    public Clazz(Class<?> clazz) {
        super(clazz.getTypeParameters(), clazz.getAnnotations());

        CachedClassInfo classInfo = CachedClassStorage.GLOBAL_PUBLIC.get(clazz);

        this.original = clazz;
        this.classPath = new ClassPath(clazz);
        this.constructors = classInfo.getConstructors()
                .stream()
                .map(constructorInfo -> new ConstructorInfo(constructorInfo, constructorInfo.getCached()))
                .collect(Collectors.toList());
        Set<String> names = new HashSet<>();

        this.methods = classInfo.getAccessibleMethods(false)
                .stream()
                .peek(m -> names.add(m.getName()))
                // .filter(m -> !m.method.isSynthetic())
                .filter(m -> !hasIdenticalParentMethodAndEnsureNotDirectlyImplementsInterfaceSinceTypeScriptDoesNotHaveInterfaceAtRuntimeInTypeDeclarationFilesJustBecauseItSucks(m.getInfo().getCached(), clazz))
                .map(method -> {
                    Map<String, TypeInfo> replacement = getGenericTypeReplacementForParentInterfaceMethodsJustBecauseJavaDoNotKnowToReplaceThemWithGenericArgumentsOfThisClass(clazz, method.getInfo().getCached());
                    return new MethodInfo(method.getName(), method.getInfo(), method.getInfo().getCached(), replacement);
                })
                .collect(Collectors.toList());
        this.fields = classInfo.getAccessibleFields(false)
                .stream()
                .filter(f -> !names.contains(f.getName()))
                .map(fieldInfo -> new FieldInfo(fieldInfo.getName(), fieldInfo.getInfo(), fieldInfo.getInfo().getCached()))
                .collect(Collectors.toList());


        if (clazz.getSuperclass() != Object.class) {
            this.superClass = TypeInfo.of(clazz.getGenericSuperclass());
        } else {
            this.superClass = null;
        }
        this.interfaces = Arrays.stream(clazz.getGenericInterfaces())
                .map(TypeInfo::of)
                .collect(Collectors.toList());
        this.attribute = new ClassAttribute(clazz);
        this.recursionDepth = 0;
    }

    @Override
    public int hashCode() {
        return classPath.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Clazz clazz = (Clazz) o;
        return Objects.equals(classPath, clazz.classPath);
    }

    /**
     * 天生万物以养民，民无一善可报天。
     * 不知蝗蠹遍天下，苦尽苍生尽王臣。
     * 人之生矣有贵贱，贵人长为天恩眷。
     * 人生富贵总由天，草民之穷由天谴。
     * 忽有狂徒夜磨刀，帝星飘摇荧惑高。
     * 翻天覆地从今始，杀人何须惜手劳。
     * 不忠之人曰可杀！不孝之人曰可杀！
     * 不仁之人曰可杀！不义之人曰可杀！
     * 不礼不智不信人，大西王曰杀杀杀！
     * 我生不为逐鹿来，都门懒筑黄金台，
     * 状元百官都如狗，总是刀下觳觫材。
     * 传令麾下四王子，破城不须封刀匕。
     * 山头代天树此碑，逆天之人立死跪亦死！
     */
    private static boolean hasIdenticalParentMethodAndEnsureNotDirectlyImplementsInterfaceSinceTypeScriptDoesNotHaveInterfaceAtRuntimeInTypeDeclarationFilesJustBecauseItSucks(Method method, Class<?> clazz) {
        Class<?> parent = clazz.getSuperclass();
        if (parent == null)
            return false;
        while (parent != null && !parent.isInterface()) {
            try {
                Method parentMethod = parent.getMethod(method.getName(), method.getParameterTypes());
                // Check if the generic return type is the same
                return parentMethod.equals(method);
            } catch (NoSuchMethodException e) {
                parent = parent.getSuperclass();
            }
        }
        return false;
    }

    /**
     * 我一直看着你👁👁
     * 当你在寂静的深夜独自行走👁👁
     * 感觉到背后幽幽的目光直流冷汗👁👁
     * 转头却空空荡荡时👁👁
     * 那是我在看着你👁👁
     * 我会一直看着你👁👁
     * 我不会干什么👁👁
     * 我只是喜欢看着你而已👁👁
     */
    private static Map<String, TypeInfo> getGenericTypeReplacementForParentInterfaceMethodsJustBecauseJavaDoNotKnowToReplaceThemWithGenericArgumentsOfThisClass(Class<?> thisClass, Method thatMethod) {
        Class<?> targetClass = thatMethod.getDeclaringClass();

        Map<String, TypeInfo> replacement = new HashMap<>();
        if (Arrays.stream(thisClass.getInterfaces()).noneMatch(c -> c.equals(targetClass))) {
            Class<?> superInterface = Arrays.stream(thisClass.getInterfaces()).filter(targetClass::isAssignableFrom).findFirst().orElse(null);
            if (superInterface == null) return Map.of();
            Map<String, TypeInfo> parentType = getGenericTypeReplacementForParentInterfaceMethodsJustBecauseJavaDoNotKnowToReplaceThemWithGenericArgumentsOfThisClass(superInterface, thatMethod);
            Map<String, TypeInfo> parentReplacement = getInterfaceRemap(thisClass, superInterface);

            for (Map.Entry<String, TypeInfo> entry : parentType.entrySet()) {
                String variable = entry.getKey();
                TypeInfo type = entry.getValue();
                replacement.put(variable, type instanceof VariableTypeInfo typeVariable ? parentReplacement.getOrDefault(typeVariable.getName(), typeVariable) : type);
            }
        } else {
            return getInterfaceRemap(thisClass, targetClass);
        }
        return replacement;
    }

    private static Map<String, TypeInfo> getInterfaceRemap(Class<?> thisClass, Class<?> thatInterface) {
        Map<String, TypeInfo> replacement = new HashMap<>();
        int indexOfInterface = -1;
        for (Type type : thisClass.getGenericInterfaces()) {
            if (type instanceof ParameterizedType parameterizedType) {
                if (parameterizedType.getRawType().equals(thatInterface)) {
                    indexOfInterface = 0;
                    for (TypeVariable<?> typeVariable : thatInterface.getTypeParameters()) {
                        replacement.put(typeVariable.getName(), TypeInfo.of(parameterizedType.getActualTypeArguments()[indexOfInterface]));
                        indexOfInterface++;
                    }
                }
            } else if (type instanceof Class<?> clazz) {
                if (clazz.equals(thatInterface)) {
                    indexOfInterface = 0;
                    for (TypeVariable<?> typeVariable : thatInterface.getTypeParameters()) {
                        // Raw use of parameterized type, so we fill with Object.class
                        // Very bad programming practice, but we have to prepare for random people coding their stuffs bad
                        replacement.put(typeVariable.getName(), TypeInfo.OBJECT);
                    }
                }
            }
        }

        if (indexOfInterface == -1) {
            // throw new IllegalArgumentException("The class does not implement the target interface");
            return Map.of();
        }

        return replacement;
    }

    public enum ClassType {
        INTERFACE,
        ENUM,
        RECORD,
        CLASS
    }

    public static class ClassAttribute {

        public final ClassType type;
        public final boolean isAbstract;
        public final boolean isInterface;
        public final Class<?> raw;


        public ClassAttribute(Class<?> clazz) {
            if (clazz.isInterface()) {
                this.type = ClassType.INTERFACE;
            } else if (clazz.isEnum()) {
                this.type = ClassType.ENUM;
            } else if (clazz.isRecord()) {
                this.type = ClassType.RECORD;
            } else {
                this.type = ClassType.CLASS;
            }

            int modifiers = clazz.getModifiers();
            this.isAbstract = Modifier.isAbstract(modifiers);
            this.isInterface = type == ClassType.INTERFACE;
            this.raw = clazz;
        }
    }
}
