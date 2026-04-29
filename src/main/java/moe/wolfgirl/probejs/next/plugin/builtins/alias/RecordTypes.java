package moe.wolfgirl.probejs.next.plugin.builtins.alias;

import dev.latvian.mods.rhino.type.RecordTypeInfo;
import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.java.ClassRegistry;
import moe.wolfgirl.probejs.next.java.members.ClassInfo;
import moe.wolfgirl.probejs.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.next.typescript.base.AliasRegistrar;
import moe.wolfgirl.probejs.next.typescript.document.Types;
import moe.wolfgirl.probejs.next.typescript.document.base.Type;
import moe.wolfgirl.probejs.next.typescript.document.types.VariableType;
import moe.wolfgirl.probejs.next.typescript.document.types.special.ObjectType;
import moe.wolfgirl.probejs.next.typescript.transpiler.TypeConverter;
import net.minecraft.tags.TagKey;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RecordTypes extends ProbeJSPlugin {
    private static final Set<Class<?>> SKIP_RECORDS = Set.of(
            TagKey.class
    );

    @Override
    public void addTypeAlias(AliasRegistrar registrar) {
        Map<ClassPath, ObjectType> recordTypes = new HashMap<>();
        TypeConverter converter = new TypeConverter(); // FIXME: We might need a way to keep only one instance of TypeConverter instead of creating a new one here.
        for (ClassInfo value : ClassRegistry.INSTANCE.getAllClasses().values()) {
            var rawClass = value.clazz();
            if (!rawClass.isRecord() || SKIP_RECORDS.contains(rawClass)) continue;
            RecordTypeInfo recordTypeInfo = (RecordTypeInfo) TypeInfo.of(rawClass);
            var objectType = Types.object(builder -> {
                for (RecordTypeInfo.Component component : recordTypeInfo.recordComponents().values()) {
                    Type type = converter.convertType(component.type());
                    type = Types.remapType(t -> t instanceof VariableType, v -> {
                        var variableType = (VariableType) v;
                        return variableType.typeInfo == null ? Types.ANY : variableType.typeInfo;
                    }, type);
                    builder.param(component.name(), true, type);
                }
            });
            recordTypes.put(value.classPath(), objectType);
        }

        for (Map.Entry<ClassPath, ObjectType> classPathPairEntry : recordTypes.entrySet()) {
            var classPath = classPathPairEntry.getKey();
            var objectType = classPathPairEntry.getValue();

            registrar.addInputAlias(classPath, objectType);
            registrar.addInputAlias(classPath, objectType.asFixedArray());
        }
    }
}
