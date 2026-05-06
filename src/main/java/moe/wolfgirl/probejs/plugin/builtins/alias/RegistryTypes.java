package moe.wolfgirl.probejs.plugin.builtins.alias;

import dev.latvian.mods.kubejs.registry.RegistryType;
import moe.wolfgirl.probejs.utils.GameUtils;
import moe.wolfgirl.probejs.utils.NameUtils;
import moe.wolfgirl.probejs.ClassPath;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.typescript.Documents;
import moe.wolfgirl.probejs.typescript.base.AliasRegistrar;
import moe.wolfgirl.probejs.typescript.base.DocumentRegistrar;
import moe.wolfgirl.probejs.typescript.document.Members;
import moe.wolfgirl.probejs.typescript.document.TypeDecl;
import moe.wolfgirl.probejs.typescript.document.Types;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.KindAware;
import moe.wolfgirl.probejs.typescript.document.base.Type;
import moe.wolfgirl.probejs.typescript.document.builders.ClassBuilder;
import moe.wolfgirl.probejs.typescript.document.types.special.NamespacedType;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RegistryTypes extends ProbeJSPlugin {
    public static final ClassPath REGISTRY_TYPES = ClassPath.special("types.RegistryTypes");
    public static final ClassPath REGISTRY_MARKED = ClassPath.special("types.RegistryMarked");
    public static final Type RESOLVE_TAG = Types.namespaced(REGISTRY_TYPES, "ResolveTag").withParams(Types.variable("T"));
    public static final Type RESOLVE_OBJECT = Types.namespaced(REGISTRY_TYPES, "ResolveObject").withParams(Types.variable("T"));

    public static Map<ResourceKey<? extends Registry<?>>, Class<?>> PREDEFINED_TYPES = Map.of(
            Registries.DIMENSION, Level.class
    );

    public static final Set<ClassPath> HOLDER_TYPES = Set.of(
            new ClassPath(Holder.class),
            new ClassPath(TagKey.class),
            new ClassPath(ResourceKey.class),
            new ClassPath(HolderSet.class)
    );

    static {
        RecordTypes.SKIP_RECORDS.add(TagKey.class);
    }

    public static NamespacedType tag(String name) {
        return Types.namespaced(REGISTRY_TYPES, name + "Tag");
    }

    public static NamespacedType object(String name) {
        return Types.namespaced(REGISTRY_TYPES, name);
    }

    @Override
    public void addTypeAlias(AliasRegistrar registrar) {
        registrar.addInputAlias(TagKey.class, RESOLVE_TAG);
        registrar.addInputAlias(Holder.class, RESOLVE_OBJECT);
        registrar.addInputAlias(ResourceKey.class, RESOLVE_OBJECT);
        registrar.addInputAlias(HolderSet.class, Types.union(RESOLVE_OBJECT).asArray());

        // Base class to registry type
        MinecraftServer currentServer = GameUtils.getCurrentServer();
        if (currentServer == null) return;
        RegistryAccess registryAccess = currentServer.registryAccess();

        for (ResourceKey<? extends Registry<?>> registry : GameUtils.getRegistries(registryAccess)) {
            ClassPath baseClass = findRegistryBaseClass(registry);
            if (baseClass == null) continue;
            String typeName = NameUtils.registryToName(registry);
            registrar.addInputAlias(baseClass, object(typeName));
        }
    }

    @Override
    public Set<Class<?>> provideClassForDiscovery() {
        Set<Class<?>> classes = new HashSet<>(Set.of(
                TagKey.class,
                Holder.class,
                ResourceKey.class,
                HolderSet.class
        ));

        MinecraftServer currentServer = GameUtils.getCurrentServer();
        if (currentServer == null) return classes;
        RegistryAccess registryAccess = currentServer.registryAccess();

        for (ResourceKey<? extends Registry<?>> registry : GameUtils.getRegistries(registryAccess)) {
            ClassPath baseClass = findRegistryBaseClass(registry);
            if (baseClass == null) continue;
            try {
                classes.add(Class.forName(baseClass.toString()));
            } catch (Throwable ignore) {
            }
        }

        return classes;
    }

    @Override
    public void modifyClasses(Documents.ClassAccessor classDocuments) {
        MinecraftServer currentServer = GameUtils.getCurrentServer();
        if (currentServer == null) return;
        RegistryAccess registryAccess = currentServer.registryAccess();

        for (ResourceKey<? extends Registry<?>> registry : GameUtils.getRegistries(registryAccess)) {
            ClassPath baseClass = findRegistryBaseClass(registry);
            if (baseClass == null) continue;
            String typeName = NameUtils.registryToName(registry);
            ClassPath mixinPath = baseClass.withSuffix("$$RegistryTypeMixin");
            classDocuments.addClassDocument(mixinPath, new InterfaceMixin(baseClass, tag(typeName), object(typeName)));
        }
    }

    @Nullable
    public static ClassPath findRegistryBaseClass(ResourceKey<? extends Registry<?>> registryKey) {
        if (registryKey.equals(Registries.CUSTOM_STAT)) return null;
        if (PREDEFINED_TYPES.containsKey(registryKey)) {
            return new ClassPath(PREDEFINED_TYPES.get(registryKey));
        } else {
            RegistryType<?> registryType = RegistryType.ofKey(GameUtils.castKey(registryKey));
            if (registryType == null) return null;
            return new ClassPath(registryType.baseClass());
        }
    }


    @Override
    public void addSpecialDocuments(DocumentRegistrar registrar) {
        MinecraftServer currentServer = GameUtils.getCurrentServer();
        if (currentServer == null) return;
        RegistryAccess registryAccess = currentServer.registryAccess();

        ClassBuilder registryTypes = Members.clazz(REGISTRY_TYPES).kind(KindAware.Kind.NAMESPACE);

        for (ResourceKey<? extends Registry<?>> key : GameUtils.getRegistries(registryAccess)) {
            var registry = registryAccess.registry(key).orElse(null);
            if (registry == null) continue;
            registryTypes.member(makeType(key, registry));
            registryTypes.member(makeTagType(key, registry));
        }

        // export type Resolve(Tag/Object)<T> = T extends RegistryMarked<Infer O, infer T> ? O/T : never;
        // Use any for O/T to avoid inferring both types
        registryTypes.member(new TypeDecl(
                REGISTRY_TYPES.append("ResolveTag"),
                List.of(Types.variable("T")),
                Types.raw("T extends RegistryMarked<infer O, any> ? O : never"),
                false
        ));
        registryTypes.member(new TypeDecl(
                REGISTRY_TYPES.append("ResolveObject"),
                List.of(Types.variable("T")),
                Types.raw("T extends RegistryMarked<any, infer O> ? O : never"),
                false
        ));

        registrar.addDocument(REGISTRY_TYPES, registryTypes.build());
        registrar.addDocument(REGISTRY_MARKED, new RegistryMarked());
    }

    private TypeDecl makeType(ResourceKey<? extends Registry<?>> key, Registry<?> registry) {
        String typeName = NameUtils.registryToName(key);
        List<String> entries = registry.keySet().stream()
                .map(ResourceLocation::toString)
                .toList();
        if (entries.isEmpty()) {
            return new TypeDecl(REGISTRY_TYPES.append(typeName), Types.NEVER, false);
        } else {
            return new TypeDecl(
                    REGISTRY_TYPES.append(typeName),
                    Types.union(entries.stream().map(Types::literal).map(t -> (Type) t).toList()),
                    false
            );
        }
    }

    private TypeDecl makeTagType(ResourceKey<? extends Registry<?>> key, Registry<?> registry) {
        String typeName = NameUtils.registryToName(key);
        List<String> entries = registry.getTagNames()
                .map(TagKey::location)
                .map(ResourceLocation::toString)
                .toList();
        if (entries.isEmpty()) {
            return new TypeDecl(REGISTRY_TYPES.append(typeName + "Tag"), Types.NEVER, false);
        } else {
            return new TypeDecl(
                    REGISTRY_TYPES.append(typeName + "Tag"),
                    Types.union(entries.stream().map(Types::literal).map(t -> (Type) t).toList()),
                    false
            );
        }
    }

    // export interface Name extends RegistryMarked<Object, Tag> {}
    // In reality this should take a class path of ${original}$$RegistryMixin to avoid conflict
    static class InterfaceMixin extends Code {
        private static final Type REGISTRY_MARKED_TYPE = Types.clazz(REGISTRY_MARKED);
        private final ClassPath classPath;
        private final Type tagType;
        private final Type objectType;

        InterfaceMixin(ClassPath classPath, Type tagType, Type objectType) {
            this.classPath = classPath;
            this.tagType = tagType;
            this.objectType = objectType;
        }

        @Override
        public Set<ClassPath> getImports() {
            Set<ClassPath> imports = new HashSet<>();
            imports.addAll(REGISTRY_MARKED_TYPE.getImports());
            imports.addAll(tagType.getImports());
            imports.addAll(objectType.getImports());
            return imports;
        }

        @Override
        public List<String> format(int indent) {
            var extendsType = REGISTRY_MARKED_TYPE.withParams(tagType, objectType);
            return List.of("%sexport interface %s extends %s {}".formatted(
                    " ".repeat(indent),
                    classPath.getClassName(),
                    extendsType.first()
            ));
        }
    }

    // const registryTag$$marker: unique symbol;
    // const registryObject$$marker: unique symbol;
    // export interface MarkerCarrier<M1, M2> {
    //     readonly [registryTag$$marker]: M1;
    //     readonly [registryObject$$marker]: M2;
    // }
    static class RegistryMarked extends Code {

        @Override
        public Set<ClassPath> getImports() {
            return Set.of();
        }

        @Override
        public List<String> format(int indent) {
            List<String> lines = new ArrayList<>();
            lines.add("%sconst registryTag$$marker: unique symbol;".formatted(" ".repeat(indent)));
            lines.add("%sconst registryObject$$marker: unique symbol;".formatted(" ".repeat(indent)));
            lines.add("%sexport interface %s<M1, M2> {".formatted(" ".repeat(indent), REGISTRY_MARKED.getClassName()));
            lines.add("%sreadonly [registryTag$$marker]: M1;".formatted(" ".repeat(indent + 4)));
            lines.add("%sreadonly [registryObject$$marker]: M2;".formatted(" ".repeat(indent + 4)));
            lines.add("%s}".formatted(" ".repeat(indent)));
            return lines;
        }
    }


}
