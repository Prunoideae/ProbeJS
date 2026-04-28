package moe.wolfgirl.probejs.next.plugin.builtins.alias;

import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.rhino.type.RecordTypeInfo;
import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.next.typescript.Documents;
import moe.wolfgirl.probejs.next.typescript.base.AliasRegistrar;
import moe.wolfgirl.probejs.next.typescript.document.Types;
import moe.wolfgirl.probejs.next.typescript.document.base.Type;
import moe.wolfgirl.probejs.next.typescript.document.types.special.ObjectType;


import java.util.HashMap;
import java.util.Map;

public class RecordTypes extends ProbeJSPlugin {
    private static final Map<ClassPath, ObjectType> recordTypes = new HashMap<>();

    @Override
    public void transformClass(Documents.ClassDocument document) {
        var classInfo = document.classInfo();
        var rawClass = classInfo.clazz();
        var converter = document.converter();
        if (!classInfo.attributes().isRecord()) return;

        RecordTypeInfo recordTypeInfo = (RecordTypeInfo) TypeInfo.of(rawClass);
        var objectType = Types.object(builder -> {
            for (RecordTypeInfo.Component component : recordTypeInfo.recordComponents().values()) {
                Type type = converter.convertType(component.type());
                builder.param(component.name(), true, type);
            }
        });


        recordTypes.put(classInfo.classPath(), objectType);
    }

    @Override
    public void addTypeAlias(AliasRegistrar registrar) {
        for (Map.Entry<ClassPath, ObjectType> classPathPairEntry : recordTypes.entrySet()) {
            var classPath = classPathPairEntry.getKey();
            var objectType = classPathPairEntry.getValue();

            registrar.addInputAlias(classPath, objectType);
            registrar.addInputAlias(classPath, objectType.asFixedArray());
        }
    }
}
