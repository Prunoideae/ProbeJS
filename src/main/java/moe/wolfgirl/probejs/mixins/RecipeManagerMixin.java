package moe.wolfgirl.probejs.mixins;

import com.google.gson.JsonObject;
import moe.wolfgirl.probejs.GameStates;
import moe.wolfgirl.probejs.ProbeConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = RecipeManager.class, priority = 900)
public class RecipeManagerMixin {
    @Inject(method = "apply*", at = @At("HEAD"))
    private void apply(Map<ResourceLocation, JsonObject> map, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        if (!ProbeConfig.INSTANCE.enabled.get()) return;
        GameStates.RECIPE_IDS.clear();
        for (Map.Entry<ResourceLocation, JsonObject> entry : map.entrySet()) {
            ResourceLocation key = entry.getKey();
            JsonObject value = entry.getValue();
            if (!key.getPath().startsWith("kjs_")) {
                GameStates.RECIPE_IDS.put(key.toString(), value);
            }
        }
    }
}
