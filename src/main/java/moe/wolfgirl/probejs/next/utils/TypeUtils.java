package moe.wolfgirl.probejs.next.utils;

import moe.wolfgirl.probejs.next.typescript.document.base.Type;
import moe.wolfgirl.probejs.next.typescript.document.types.ClassType;

import java.lang.reflect.Modifier;

public class TypeUtils {

    public static boolean isFunctionalInterface(Class<?> clazz) {
        int abstractMethodCount = 0;
        try {
            for (var method : clazz.getMethods()) {
                if (method.isDefault() || Modifier.isStatic(method.getModifiers())) continue;
                if (method.getDeclaringClass() == Object.class) continue;
                abstractMethodCount++;
            }
            return clazz.isInterface() && abstractMethodCount == 1;
        } catch (Throwable t) {
            // Get methods will load other classes
            return false;
        }

    }

    public static boolean isFunctionalInterface(Type type) {
        if (type instanceof ClassType classType) {
            try {
                Class<?> clazz = Class.forName(classType.classPath.toString());
                return isFunctionalInterface(clazz);
            } catch (ClassNotFoundException e) {
                return false;
            }
        }
        return false;
    }
}
