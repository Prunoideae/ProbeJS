package moe.wolfgirl.probejs.plugin.builtins.alias;

import moe.wolfgirl.probejs.java.ClassRegistry;
import moe.wolfgirl.probejs.java.members.ClassInfo;
import moe.wolfgirl.probejs.java.members.MethodInfo;
import moe.wolfgirl.probejs.java.members.other.ParamInfo;
import moe.wolfgirl.probejs.plugin.Priority;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.typescript.base.AliasRegistrar;
import moe.wolfgirl.probejs.typescript.document.Types;
import moe.wolfgirl.probejs.typescript.document.types.VariableType;
import moe.wolfgirl.probejs.typescript.transpiler.TypeConverter;
import moe.wolfgirl.probejs.utils.TypeUtils;

import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class InterfaceTypes extends ProbeJSPlugin {

    @Override
    @Priority(-100)
    public void addTypeAlias(AliasRegistrar registrar) {
        TypeConverter converter = new TypeConverter();

        for (ClassInfo value : ClassRegistry.INSTANCE.getAllClasses().values()) {
            var rawClass = value.clazz();
            if (!TypeUtils.isFunctionalInterface(rawClass)) continue;

            registrar.addInputAlias(value.classPath(), Types.lambda(builder -> {
                MethodInfo abstractMethod = value.methods().stream()
                        .filter(m -> !m.isStatic())
                        .filter(MethodInfo::isAbstract)
                        .findFirst()
                        .orElse(null);
                if (abstractMethod == null) return; // This should never happen

                // Method specific variables becomes any, class level preserved
                Set<String> methodVariables = Arrays.stream(abstractMethod.typeVariables()).map(TypeVariable::getName).collect(Collectors.toSet());

                for (ParamInfo param : abstractMethod.params()) {
                    builder.param(param.name(), Types.remapType(
                            t -> t instanceof VariableType v && methodVariables.contains(v.name),
                            v -> Types.ANY,
                            converter.convertType(param.typeInfo())
                    ));
                }
                builder.returns(Types.remapType(
                        t -> t instanceof VariableType v && methodVariables.contains(v.name),
                        v -> Types.ANY,
                        converter.convertType(abstractMethod.returnType()).markAsInput()
                ));
            }));
        }
    }


}
