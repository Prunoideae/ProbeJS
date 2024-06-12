package moe.wolfgirl.probejs.next.docs;

import dev.architectury.utils.GameInstance;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import moe.wolfgirl.probejs.next.ScriptDump;
import moe.wolfgirl.probejs.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.next.typescript.code.member.TypeDecl;
import moe.wolfgirl.probejs.next.typescript.code.ts.Wrapped;
import moe.wolfgirl.probejs.next.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.next.typescript.code.type.Types;
import moe.wolfgirl.probejs.next.utils.NameUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.TagKey;

import java.util.*;

/**
 * Assign types to all the registry types
 */
public class RegistryTypes extends ProbeJSPlugin {

    @Override
    public void assignType(ScriptDump scriptDump) {
        for (Map.Entry<ResourceKey<? extends Registry<?>>, RegistryInfo<?>> entry : RegistryInfo.MAP.entrySet()) {
            ResourceKey<? extends Registry<?>> key = entry.getKey();
            RegistryInfo<?> info = entry.getValue();

            if (info.getVanillaRegistry() == null) continue;

            String typeName = NameUtils.rlToTitle(key.location().getPath());
            scriptDump.assignType(info.objectBaseClass, Types.primitive("Special.%s".formatted(typeName)));
        }
    }

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        Wrapped.Namespace special = new Wrapped.Namespace("Special");
        MinecraftServer currentServer = GameInstance.getServer();
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

        scriptDump.addGlobal("registry_type", special);
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
        }
        return registryObjectClasses;
    }
}
