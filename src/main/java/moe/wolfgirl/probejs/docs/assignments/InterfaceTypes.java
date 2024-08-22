package moe.wolfgirl.probejs.docs.assignments;

import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.lang.java.ClassRegistry;
import moe.wolfgirl.probejs.lang.java.clazz.Clazz;
import moe.wolfgirl.probejs.lang.java.clazz.members.MethodInfo;
import moe.wolfgirl.probejs.lang.java.clazz.members.ParamInfo;
import moe.wolfgirl.probejs.lang.transpiler.TypeConverter;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import moe.wolfgirl.probejs.lang.typescript.code.type.js.JSLambdaType;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

public class InterfaceTypes extends ProbeJSPlugin {
    @Override
    public void assignType(ScriptDump scriptDump) {
        TypeConverter converter = scriptDump.transpiler.typeConverter;
        for (Clazz recordedClass : scriptDump.recordedClasses) {
            if (!recordedClass.attribute.isInterface) continue;
            Optional<MethodInfo> optionalMethodInfo = recordedClass.methods
                    .stream()
                    .filter(methodInfo -> methodInfo.attributes.isAbstract)
                    .findFirst();
            long count = recordedClass.methods.stream().filter(m -> m.attributes.isAbstract).count();
            if (optionalMethodInfo.isEmpty()) continue;
            if (count != 1) continue;

            MethodInfo methodInfo = optionalMethodInfo.get();
            JSLambdaType.Builder builder = Types.lambda();
            for (ParamInfo param : methodInfo.params) {
                builder.param(param.name, converter.convertType(param.type));
            }
            builder.returnType(converter.convertType(methodInfo.returnType));
            scriptDump.assignType(recordedClass.classPath, builder.build());
        }
    }
}
