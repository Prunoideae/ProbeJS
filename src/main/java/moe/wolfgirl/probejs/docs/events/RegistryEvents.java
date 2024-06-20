package moe.wolfgirl.probejs.docs.events;

import dev.latvian.mods.kubejs.registry.*;
import dev.latvian.mods.kubejs.script.ScriptType;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;
import moe.wolfgirl.probejs.lang.typescript.code.member.ClassDecl;
import moe.wolfgirl.probejs.lang.typescript.code.ts.MethodDeclaration;
import moe.wolfgirl.probejs.lang.typescript.code.ts.Statements;
import moe.wolfgirl.probejs.lang.typescript.code.ts.Wrapped;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import moe.wolfgirl.probejs.utils.NameUtils;
import moe.wolfgirl.probejs.utils.RegistryUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RegistryEvents extends ProbeJSPlugin {

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        if (scriptDump.scriptType != ScriptType.STARTUP) return;

        Wrapped.Namespace groupNamespace = new Wrapped.Namespace("StartupEvents");

        for (ResourceKey<? extends Registry<?>> key : BuiltInRegistries.REGISTRY.registryKeySet()) {
            ClassPath registryPath = getRegistryClassPath(key.location().getNamespace(), key.location().getPath());
            String extraName = key.location().getNamespace().equals("minecraft") ?
                    key.location().getPath() :
                    key.location().toString();

            MethodDeclaration declaration = Statements.method("registry")
                    .param("type", Types.literal(extraName))
                    .param("handler", Types.lambda()
                            .param("event", Types.type(registryPath))
                            .build()
                    )
                    .build();
            groupNamespace.addCode(declaration);
        }

        scriptDump.addGlobal("registry_events", groupNamespace);
    }

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        if (scriptDump.scriptType != ScriptType.STARTUP) return;

        for (ResourceKey<? extends Registry<?>> key : BuiltInRegistries.REGISTRY.registryKeySet()) {
            RegistryInfo<?> info = RegistryInfo.of(RegistryUtils.castKey(key));
            RegistryType<?> type = RegistryType.ofKey(key);
            if (type == null) continue;

            ClassPath registryPath = getRegistryClassPath(key.location().getNamespace(), key.location().getPath());
            ClassDecl registryClass = generateRegistryClass(key, type.baseClass(), info);

            TypeScriptFile registryFile = new TypeScriptFile(registryPath);
            registryFile.addCode(registryClass);
            globalClasses.put(registryPath, registryFile);
        }

        // Let createCustom to use Supplier<T> instead of object
        TypeScriptFile registryEvent = globalClasses.get(new ClassPath(RegistryKubeEvent.class));
        ClassDecl eventClass = registryEvent.findCode(ClassDecl.class).orElse(null);
        if (eventClass == null) return;

        eventClass.methods.stream()
                .filter(method -> method.name.equals("createCustom") && method.params.size() == 2)
                .findAny()
                .ifPresent(method -> method.params.get(1).type = Types.lambda().returnType(Types.generic("T")).build());

    }

    private static ClassPath getRegistryClassPath(String namespace, String location) {
        return new ClassPath("moe.wolfgirl.probejs.generated.registry.%s.%s".formatted(
                namespace, NameUtils.rlToTitle(location)
        ));
    }

    private static ClassDecl generateRegistryClass(ResourceKey<?> key, Class<?> baseClass, RegistryInfo<?> info) {
        ClassDecl.Builder builder = Statements.clazz(NameUtils.rlToTitle(key.location().getPath()))
                .superClass(Types.parameterized(Types.type(RegistryKubeEvent.class), Types.type(baseClass)));

        BuilderType<?> defaultType = info.getDefaultType();
        if (defaultType != null) {
            builder.method("create", method -> method
                    .returnType(Types.typeMaybeGeneric(defaultType.builderClass()))
                    .param("name", Types.STRING));
        }

        for (BuilderType<?> type : info.getTypes()) {
            builder.method("create", method -> method
                    .returnType(Types.typeMaybeGeneric(type.builderClass()))
                    .param("name", Types.STRING)
                    .param("type", Types.literal(type.type())));
        }

        return builder.build();
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        Set<Class<?>> classes = new HashSet<>();

        for (ResourceKey<? extends Registry<?>> key : BuiltInRegistries.REGISTRY.registryKeySet()) {
            RegistryInfo<?> registryInfo = RegistryInfo.of(RegistryUtils.castKey(key));
            var defaultType = registryInfo.getDefaultType();
            if (defaultType != null) classes.add(defaultType.builderClass());
            for (BuilderType<?> type : registryInfo.getTypes()) {
                classes.add(type.builderClass());
            }
        }

        return classes;
    }
}
