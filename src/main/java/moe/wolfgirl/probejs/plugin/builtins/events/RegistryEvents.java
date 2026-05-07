package moe.wolfgirl.probejs.plugin.builtins.events;

import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.registry.*;
import moe.wolfgirl.probejs.utils.GameUtils;
import moe.wolfgirl.probejs.utils.NameUtils;
import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.typescript.Documents;
import moe.wolfgirl.probejs.typescript.base.DocumentRegistrar;
import moe.wolfgirl.probejs.typescript.document.ClassDecl;
import moe.wolfgirl.probejs.typescript.document.Members;
import moe.wolfgirl.probejs.typescript.document.Types;
import moe.wolfgirl.probejs.typescript.document.base.KindAware;
import moe.wolfgirl.probejs.typescript.document.builders.ClassBuilder;
import moe.wolfgirl.probejs.typescript.document.members.MethodDecl;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DataPackRegistriesHooks;

import java.util.stream.Collectors;

public class RegistryEvents extends Events {
    public static final ClassPath STARTUP_REGISTRY = Events.STARTUP_EVENTS.append("StartupEvents$Registry");
    public static final ClassPath SERVER_REGISTRY = Events.SERVER_EVENTS.append("ServerEvents$Registry");
    public static final ClassPath STARTUP_BASE = Events.STARTUP_EVENTS.append("registry");
    public static final ClassPath SERVER_BASE = Events.SERVER_EVENTS.append("registry");

    static {
        Events.SKIP_EVENTS.add(Pair.of("StartupEvents", "registry"));
        Events.SKIP_EVENTS.add(Pair.of("ServerEvents", "registry"));
    }

    @Override
    public void transformClass(Documents.ClassDocument document) {
        if (document.classInfo().clazz().equals(RegistryKubeEvent.class)) {
            var classDecl = document.document(); // Remove create methods to prevent conflicts with generated builder classes
            classDecl.members.removeIf(code -> code instanceof MethodDecl methodDecl && methodDecl.name.equals("create"));
        }
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void addSidedDocuments(DocumentRegistrar registrar) {
        var startupRegistryDecl = Members.clazz(Events.STARTUP_EVENTS.append("StartupEvents")).kind(KindAware.Kind.NAMESPACE).noExport();
        var serverRegistryDecl = Members.clazz(Events.SERVER_EVENTS.append("ServerEvents")).kind(KindAware.Kind.NAMESPACE).noExport();

        var serverRegistryKeys = DataPackRegistriesHooks.getDataPackRegistries()
                .stream().map(RegistryDataLoader.RegistryData::key)
                .filter(k -> BuilderTypeRegistryHandler.INFO.get().containsKey(k))
                .collect(Collectors.toSet());
        var startupRegistryKeys = BuilderTypeRegistryHandler.INFO.get().keySet()
                .stream().filter(k -> !serverRegistryKeys.contains(k))
                .collect(Collectors.toSet());

        for (ResourceKey<?> regKey : startupRegistryKeys) {
            var info = BuilderTypeRegistryHandler.info(GameUtils.castKey(regKey));
            if (info == null || (info.defaultType() == null && info.types().isEmpty())) continue;
            RegistryType<?> type = RegistryType.ofKey(GameUtils.castKey(regKey));
            if (type == null) continue;

            registrar.addDocument(
                    builderClass(STARTUP_BASE, regKey),
                    makeMockedBuilderClass(STARTUP_BASE, type.baseClass(), regKey, info)
            );

            startupRegistryDecl.method("registry", mb -> {
                ResourceLocation loc = regKey.location();
                String extraName = loc.getNamespace().equals("minecraft") ? loc.getPath() : loc.toString();
                mb.param("type", Types.literal(extraName));
                mb.param("handler", Types.lambda(lb -> {
                    lb.param("event", Types.clazz(builderClass(STARTUP_BASE, regKey)));
                }));
            });
        }

        for (ResourceKey<? extends Registry<?>> regKey : serverRegistryKeys) {
            var info = BuilderTypeRegistryHandler.info(GameUtils.castKey(regKey));
            if (info == null || (info.defaultType() == null && info.types().isEmpty())) continue;
            RegistryType<?> type = RegistryType.ofKey(GameUtils.castKey(regKey));
            if (type == null) continue;

            registrar.addDocument(
                    builderClass(SERVER_BASE, regKey),
                    makeMockedBuilderClass(SERVER_BASE, type.baseClass(), regKey, info)
            );

            serverRegistryDecl.method("registry", mb -> {
                ResourceLocation loc = regKey.location();
                String extraName = loc.getNamespace().equals("minecraft") ? loc.getPath() : loc.toString();
                mb.param("type", Types.literal(extraName));
                mb.param("handler", Types.lambda(lb -> {
                    lb.param("event", Types.clazz(builderClass(SERVER_BASE, regKey)));
                }));
            });
        }

        registrar.addGlobal(STARTUP_REGISTRY, startupRegistryDecl.build());
        registrar.addGlobal(SERVER_REGISTRY, serverRegistryDecl.build());
    }

    private static ClassDecl makeMockedBuilderClass(
            ClassPath basePath,
            Class<?> baseClass,
            ResourceKey<?> key,
            BuilderTypeRegistryHandler.Info<?> info) {
        ClassPath registryPath = builderClass(basePath, key);
        ClassBuilder builder = Members.clazz(registryPath)
                .noExport()
                .extendsType(Types.clazz(RegistryKubeEvent.class).withParams(Types.clazz(baseClass).asMaybeGeneric()))
                .kind(KindAware.Kind.CLASS);

        BuilderType<?> defaultType = info.defaultType();
        if (defaultType != null) {
            builder.method("create", mb -> {
                mb.returnType(Types.clazz(defaultType.builderClass()).asMaybeGeneric());
                mb.param("name", Types.STRING);
            });
        }

        for (BuilderType<?> type : info.types()) {
            builder.method("create", mb -> {
                mb.returnType(Types.clazz(type.builderClass()).asMaybeGeneric());
                mb.param("name", Types.STRING);
                mb.param("type", Types.literal(type.type().toString()));
            });
        }

        return builder.build();
    }

    private static ClassPath builderClass(ClassPath base, ResourceKey<?> resourceKey) {
        return base.append(NameUtils.registryToName(resourceKey));
    }

    @Override
    public boolean allowClassInDiscovery(Class<?> clazz) {
        return BuilderBase.class.isAssignableFrom(clazz);
    }
}
