package moe.wolfgirl.probejs.mixins;

import com.mojang.serialization.DynamicOps;
import moe.wolfgirl.probejs.GlobalStates;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(LootDataType.class)
public abstract class LootTableMixin<T> {
    @Inject(method = "deserialize", at = @At("RETURN"))
    public <V> void apply(ResourceLocation resourceLocation, DynamicOps<V> ops, V value, CallbackInfoReturnable<Optional<T>> cir) {
        GlobalStates.LOOT_TABLES.add(resourceLocation.toString());
    }
}
