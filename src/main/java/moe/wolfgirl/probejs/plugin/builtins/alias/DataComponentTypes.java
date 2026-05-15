package moe.wolfgirl.probejs.plugin.builtins.alias;

import dev.latvian.mods.kubejs.component.ComponentFunctions;
import dev.latvian.mods.kubejs.component.DataComponentWrapper;
import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.typescript.Documents;
import moe.wolfgirl.probejs.typescript.base.AliasRegistrar;
import moe.wolfgirl.probejs.typescript.base.DocumentRegistrar;
import moe.wolfgirl.probejs.typescript.document.Members;
import moe.wolfgirl.probejs.typescript.document.TypeDecl;
import moe.wolfgirl.probejs.typescript.document.Types;
import moe.wolfgirl.probejs.typescript.document.base.KindAware;
import moe.wolfgirl.probejs.typescript.document.builders.ClassBuilder;
import moe.wolfgirl.probejs.typescript.document.members.MethodDecl;
import moe.wolfgirl.probejs.typescript.document.types.special.NamespacedType;
import moe.wolfgirl.probejs.typescript.transpiler.TypeConverter;
import moe.wolfgirl.probejs.utils.GameUtils;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;

import java.util.*;

public class DataComponentTypes extends ProbeJSPlugin {
    public static final ClassPath DATA_COMPONENT_TYPES = ClassPath.special("types.DataComponentTypes");
    public static final NamespacedType INPUT_MAP = Types.namespaced(DATA_COMPONENT_TYPES, "InputMap");
    public static final NamespacedType OUTPUT_MAP = Types.namespaced(DATA_COMPONENT_TYPES, "OutputMap");
    private static final Set<String> REMOVED_METHODS = Set.of("get", "set", "getOrDefault");

    @Override
    public void transformClass(Documents.ClassDocument document) {
        var classInfo = document.classInfo();
        var classDocument = document.document();
        // ItemStack or FluidStack, but we make it compatible with mixins
        if (ComponentFunctions.class.isAssignableFrom(classInfo.clazz())) {
            // get<T extends keyof OutputMap>(type: T): OutputMap[T]
            // set<T extends keyof InputMap>(type: T, data: InputMap[T]): void
            classDocument.members.removeIf(m -> m instanceof MethodDecl md && REMOVED_METHODS.contains(md.name));
            classDocument.members.add(Members.method("get")
                    .typeParam("T", Types.wrapped("keyof %s", OUTPUT_MAP))
                    .param("type", Types.variable("T"))
                    .returnType(Types.wrapped("%s[T] | null", OUTPUT_MAP))
                    .build());
            classDocument.members.add(Members.method("getOrDefault")
                    .typeParam("T", Types.wrapped("keyof %s", OUTPUT_MAP))
                    .param("type", Types.variable("T"))
                    .param("default", Types.wrapped("%s[T]", OUTPUT_MAP))
                    .returnType(Types.wrapped("%s[T]", OUTPUT_MAP))
                    .build());

            classDocument.members.add(Members.method("set")
                    .param("components", Types.clazz(DataComponentMap.class))
                    .returnType(Types.THIS)
                    .build());
            classDocument.members.add(Members.method("set")
                    .typeParam("T", Types.wrapped("keyof %s", INPUT_MAP))
                    .param("type", Types.variable("T"))
                    .param("data", Types.wrapped("%s[T]", INPUT_MAP))
                    .returnType(Types.THIS)
                    .build());
        }
    }

    @Override
    public void addTypeAlias(AliasRegistrar registrar) {
        registrar.addInputAlias(DataComponentMap.class, Types.raw("Partial").withParams(INPUT_MAP));
        registrar.addInputAlias(DataComponentPatch.class, Types.raw("Partial").withParams(INPUT_MAP));
    }

    @Override
    public void addSpecialDocuments(DocumentRegistrar registrar) {
        MinecraftServer server = GameUtils.getCurrentServer();
        if (server == null) return;
        RegistryAccess registryAccess = server.registryAccess();
        var dataComponentRegistry = registryAccess.registry(Registries.DATA_COMPONENT_TYPE).orElse(null);
        if (dataComponentRegistry == null) return;
        ClassBuilder classBuilder = Members.clazz(DATA_COMPONENT_TYPES).kind(KindAware.Kind.NAMESPACE);
        TypeConverter converter = new TypeConverter();

        var inputMapType = Types.newObject();
        var outputMapType = Types.newObject();

        for (Map.Entry<DataComponentType<?>, TypeInfo> entry : DataComponentWrapper.TYPE_INFOS.get().entrySet()) {
            var key = dataComponentRegistry.getKey(entry.getKey());
            var typeInfo = entry.getValue();
            if (key == null) continue; // Unregistered DataComponentType???
            var type = converter.convertType(typeInfo);
            inputMapType.param(key.toString(), type.markAsInput());
            var oType = converter.convertType(typeInfo);
            outputMapType.param(key.toString(), oType);
        }

        classBuilder.member(new TypeDecl(INPUT_MAP.asClassPath(), inputMapType.build(), false));
        classBuilder.member(new TypeDecl(OUTPUT_MAP.asClassPath(), outputMapType.build(), false));
        registrar.addDocument(DATA_COMPONENT_TYPES, classBuilder.build());
    }
}
