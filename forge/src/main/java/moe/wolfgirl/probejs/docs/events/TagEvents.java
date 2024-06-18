package moe.wolfgirl.probejs.docs.events;

import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.server.tag.TagEventJS;
import dev.latvian.mods.kubejs.server.tag.TagWrapper;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;
import moe.wolfgirl.probejs.lang.typescript.code.member.ClassDecl;
import moe.wolfgirl.probejs.lang.typescript.code.ts.MethodDeclaration;
import moe.wolfgirl.probejs.lang.typescript.code.ts.Statements;
import moe.wolfgirl.probejs.lang.typescript.code.ts.Wrapped;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import moe.wolfgirl.probejs.utils.NameUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Map;

public class TagEvents extends ProbeJSPlugin {
    public static final ClassPath TAG_EVENT = new ClassPath("moe.wolfgirl.probejs.generated.TagEventProbe");
    public static final ClassPath TAG_WRAPPER = new ClassPath("moe.wolfgirl.probejs.generated.TagWrapperProbe");

    // Create TagEventProbe<T, I> and TagWrapperProbe<T, I>
    // Generate string overrides for all registry types
    // tags(extra: "item", handler: (event: TagEventProbe<Special.ItemTag, Special.Item>) => void)

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        if (scriptDump.scriptType != ScriptType.SERVER) return;

        BaseType eventType = Types.type(TAG_EVENT);
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null) return;
        RegistryAccess registryAccess = currentServer.registryAccess();

        Wrapped.Namespace groupNamespace = new Wrapped.Namespace("ServerEvents");
        for (ResourceKey<? extends Registry<?>> key : RegistryInfo.MAP.keySet()) {
            Registry<?> registry = registryAccess.registry(key).orElse(null);
            if (registry == null) continue;

            String typeName = "Special." + NameUtils.rlToTitle(key.location().getPath());
            String tagName = typeName + "Tag";
            String extraName = key.location().getNamespace().equals("minecraft") ?
                    key.location().getPath() :
                    key.location().toString();
            MethodDeclaration declaration = Statements.method("tags")
                    .param("extra", Types.literal(extraName))
                    .param("handler", Types.lambda()
                            .param("event", Types.parameterized(
                                    eventType,
                                    Types.primitive(tagName), Types.primitive(typeName)
                            ))
                            .build()
                    )
                    .build();
            groupNamespace.addCode(declaration);
        }

        scriptDump.addGlobal("tag_events", groupNamespace);
    }

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        if (scriptDump.scriptType != ScriptType.SERVER) return;

        BaseType wrapperType = Types.type(TAG_WRAPPER);

        ClassDecl tagEventProbe = Statements.clazz(TAG_EVENT.getName())
                .superClass(Types.type(TagEventJS.class))
                .typeVariables("T", "I")
                .method("add", builder -> builder
                        .returnType(Types.parameterized(wrapperType, Types.generic("T"), Types.generic("I")))
                        .param("tag", Types.generic("T"))
                        .param("filters", Types.generic("I").asArray(), false, true)
                )
                .method("remove", builder -> builder
                        .returnType(Types.parameterized(wrapperType, Types.generic("T"), Types.generic("I")))
                        .param("tag", Types.generic("T"))
                        .param("filters", Types.generic("I").asArray(), false, true)
                )
                .build();
        TypeScriptFile eventFile = new TypeScriptFile(TAG_EVENT);
        eventFile.addCode(tagEventProbe);
        globalClasses.put(TAG_EVENT, eventFile);

        ClassDecl tagWrapperProbe = Statements.clazz(TAG_WRAPPER.getName())
                .superClass(Types.type(TagWrapper.class))
                .typeVariables("T", "I")
                .method("add", builder -> builder
                        .returnType(Types.THIS)
                        .param("filters", Types.generic("I").asArray(), false, true)
                )
                .method("remove", builder -> builder
                        .returnType(Types.THIS)
                        .param("filters", Types.generic("I").asArray(), false, true)
                )
                .build();
        TypeScriptFile wrapperFile = new TypeScriptFile(TAG_WRAPPER);
        wrapperFile.addCode(tagWrapperProbe);
        globalClasses.put(TAG_WRAPPER, wrapperFile);

    }
}
