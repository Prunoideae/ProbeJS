package moe.wolfgirl.probejs.next;

import dev.architectury.platform.Mod;
import dev.architectury.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GlobalStates {
    public static final Set<String> LANG_KEYS = new HashSet<>();
    public static final Set<String> RECIPE_IDS = new HashSet<>();
    public static final Set<String> LOOT_TABLES = new HashSet<>();

    public static final Supplier<Set<String>> RAW_TEXTURES = () ->
            Minecraft.getInstance()
                    .getTextureManager()
                    .byPath
                    .keySet()
                    .stream()
                    .map(ResourceLocation::toString)
                    .collect(Collectors.toSet());

    public static final Supplier<Set<String>> TEXTURES = () ->
            Minecraft.getInstance()
                    .getModelManager()
                    .getAtlas(InventoryMenu.BLOCK_ATLAS)
                    .texturesByName
                    .keySet()
                    .stream().map(ResourceLocation::toString)
                    .collect(Collectors.toSet());

    public static final Supplier<Set<String>> MODS = () ->
            Platform.getMods()
                    .stream()
                    .map(Mod::getModId)
                    .collect(Collectors.toSet());
}
