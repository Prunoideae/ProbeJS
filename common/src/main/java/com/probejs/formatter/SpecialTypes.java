package com.probejs.formatter;

import com.probejs.formatter.formatter.FormatterClass;
import com.probejs.formatter.formatter.FormatterType;
import com.probejs.info.ClassInfo;
import com.probejs.info.MethodInfo;
import com.probejs.info.type.ITypeInfo;
import com.probejs.info.type.TypeInfoClass;
import com.probejs.info.type.TypeInfoParameterized;
import com.probejs.info.type.TypeInfoVariable;
import dev.latvian.mods.kubejs.item.ingredient.IngredientJS;

import java.util.*;
import java.util.function.Function;

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
                formattedParam.add("%s: %s".formatted(param.getName(), new FormatterType(resolvedType).format(0, 0)));
            }
            ITypeInfo resolvedReturn = info.getReturnType();
            if (resolvedReturn instanceof TypeInfoVariable) {
                resolvedReturn = variableMap.getOrDefault(resolvedReturn.getTypeName(), new TypeInfoClass(Object.class));
            }
            return "((%s) => %s)".formatted(String.join(", ", formattedParam), new FormatterType(resolvedReturn).format(0, 0));
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

    public static String formatMaps(Object obj) {
        List<String> values = new ArrayList<>();
        if (obj instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                String formattedKey = NameResolver.formatValue(key);
                if (formattedKey == null)
                    continue;
                String formattedValue = NameResolver.formatValue(value);
                if (formattedValue == null)
                    formattedValue = FormatterClass.formatTypeParameterized(new TypeInfoClass(value.getClass()));
                values.add("%s:%s".formatted(formattedKey, formattedValue));
            }
        }
        return "{%s}".formatted(String.join(",", values));
    }

    public static void init() {
        //Skipping classes that are not reasonably to be a functional interface
    }
}
