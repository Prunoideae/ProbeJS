package moe.wolfgirl.probejs.docs;

import dev.latvian.mods.kubejs.registry.RegistryInfo;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.lang.snippet.Snippet;
import moe.wolfgirl.probejs.lang.snippet.SnippetDump;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;
import moe.wolfgirl.probejs.lang.typescript.code.member.ClassDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.FieldDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.TypeDecl;
import moe.wolfgirl.probejs.lang.typescript.code.ts.Wrapped;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import moe.wolfgirl.probejs.utils.NameUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;

/**
 * Assign types to all the registry types
 */
public class RegistryTypes extends ProbeJSPlugin {
    public static final String LITERAL_FIELD = "probejsInternal$$Literal";
    public static final String TAG_FIELD = "probejsInternal$$Tag";
    public static final String OF_TYPE_DECL = "T extends { %s: infer U } ? U : string";

    @Override
    public void assignType(ScriptDump scriptDump) {
        List<BaseType> registryNames = new ArrayList<>();

        for (Map.Entry<ResourceKey<? extends Registry<?>>, RegistryInfo<?>> entry : RegistryInfo.MAP.entrySet()) {
            ResourceKey<? extends Registry<?>> key = entry.getKey();
            RegistryInfo<?> info = entry.getValue();

            if (info.getVanillaRegistry() == null) continue;

            String typeName = NameUtils.rlToTitle(key.location().getPath());
            scriptDump.assignType(info.objectBaseClass, Types.primitive("Special.%s".formatted(typeName)));
            registryNames.add(Types.literal(key.location().toString()));
        }

        // ResourceKey<T> to Special.LiteralOf<T>
        scriptDump.assignType(ResourceKey.class, Types.parameterized(Types.primitive("Special.LiteralOf"), Types.generic("T")));
        // Also holder
        scriptDump.assignType(Holder.class, Types.parameterized(Types.primitive("Special.LiteralOf"), Types.generic("T")));
        // Registries (why?)
        scriptDump.assignType(Registry.class, Types.or(registryNames.toArray(BaseType[]::new)));
        // TagKey<T> to Special.TagOf<T>
        scriptDump.assignType(TagKey.class, Types.parameterized(Types.primitive("Special.TagOf"), Types.generic("T")));
    }

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        Wrapped.Namespace special = new Wrapped.Namespace("Special");
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null) return;
        RegistryAccess registryAccess = currentServer.registryAccess();

        for (ResourceKey<? extends Registry<?>> key : RegistryInfo.MAP.keySet()) {
            Registry<?> registry = registryAccess.registry(key).orElse(null);
            if (registry == null) continue;

            List<String> entryNames = new ArrayList<>();
            for (ResourceLocation entryName : registry.keySet()) {
                if (entryName.getNamespace().equals("minecraft"))
                    entryNames.add(entryName.getPath());
                entryNames.add(entryName.toString());
            }

            BaseType types = Types.or(entryNames.stream().map(Types::literal).toArray(BaseType[]::new));
            String typeName = NameUtils.rlToTitle(key.location().getPath());

            TypeDecl typeDecl = new TypeDecl(typeName, types);
            special.addCode(typeDecl);

            BaseType[] tagNames = registry.getTagNames()
                    .map(TagKey::location)
                    .map(ResourceLocation::toString)
                    .map(Types::literal)
                    .toArray(BaseType[]::new);

            BaseType tagTypes = Types.or(tagNames);
            String tagName = typeName + "Tag";

            TypeDecl tagDecl = new TypeDecl(tagName, tagTypes);
            special.addCode(tagDecl);
        }

        // Expose LiteralOf<T> and TagOf<T>
        TypeDecl literalOf = new TypeDecl("LiteralOf<T>", Types.primitive(OF_TYPE_DECL.formatted(LITERAL_FIELD)));
        TypeDecl tagOf = new TypeDecl("TagOf<T>", Types.primitive(OF_TYPE_DECL.formatted(TAG_FIELD)));
        special.addCode(literalOf);
        special.addCode(tagOf);

        scriptDump.addGlobal("registry_type", special);
    }

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        // We inject literal and tag into registry types
        for (Map.Entry<ResourceKey<? extends Registry<?>>, RegistryInfo<?>> entry : RegistryInfo.MAP.entrySet()) {
            ResourceKey<? extends Registry<?>> key = entry.getKey();
            RegistryInfo<?> info = entry.getValue();

            TypeScriptFile typeScriptFile = globalClasses.get(new ClassPath(info.objectBaseClass));
            if (typeScriptFile == null) continue;
            ClassDecl classDecl = typeScriptFile.findCode(ClassDecl.class).orElse(null);
            if (classDecl == null) continue;

            String typeName = NameUtils.rlToTitle(key.location().getPath());
            String tagName = typeName + "Tag";


            var literalField = new FieldDecl(LITERAL_FIELD, Types.primitive("Special.%s".formatted(typeName)));
            literalField.addComment("This field is a type stub generated by ProbeJS and shall not be used in any sense.");
            classDecl.fields.add(literalField);
            var tagField = new FieldDecl(TAG_FIELD, Types.primitive("Special.%s".formatted(tagName)));
            tagField.addComment("This field is a type stub generated by ProbeJS and shall not be used in any sense.");
            classDecl.fields.add(tagField);
        }
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        Set<Class<?>> registryObjectClasses = new HashSet<>();
        for (RegistryInfo<?> value : RegistryInfo.MAP.values()) {
            Registry<?> registry = value.getVanillaRegistry();
            if (registry == null) continue;
            for (Object o : registry) {
                registryObjectClasses.add(o.getClass());
            }
            registryObjectClasses.add(value.objectBaseClass);
        }
        return registryObjectClasses;
    }

    @Override
    public void addVSCodeSnippets(SnippetDump dump) {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null) return;
        RegistryAccess registryAccess = currentServer.registryAccess();

        for (ResourceKey<? extends Registry<?>> key : RegistryInfo.MAP.keySet()) {
            Registry<?> registry = registryAccess.registry(key).orElse(null);
            if (registry == null) continue;

            List<String> entries = registry.keySet()
                    .stream()
                    .map(ResourceLocation::toString)
                    .toList();
            if (entries.isEmpty()) continue;

            String registryName = key.location().getNamespace().equals("minecraft") ?
                    key.location().getPath() :
                    key.location().toString();

            Snippet registrySnippet = dump.snippet("probejs$$" + key.location());
            registrySnippet.prefix("@%s".formatted(registryName))
                    .description("All available items in the registry \"%s\"".formatted(key.location()))
                    .literal("\"")
                    .choices(entries)
                    .literal("\"");

            List<String> tags = registry.getTagNames()
                    .map(TagKey::location)
                    .map(ResourceLocation::toString)
                    .toList();
            if (tags.isEmpty()) continue;

            Snippet tagSnippet = dump.snippet("probejs_tag$$" + key.location());
            tagSnippet.prefix("@%s_tag".formatted(registryName))
                    .description("All available tags in the registry \"%s\", no # is added".formatted(key.location()))
                    .literal("\"")
                    .choices(tags)
                    .literal("\"");
        }
    }
}
