package moe.wolfgirl.probejs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.client.resources.language.LanguageInfo;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GlobalStates {
    public static final Set<String> MIXIN_LANG_KEYS = new HashSet<>();
    public static final Set<String> RECIPE_IDS = new HashSet<>();
    public static final Set<String> LOOT_TABLES = new HashSet<>();

    public static final Supplier<Set<String>> LANG_KEYS = () -> {
        Set<String> keys;
        synchronized (MIXIN_LANG_KEYS) {
            keys = new HashSet<>(MIXIN_LANG_KEYS);
        }
        Minecraft mc = Minecraft.getInstance();
        LanguageManager manager = mc.getLanguageManager();
        LanguageInfo english = manager.getLanguage("en_us");
        if (english == null) return keys;

        ClientLanguage clientLanguage = ClientLanguage.loadFrom(
                mc.getResourceManager(),
                List.of("en_us"),
                english.bidirectional()
        );
        keys.addAll(clientLanguage.storage.keySet());
        return keys;
    };

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
            ModList.get()
                    .getMods()
                    .stream()
                    .map(IModInfo::getModId)
                    .collect(Collectors.toSet());

    // For probing stuffs
    public static BlockPos LAST_RIGHTCLICKED = null;
    public static Entity LAST_ENTITY = null;
}
