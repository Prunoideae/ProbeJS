package moe.wolfgirl.probejs.next.plugin.builtins.alias;

import moe.wolfgirl.probejs.legacy.utils.GameUtils;
import moe.wolfgirl.probejs.legacy.utils.NameUtils;
import moe.wolfgirl.probejs.legacy.utils.RegistryUtils;
import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.next.typescript.base.AliasRegistrar;
import moe.wolfgirl.probejs.next.typescript.base.DocumentRegistrar;
import moe.wolfgirl.probejs.next.typescript.document.Members;
import moe.wolfgirl.probejs.next.typescript.document.TypeDecl;
import moe.wolfgirl.probejs.next.typescript.document.Types;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.document.base.KindAware;
import moe.wolfgirl.probejs.next.typescript.document.base.Type;
import moe.wolfgirl.probejs.next.typescript.document.builders.ClassBuilder;
import moe.wolfgirl.probejs.next.typescript.document.types.special.NamespacedType;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RegistryTypes extends ProbeJSPlugin {
    public static final ClassPath REGISTRY_TYPES = ClassPath.special("types.RegistryTypes");
    public static Map<ResourceKey<? extends Registry<?>>, Class<?>> PREDEFINED_TYPES = Map.of(
            Registries.DIMENSION, Level.class
    );

    public static final String TAG_MARKER = "probejs$tag_marker";
    public static final String OBJECT_MARKER = "probejs$object_marker";
    public static final String TAG_RESOLVER = "T extends {%s: infer M} ? M : never".formatted(TAG_MARKER);
    public static final String OBJECT_RESOLVER = "T extends {%s: infer M} ? M : never".formatted(OBJECT_MARKER);

    @Override
    public void addTypeAlias(AliasRegistrar registrar) {
        super.addTypeAlias(registrar);
    }

    @Override
    public void addSpecialDocuments(DocumentRegistrar registrar) {
        MinecraftServer currentServer = GameUtils.getCurrentServer();
        if (currentServer == null) return;
        RegistryAccess registryAccess = currentServer.registryAccess();

        ClassBuilder registryTypes = Members.clazz(REGISTRY_TYPES)
                .kind(KindAware.Kind.NAMESPACE);

        for (ResourceKey<? extends Registry<?>> key : RegistryUtils.getRegistries(registryAccess)) {
            var registry = registryAccess.registry(key).orElse(null);
            if (registry == null) continue;
            registryTypes.member(makeType(key, registry));
        }

        registrar.addDocument(REGISTRY_TYPES, registryTypes.build());
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

    // export interface Name extends RegistryMarked<Object, Tag> {}
    static class InterfaceMixin extends Code {
        private static final NamespacedType REGISTRY_MARKED = Types.namespaced(REGISTRY_TYPES, "RegistryMarked");
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
            imports.addAll(REGISTRY_MARKED.getImports());
            imports.addAll(tagType.getImports());
            imports.addAll(objectType.getImports());
            return imports;
        }

        @Override
        public List<String> format(int indent) {
            var extendsType = REGISTRY_MARKED.withParams(objectType, tagType);
            return List.of("%sexport interface %s extends %s {}".formatted(
                    " ".repeat(indent),
                    classPath.getClassName(),
                    extendsType.first()
            ));
        }
    }
}
