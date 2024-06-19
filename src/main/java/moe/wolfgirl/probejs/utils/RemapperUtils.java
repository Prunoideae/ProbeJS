package moe.wolfgirl.probejs.utils;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.util.Lazy;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.JavaMembers;


import java.lang.reflect.Constructor;
import java.util.Collection;

public class RemapperUtils {

    private static final Lazy<KubeJSContext> CONTEXT = Lazy.of(() -> {
        ScriptManager manager = KubeJS.getStartupScriptManager();
        return (KubeJSContext) manager.contextFactory.enter();
    });

    public static Collection<JavaMembers.MethodInfo> getMethods(Class<?> from) {
        KubeJSContext context = CONTEXT.get();
        JavaMembers members = JavaMembers.lookupClass(context, context.topLevelScope, from, from, false);
        return members.getAccessibleMethods(context, false);
    }

    public static Collection<JavaMembers.FieldInfo> getFields(Class<?> from) {
        KubeJSContext context = CONTEXT.get();
        JavaMembers members = JavaMembers.lookupClass(context, context.topLevelScope, from, from, false);
        return members.getAccessibleFields(context, false);
    }

    public static Collection<Constructor<?>> getConstructors(Class<?> from) {
        KubeJSContext context = CONTEXT.get();
        JavaMembers members = JavaMembers.lookupClass(context, context.topLevelScope, from, from, false);
        return members.getAccessibleConstructors();
    }
}
