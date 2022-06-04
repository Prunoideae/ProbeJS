package com.probejs.formatter;

import com.probejs.compiler.SpecialCompiler;
import com.probejs.formatter.formatter.FormatterClass;
import com.probejs.formatter.formatter.FormatterRegistry;
import com.probejs.formatter.formatter.FormatterType;
import com.probejs.info.ClassInfo;
import com.probejs.info.MethodInfo;
import com.probejs.info.type.ITypeInfo;
import com.probejs.info.type.TypeInfoClass;
import com.probejs.info.type.TypeInfoParameterized;
import com.probejs.info.type.TypeInfoVariable;
import dev.latvian.mods.rhino.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SpecialTypes {

    public static Set<Class<?>> skippedSpecials = new HashSet<>();

    private static class FormatterLambda {
        private final MethodInfo info;

        private FormatterLambda(MethodInfo info) {
            this.info = info;
        }

        public String format(ITypeInfo typeInfo) {
            Map<String, ITypeInfo> variableMap = new HashMap<>();
            if (typeInfo instanceof TypeInfoParameterized parameterized) {
                List<ITypeInfo> concreteTypes = new ArrayList<>(parameterized.getParamTypes());
                for (ITypeInfo variable : info.getFrom().getParameters()) {
                    variableMap.put(variable.getTypeName(), concreteTypes.isEmpty() ? new TypeInfoClass(Object.class) : concreteTypes.remove(0));
                }
            }

            List<String> formattedParam = new ArrayList<>();
            for (MethodInfo.ParamInfo param : info.getParams()) {
                ITypeInfo resolvedType = param.getType();
                if (resolvedType instanceof TypeInfoVariable) {
                    resolvedType = variableMap.getOrDefault(resolvedType.getTypeName(), new TypeInfoClass(Object.class));
                }
                formattedParam.add("%s: %s".formatted(param.getName(), new FormatterType(resolvedType, false).format(0, 0)));
            }
            ITypeInfo resolvedReturn = info.getReturnType();
            if (resolvedReturn instanceof TypeInfoVariable) {
                resolvedReturn = variableMap.getOrDefault(resolvedReturn.getTypeName(), new TypeInfoClass(Object.class));
            }
            return "((%s) => %s)".formatted(String.join(", ", formattedParam), new FormatterType(resolvedReturn, false).format(0, 0));
        }
    }

    public static void processFunctionalInterfaces(Set<Class<?>> globalClasses) {
        for (Class<?> clazz : globalClasses) {
            if (clazz.isInterface() && clazz.getAnnotation(FunctionalInterface.class) != null && !skippedSpecials.contains(clazz)) {
                //Functional interfaces has one and only one abstract method
                ClassInfo info = ClassInfo.getOrCache(clazz);
                for (MethodInfo method : info.getMethodInfo()) {
                    if (method.isAbstract()) {
                        FormatterLambda formatter = new FormatterLambda(method);
                        NameResolver.putTypeFormatter(clazz, formatter::format);
                        break;
                    }
                }
            }
        }
    }

    private static String formatValueOrType(Object obj) {
        String formattedValue = NameResolver.formatValue(obj);
        if (formattedValue == null) {
            if (!NameResolver.resolvedNames.containsKey(obj.getClass().getName()) && !obj.getClass().getName().contains("$Lambda$")) {
                NameResolver.resolveName(obj.getClass());
            }
            formattedValue = FormatterClass.formatTypeParameterized(new TypeInfoClass(obj.getClass()));
        }
        return formattedValue;
    }

    public static String formatMap(Object obj) {
        List<String> values = new ArrayList<>();
        if (obj instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                String formattedKey = NameResolver.formatValue(key);
                if (formattedKey == null)
                    continue;
                String formattedValue = formatValueOrType(value);
                values.add("%s:%s".formatted(formattedKey, formattedValue));
            }
        }
        return "{%s}".formatted(String.join(",", values));
    }

    public static String formatClassLike(ITypeInfo obj) {
        ITypeInfo inner = null;
        if (obj instanceof TypeInfoParameterized cls) {
            inner = cls.getParamTypes().get(0);
        } else if (obj instanceof TypeInfoClass cls) {
            inner = cls;
        }
        if (inner == null)
            return "any";
        return "typeof %s".formatted(new FormatterType(inner.getBaseType(), false).format(0, 0));
    }

    public static String formatList(Object obj) {
        List<String> values = new ArrayList<>();
        if (obj instanceof List<?> list) {
            for (Object o : list) {
                String formattedValue = NameResolver.formatValue(o);
                if (formattedValue == null)
                    formattedValue = "undefined";
                values.add(formattedValue);
            }
        }
        return "[%s]".formatted(String.join(", ", values));
    }

    public static String formatScriptable(Object obj) {
        List<String> values = new ArrayList<>();
        if (obj instanceof ScriptableObject scriptable) {
            Scriptable pt = scriptable.getPrototype();
            if (pt.get("constructor", pt) instanceof BaseFunction fun) {
                //Resolves Object since they're not typed
                if (!fun.getFunctionName().isEmpty() && !fun.getFunctionName().equals("Object")) {
                    return fun.getFunctionName();
                }
            }

            for (Object id : scriptable.getIds()) {
                String formattedKey = NameResolver.formatValue(id);
                Object value;
                if (id instanceof Number) {
                    value = scriptable.get((Integer) id, scriptable);
                } else {
                    value = scriptable.get((String) id, scriptable);
                }
                String formattedValue = formatValueOrType(value);
                values.add("%s:%s".formatted(formattedKey, formattedValue));
            }
            var proto = scriptable.getPrototype();
            for (Object id : proto.getIds()) {
                String formattedKey = NameResolver.formatValue(id);
                Object value;
                if (id instanceof Number) {
                    value = proto.get((Integer) id, scriptable);
                } else {
                    value = proto.get((String) id, scriptable);
                }
                String formattedValue = formatValueOrType(value);
                values.add("%s:%s".formatted(formattedKey, formattedValue));
            }
        }
        return "{%s}".formatted(String.join(",", values));
    }

    public static String formatFunction(Object obj) {
        if (obj instanceof BaseFunction function) {
            return "(%s) => any".formatted(IntStream.range(0, function.getLength()).mapToObj("arg%s"::formatted).collect(Collectors.joining(", ")));
        }
        return null;
    }

    public static String formatNJO(Object obj) {
        if (obj instanceof NativeJavaObject njo) {
            return formatValueOrType(njo.unwrap());
        }
        return null;
    }

    public static <T> void assignRegistry(Class<T> clazz, ResourceKey<Registry<T>> registry) {
        SpecialCompiler.specialCompilers.add(new FormatterRegistry<T>(registry, clazz));
        NameResolver.putSpecialAssignments(clazz, () -> List.of("Special.%s".formatted(clazz.getSimpleName())));
    }
}
