package moe.wolfgirl.probejs.docs.assignments;

import dev.latvian.mods.kubejs.registry.RegistryType;
import moe.wolfgirl.probejs.ProbeConfig;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
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
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.utils.NameUtils;
import moe.wolfgirl.probejs.utils.RegistryUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;

/**
 * Assign types to all the registry types
 */
public class RegistryTypes extends ProbeJSPlugin {
    public static final String LITERAL_FIELD = "probejsInternal$$Literal";
    public static final String TAG_FIELD = "probejsInternal$$Tag";
    public static final String OF_TYPE_DECL = "T extends { %s: infer U } ? U : never";

    public static Map<ResourceKey<? extends Registry<?>>, Class<?>> PREDEFINED_TYPES = Map.of(
            Registries.DIMENSION, Level.class
    );

    @Override
    public void assignType(ScriptDump scriptDump) {
        List<BaseType> registryNames = new ArrayList<>();
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null) return;
        RegistryAccess access = currentServer.registryAccess();

        for (ResourceKey<? extends Registry<?>> key : RegistryUtils.getRegistries(access)) {
            Class<?> assigned;
            if (PREDEFINED_TYPES.containsKey(key)) {
                assigned = PREDEFINED_TYPES.get(key);
            } else {
                RegistryType<?> type = RegistryType.ofKey(key);
                if (type == null) continue;
                assigned = type.baseClass();
            }
            String typeName = NameUtils.registryToName(key);
            scriptDump.assignType(assigned, Types.primitive("Special.%s".formatted(typeName)));
            registryNames.add(Types.literal(key.location().toString()));
        }

        // ResourceKey<T> to Special.LiteralOf<T>
        assignRegistryType(scriptDump, ResourceKey.class, "Special.LiteralOf", "T");

        // Also holder
        assignRegistryType(scriptDump, Holder.class, "Special.LiteralOf", "T");
        // Registries (why?)
        scriptDump.assignType(Registry.class, Types.or(registryNames.toArray(BaseType[]::new)));
        // TagKey<T> to Special.TagOf<T>
        assignRegistryType(scriptDump, TagKey.class, "Special.TagOf", "T");
        // HolderSet<T> to Special.LiteralOf<T> | Special.TagOf<T>
        scriptDump.assignType(HolderSet.class, Types.or(
                Types.parameterized(Types.primitive("Special.LiteralOf"), Types.generic("T")).asArray(),
                Types.primitive("`#${Special.TagOf<T>}`").asArray(),
                Types.ignoreContext(Types.parameterized(Types.type(HolderSet.class), Types.generic("T")), BaseType.FormatType.RETURN)
        ));
    }

    private static void assignRegistryType(ScriptDump scriptDump, Class<?> type, String literalType, String symbol) {
        scriptDump.assignType(type, Types.parameterized(Types.primitive(literalType), Types.generic(symbol)));
        scriptDump.assignType(type, Types.ignoreContext(Types.parameterized(Types.type(type), Types.generic(symbol)), BaseType.FormatType.RETURN));
    }

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        boolean enabled = ProbeConfig.INSTANCE.complete.get();

        Wrapped.Namespace special = new Wrapped.Namespace("Special");
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null) return;
        RegistryAccess registryAccess = currentServer.registryAccess();

        for (ResourceKey<? extends Registry<?>> key : RegistryUtils.getRegistries(registryAccess)) {
            Registry<?> registry = registryAccess.registry(key).orElse(null);
            if (registry == null) continue;
            createTypes(special, key, registry, enabled);
        }
        createTypes(special, BuiltInRegistries.REGISTRY.key(), BuiltInRegistries.REGISTRY, enabled);

        // Expose LiteralOf<T> and TagOf<T>
        TypeDecl literalOf = new TypeDecl("LiteralOf<T>", Types.primitive(OF_TYPE_DECL.formatted(LITERAL_FIELD)));
        TypeDecl tagOf = new TypeDecl("TagOf<T>", Types.primitive(OF_TYPE_DECL.formatted(TAG_FIELD)));
        special.addCode(literalOf);
        special.addCode(tagOf);

        scriptDump.addGlobal("registry_type", special);
    }

    private static void createTypes(Wrapped.Namespace special, ResourceKey<? extends Registry<?>> key, Registry<?> registry, boolean enabled) {
        List<String> entryNames = new ArrayList<>();
        for (ResourceLocation entryName : registry.keySet()) {
            if (entryName.getNamespace().equals("minecraft"))
                entryNames.add(entryName.getPath());
            entryNames.add(entryName.toString());
        }

        BaseType types = enabled ? Types.or(entryNames.stream().map(Types::literal).toArray(BaseType[]::new)) : Types.STRING;
        String typeName = NameUtils.registryToName(key);

        TypeDecl typeDecl = new TypeDecl(typeName, types);
        special.addCode(typeDecl);

        BaseType[] tagNames = registry.getTagNames()
                .map(TagKey::location)
                .map(ResourceLocation::toString)
                .map(Types::literal)
                .toArray(BaseType[]::new);

        BaseType tagTypes = enabled ? Types.or(tagNames) : Types.STRING;
        String tagName = typeName + "Tag";

        TypeDecl tagDecl = new TypeDecl(tagName, tagTypes);
        special.addCode(tagDecl);
    }

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null) return;
        RegistryAccess registryAccess = currentServer.registryAccess();

        // We inject literal and tag into registry types
        for (ResourceKey<? extends Registry<?>> key : RegistryUtils.getRegistries(registryAccess)) {
            RegistryType<?> type = RegistryType.ofKey(key);
            if (type == null) continue;
            makeClassModifications(globalClasses, key, type.baseClass());
        }
        makeClassModifications(globalClasses, BuiltInRegistries.REGISTRY.key(), Registry.class);
        makeClassModifications(globalClasses, Registries.DIMENSION, Level.class);
    }

    private static void makeClassModifications(Map<ClassPath, TypeScriptFile> globalClasses, ResourceKey<? extends Registry<?>> key, Class<?> baseClass) {
        TypeScriptFile typeScriptFile = globalClasses.get(new ClassPath(baseClass));
        if (typeScriptFile == null) return;
        ClassDecl classDecl = typeScriptFile.findCode(ClassDecl.class).orElse(null);
        if (classDecl == null) return;

        String typeName = NameUtils.registryToName(key);
        String tagName = typeName + "Tag";

        var literalField = new FieldDecl(LITERAL_FIELD, Types.primitive("Special.%s".formatted(typeName)));
        literalField.addComment("This field is a type stub generated by ProbeJS and shall not be used in any sense.");
        classDecl.bodyCode.add(literalField);
        var tagField = new FieldDecl(TAG_FIELD, Types.primitive("Special.%s".formatted(tagName)));
        tagField.addComment("This field is a type stub generated by ProbeJS and shall not be used in any sense.");
        classDecl.bodyCode.add(tagField);
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        Set<Class<?>> registryObjectClasses = new HashSet<>();
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null) return registryObjectClasses;
        RegistryAccess registryAccess = currentServer.registryAccess();

        for (ResourceKey<? extends Registry<?>> value : RegistryUtils.getRegistries(registryAccess)) {
            Registry<?> registry = registryAccess.registry(value).orElse(null);
            if (registry == null) continue;
            try {
                for (Object o : registry) {
                    registryObjectClasses.add(o.getClass());
                }
            } catch (Throwable t) {
                ProbeJS.LOGGER.error("Unable to fetch registry info for %s".formatted(value));
            }
            RegistryType<?> type = RegistryType.ofKey(value);
            if (type == null) continue;
            registryObjectClasses.add(type.baseClass());
        }
        return registryObjectClasses;
    }

    @Override
    public void addVSCodeSnippets(SnippetDump dump) {
        MinecraftServer currentServer = ServerLifecycleHooks.getCurrentServer();
        if (currentServer == null) return;
        RegistryAccess registryAccess = currentServer.registryAccess();

        for (ResourceKey<? extends Registry<?>> key : RegistryUtils.getRegistries(registryAccess)) {
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
                    .map(s -> "#" + s)
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
