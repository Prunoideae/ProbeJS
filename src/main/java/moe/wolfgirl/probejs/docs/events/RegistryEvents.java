package moe.wolfgirl.probejs.docs.events;

import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.registry.*;
import dev.latvian.mods.kubejs.script.ScriptType;
import moe.wolfgirl.probejs.ProbeJS;
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
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DataPackRegistriesHooks;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class RegistryEvents extends ProbeJSPlugin {
    @Override
    public Set<Pair<String, String>> disableEventDumps(ScriptDump dump) {
        return Set.of(
                Pair.of("StartupEvents", "registry"),
                Pair.of("ServerEvents", "registry")
        );
    }

    private static void addGlobal(ScriptDump scriptDump, Iterable<? extends ResourceKey<? extends Registry<?>>> keys,
                                  String namespace, String name) {
        Wrapped.Namespace groupNamespace = new Wrapped.Namespace(namespace);
        for (ResourceKey<? extends Registry<?>> key : keys) {
            BuilderTypeRegistryHandler.Info<?> info = BuilderTypeRegistryHandler.info(RegistryUtils.castKey(key));
            if (info == null) continue;
            if (info.defaultType() == null && info.types().isEmpty()) {
                continue;
            }
            ClassPath registryPath = getRegistryClassPath(key.location().getNamespace(), key.location().getPath());
            String extraName = key.location().getNamespace().equals("minecraft") ?
                    key.location().getPath() :
                    key.location().toString();

            MethodDeclaration declaration = Statements.method(name)
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
    public void addGlobals(ScriptDump scriptDump) {
        if (scriptDump.scriptType == ScriptType.STARTUP) {
            addGlobal(scriptDump, BuiltInRegistries.REGISTRY.registryKeySet(), "StartupEvents", "registry");
        } else if (scriptDump.scriptType == ScriptType.SERVER) {
            var keys = DataPackRegistriesHooks.getDataPackRegistries()
                    .stream().map(RegistryDataLoader.RegistryData::key)
                    .collect(Collectors.toSet());
            addGlobal(scriptDump, keys, "ServerEvents", "registry");
        }
    }

    private static void modifyClass(Map<ClassPath, TypeScriptFile> globalClasses,
                                    Iterable<? extends ResourceKey<? extends Registry<?>>> keys) {
        for (ResourceKey<? extends Registry<?>> key : keys) {
            BuilderTypeRegistryHandler.Info<?> info = BuilderTypeRegistryHandler.info(RegistryUtils.castKey(key));
            if (info == null) continue;
            if (info.defaultType() == null && info.types().isEmpty()) continue;
            RegistryType<?> type = RegistryType.ofKey(key);
            if (type == null) continue;

            ClassPath registryPath = getRegistryClassPath(key.location().getNamespace(), key.location().getPath());
            ClassDecl registryClass = generateRegistryClass(key, type.baseClass(), info);

            TypeScriptFile registryFile = new TypeScriptFile(registryPath);
            registryFile.addCode(registryClass);
            globalClasses.put(registryPath, registryFile);
        }
    }

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        if (scriptDump.scriptType == ScriptType.STARTUP) {
            modifyClass(globalClasses, BuiltInRegistries.REGISTRY.registryKeySet());
            // Let createCustom to use Supplier<T> instead of object
            TypeScriptFile registryEvent = globalClasses.get(new ClassPath(RegistryKubeEvent.class));
            ClassDecl eventClass = registryEvent.findCode(ClassDecl.class).orElse(null);
            if (eventClass == null) return;

            eventClass.methods.stream()
                    .filter(method -> method.name.equals("createCustom") && method.params.size() == 2)
                    .findAny()
                    .ifPresent(method -> method.params.get(1).type = Types.lambda().returnType(Types.generic("T")).build());

        } else if (scriptDump.scriptType == ScriptType.SERVER) {
            var keys = DataPackRegistriesHooks.getDataPackRegistries()
                    .stream().map(RegistryDataLoader.RegistryData::key)
                    .collect(Collectors.toSet());
            modifyClass(globalClasses, keys);
        }
    }

    private static ClassPath getRegistryClassPath(String namespace, String location) {
        return new ClassPath("moe.wolfgirl.probejs.generated.registry.%s.%s".formatted(
                namespace, NameUtils.rlToTitle(location)
        ));
    }

    private static ClassDecl generateRegistryClass(ResourceKey<?> key, Class<?> baseClass, BuilderTypeRegistryHandler.Info<?> info) {
        ClassDecl.Builder builder = Statements.clazz("$" + NameUtils.rlToTitle(key.location().getPath()))
                .superClass(Types.parameterized(Types.type(RegistryKubeEvent.class), Types.type(baseClass)));

        BuilderType<?> defaultType = info.defaultType();
        if (defaultType != null) {
            builder.method("create", method -> method
                    .returnType(Types.typeMaybeGeneric(defaultType.builderClass()))
                    .param("name", Types.STRING));
        }

        for (BuilderType<?> type : info.types()) {
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

        Set<ResourceKey<? extends Registry<?>>> allKeys = new HashSet<>(BuiltInRegistries.REGISTRY.registryKeySet());
        DataPackRegistriesHooks.getDataPackRegistries()
                .stream().map(RegistryDataLoader.RegistryData::key)
                .forEach(allKeys::add);

        for (ResourceKey<? extends Registry<?>> key : allKeys) {
            BuilderTypeRegistryHandler.Info<?> registryInfo = BuilderTypeRegistryHandler.info(RegistryUtils.castKey(key));
            if (registryInfo == null) continue;
            var defaultType = registryInfo.defaultType();
            if (defaultType != null) classes.add(defaultType.builderClass());
            for (BuilderType<?> type : registryInfo.types()) {
                classes.add(type.builderClass());
            }
        }
        return classes;
    }
}
