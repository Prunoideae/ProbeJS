package moe.wolfgirl.probejs;

import com.google.gson.JsonObject;
import dev.latvian.mods.kubejs.util.Lazy;
import moe.wolfgirl.probejs.misc.gui.DumpScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.core.BlockPos;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModInfo;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class GameStates {
    public static final Set<String> MIXIN_LANG_KEYS = new HashSet<>();
    public static final Map<String, JsonObject> RECIPE_IDS = new HashMap<>();
    public static final Set<String> LOOT_TABLES = new HashSet<>();
    public static DumpScreen DUMP_SCREEN = null;
    // It is better to call DUMP_STATE directly rather than checking if dump screen is null, because dump state ensures
    // null-check when inside the dumping lifecycle. If used outside of dumping, use DUMP_SCREEN with null-check instead.
    public static DumpState DUMP_STATE = null;

    public static final Supplier<Set<String>> LANG_KEYS = () ->
            Language.getInstance() instanceof ClientLanguage clientLanguage ?
                    clientLanguage.storage.keySet()
                            .stream()
                            .filter(s -> s.toLowerCase().equals(s) && !s.startsWith("_"))
                            .collect(Collectors.toSet()) :
                    Set.of();

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

    public static final Lazy<ClientLanguage> DEFAULT_LANGUAGE = Lazy.of(() -> {
        ResourceManager manager = Minecraft.getInstance().getResourceManager();
        return ClientLanguage.loadFrom(manager, List.of(Language.DEFAULT), false);
    });
}
