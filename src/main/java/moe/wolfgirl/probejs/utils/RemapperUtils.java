package moe.wolfgirl.probejs.utils;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.JavaMembers;


import java.lang.reflect.Constructor;
import java.util.Collection;

public class RemapperUtils {

    private static final Remapper RUNTIME = RemappingHelper.getMinecraftRemapper();

    public static String getRemappedClassName(Class<?> clazz) {
        String remapped = RUNTIME.getMappedClass(clazz);
        return remapped.equals("") ? clazz.getName() : remapped;
    }

    public static Collection<JavaMembers.MethodInfo> getMethods(Class<?> from) {
        ScriptManager scriptManager = KubeJS.getStartupScriptManager();
        JavaMembers members = JavaMembers.lookupClass(scriptManager.context, scriptManager.topLevelScope, from, from, false);
        return members.getAccessibleMethods(scriptManager.context, false);
    }

    public static Collection<JavaMembers.FieldInfo> getFields(Class<?> from) {
        ScriptManager scriptManager = KubeJS.getStartupScriptManager();
        Context context = scriptManager.contextFactory.enter();
        JavaMembers members = JavaMembers.lookupClass(context, scriptManager.topLevelScope, from, from, false);
        return members.getAccessibleFields(context, false);
    }

    public static Collection<Constructor<?>> getConstructors(Class<?> from) {
        ScriptManager scriptManager = KubeJS.getStartupScriptManager();
        JavaMembers members = JavaMembers.lookupClass(scriptManager.context, scriptManager.topLevelScope, from, from, false);
        return members.getAccessibleConstructors();
    }
}
