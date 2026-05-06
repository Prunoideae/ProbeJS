package moe.wolfgirl.probejs.plugin.builtins.events;

import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.server.tag.TagKubeEvent;
import dev.latvian.mods.kubejs.server.tag.TagWrapper;
import moe.wolfgirl.probejs.utils.GameUtils;
import moe.wolfgirl.probejs.ClassPath;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.plugin.builtins.alias.RegistryTypes;
import moe.wolfgirl.probejs.typescript.Documents;
import moe.wolfgirl.probejs.typescript.base.DocumentRegistrar;
import moe.wolfgirl.probejs.typescript.document.Members;
import moe.wolfgirl.probejs.typescript.document.Types;
import moe.wolfgirl.probejs.typescript.document.base.KindAware;
import moe.wolfgirl.probejs.typescript.document.members.MethodDecl;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;

import java.util.Set;

public class TagEvents extends ProbeJSPlugin {
    public static final ClassPath SERVER_TAGS = Events.SERVER_EVENTS.append("ServerEvents$Tags");
    public static final ClassPath TAG_EVENT_PROBE = Events.SERVER_EVENTS.append("TagEvent");
    public static final ClassPath TAG_WRAPPER_PROBE = Events.SERVER_EVENTS.append("TagWrapper");

    public static final ClassPath TAG_KUBE_EVENT = new ClassPath(TagKubeEvent.class);
    public static final ClassPath TAG_WRAPPER = new ClassPath(TagWrapper.class);
    private static final Set<String> OVERWRITE_METHODS = Set.of("get", "add", "remove", "removeAll", "removeAllTagsFrom");

    static {
        Events.SKIP_EVENTS.add(Pair.of("ServerEvents", "tags"));
    }

    @Override
    public void addSidedDocuments(DocumentRegistrar registrar) {
        var tagEventDecl = Members.clazz(TAG_EVENT_PROBE).typeParam("T").extendsType(Types.clazz(TAG_KUBE_EVENT));
        var tagWrapperDecl = Members.clazz(TAG_WRAPPER_PROBE).typeParam("T").extendsType(Types.clazz(TAG_WRAPPER));
        var serverTagsDecl = Members.clazz(Events.SERVER_EVENTS.append("ServerEvents")).kind(KindAware.Kind.NAMESPACE).noExport();

        // TagEvent
        tagEventDecl.method("get", mb -> {
            mb.returnType(Types.parameterized(Types.clazz(TAG_WRAPPER_PROBE), Types.variable("T")));
            mb.param("tag", RegistryTypes.RESOLVE_TAG);
        }).method("removeAll", mb -> {
            mb.returnType(Types.parameterized(Types.clazz(TAG_WRAPPER_PROBE), Types.variable("T")));
            mb.param("tag", RegistryTypes.RESOLVE_TAG);
        }).method("add", mb -> {
            mb.returnType(Types.parameterized(Types.clazz(TAG_WRAPPER_PROBE), Types.variable("T")));
            mb.param("tag", RegistryTypes.RESOLVE_TAG);
            mb.param("values", RegistryTypes.RESOLVE_OBJECT.asArray(), true);
        }).method("remove", mb -> {
            mb.returnType(Types.parameterized(Types.clazz(TAG_WRAPPER_PROBE), Types.variable("T")));
            mb.param("tag", RegistryTypes.RESOLVE_TAG);
            mb.param("values", RegistryTypes.RESOLVE_OBJECT.asArray(), true);
        }).method("removeAllTagsFrom", mb -> {
            mb.returnType(Types.VOID);
            mb.param("values", RegistryTypes.RESOLVE_OBJECT.asArray(), true);
        });

        // TagWrapper
        tagWrapperDecl.method("add", mb -> {
            mb.returnType(Types.variable("T"));
            mb.param("values", RegistryTypes.RESOLVE_OBJECT.asArray(), true);
        }).method("remove", mb -> {
            mb.returnType(Types.variable("T"));
            mb.param("values", RegistryTypes.RESOLVE_OBJECT.asArray(), true);
        }).method("removeAll", mb -> mb.returnType(Types.VOID));

        registrar.addDocument(TAG_EVENT_PROBE, tagEventDecl.build());
        registrar.addDocument(TAG_WRAPPER_PROBE, tagWrapperDecl.build());

        // tag events
        MinecraftServer server = GameUtils.getCurrentServer();
        if (server == null) return;
        RegistryAccess registryAccess = server.registryAccess();

        for (ResourceKey<? extends Registry<?>> registry : GameUtils.getRegistries(registryAccess)) {
            ClassPath baseType = RegistryTypes.findRegistryBaseClass(registry);
            if (baseType == null) continue;
            String extraName = registry.location().getNamespace().equals("minecraft") ? registry.location().getPath() : registry.location().toString();
            serverTagsDecl.method("tags", mb -> {
                mb.param("type", Types.literal(extraName));
                mb.param("handler", Types.lambda(lb -> {
                    lb.param("event", Types.clazz(TAG_EVENT_PROBE).withParams(Types.clazz(baseType).asMaybeGeneric()));
                }));
            });
        }

        registrar.addGlobal(SERVER_TAGS, serverTagsDecl.build());
    }

    @Override
    public void transformClass(Documents.ClassDocument document) {
        var classInfo = document.classInfo();
        var classDocument = document.document();
        if (classInfo.classPath().equals(TAG_KUBE_EVENT) || classInfo.classPath().equals(TAG_WRAPPER)) {
            classDocument.members.removeIf(code -> code instanceof MethodDecl methodDecl && OVERWRITE_METHODS.contains(methodDecl.name));
        }
    }


}
