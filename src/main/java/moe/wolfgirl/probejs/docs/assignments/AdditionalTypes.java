package moe.wolfgirl.probejs.docs.assignments;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugins;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.script.TypeDescriptionRegistry;
import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.transpiler.TypeConverter;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.TSClassType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;

import java.util.Map;

public class AdditionalTypes extends ProbeJSPlugin {

    @Override
    public void assignType(ScriptDump scriptDump) {
        TypeDescriptions typeDescriptions = new TypeDescriptions(scriptDump.scriptType);
        KubeJSPlugins.forEachPlugin(plugin -> plugin.registerTypeDescriptions(typeDescriptions));
        TypeConverter typeConverter = scriptDump.transpiler.typeConverter;

        for (Map.Entry<Class<?>, TypeInfo> entry : typeDescriptions.registries.entries()) {
            Class<?> clazz = entry.getKey();
            ClassPath classPath = new ClassPath(clazz);
            TypeInfo typeInfo = entry.getValue();

            // Prevent people from registering circulating types
            BaseType baseType = Types.filter(typeConverter.convertType(typeInfo),
                    type -> type instanceof TSClassType classType &&
                            classType.classPath.equals(classPath)
            );

            scriptDump.assignType(clazz, baseType);
        }
    }

    static class TypeDescriptions implements TypeDescriptionRegistry {
        private final ScriptType scriptType;
        private final Multimap<Class<?>, TypeInfo> registries = ArrayListMultimap.create();

        TypeDescriptions(ScriptType scriptType) {
            this.scriptType = scriptType;
        }

        @Override
        public ScriptType scriptType() {
            return scriptType;
        }

        @Override
        public void register(Class<?> target, TypeInfo typeInfo) {
            registries.put(target, typeInfo);
        }
    }
}
