package moe.wolfgirl.mixins;

import moe.wolfgirl.specials.special.FormatterLootTable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataManager;
import net.minecraft.world.level.storage.loot.LootDataType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Mixin(LootDataManager.class)
public class LootTableMixin {
    @Inject(method = "apply*", at = @At("RETURN"))
    public void apply(Map<LootDataType<?>, Map<ResourceLocation, ?>> parsedMap, CallbackInfo ci) {
        FormatterLootTable.LOOT_TABLES.clear();
        FormatterLootTable.LOOT_TABLES.addAll(parsedMap.values().stream().map(Map::keySet).flatMap(Collection::stream).collect(Collectors.toSet()));
    }
}
