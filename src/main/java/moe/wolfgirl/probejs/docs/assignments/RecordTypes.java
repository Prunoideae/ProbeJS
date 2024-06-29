package moe.wolfgirl.probejs.docs.assignments;

import dev.latvian.mods.rhino.type.ClassTypeInfo;
import dev.latvian.mods.rhino.type.ParameterizedTypeInfo;
import dev.latvian.mods.rhino.type.RecordTypeInfo;
import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.java.clazz.Clazz;
import moe.wolfgirl.probejs.lang.transpiler.TypeConverter;
import moe.wolfgirl.probejs.lang.transpiler.transformation.InjectSpecialType;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.TSParamType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import moe.wolfgirl.probejs.lang.typescript.code.type.js.JSArrayType;
import moe.wolfgirl.probejs.lang.typescript.code.type.js.JSObjectType;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;

public class RecordTypes extends ProbeJSPlugin {

    @Override
    public void assignType(ScriptDump scriptDump) {
        TypeConverter converter = scriptDump.transpiler.typeConverter;
        for (Clazz recordedClass : scriptDump.recordedClasses) {
            if (!recordedClass.original.isRecord()) continue;
            RecordTypeInfo typeWrapper = (RecordTypeInfo) TypeInfo.of(recordedClass.original);

            JSObjectType.Builder objectType = Types.object();
            JSArrayType.Builder arrayType = Types.arrayOf();

            for (RecordTypeInfo.Component component : typeWrapper.recordComponents().values()) {
                BaseType type = converter.convertType(component.type());

                if (component.type() instanceof ParameterizedTypeInfo parameterizedTypeInfo) {
                    if (InjectSpecialType.NO_WRAPPING.contains(new ClassPath(parameterizedTypeInfo.rawType().asClass()))) {
                        if (type instanceof TSParamType paramType) {
                            paramType.params.replaceAll(baseType -> Types.ignoreContext(baseType, BaseType.FormatType.RETURN));
                        }
                    }
                }

                objectType.member(component.name(), true, type);
                arrayType.member(component.name(), true, type);
            }

            scriptDump.assignType(recordedClass.classPath, objectType.build());
            scriptDump.assignType(recordedClass.classPath, arrayType.build());
        }
    }
}
