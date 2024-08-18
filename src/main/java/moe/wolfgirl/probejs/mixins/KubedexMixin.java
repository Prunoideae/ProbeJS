package moe.wolfgirl.probejs.mixins;

import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.latvian.mods.kubejs.kubedex.KubedexPayloadHandler;
import dev.latvian.mods.kubejs.web.local.KubeJSWeb;
import moe.wolfgirl.probejs.GlobalStates;
import moe.wolfgirl.probejs.utils.JsonUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(value = KubedexPayloadHandler.class, remap = false)
public class KubedexMixin {


    @Inject(method = "itemStacks", remap = false, at = @At("RETURN"))
    private static void handleItem(ServerPlayer player, Collection<ItemStack> stacks, CallbackInfo ci) {
        var ops = player.server.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        if (GlobalStates.KUBEDEX != null) {
            KubeJSWeb.broadcastEvent(GlobalStates.KUBEDEX, "item", () -> {
                var jsonArray = new JsonArray();
                for (ItemStack stack : stacks) {
                    jsonArray.add(stack.kjs$toItemString0(ops));
                }
                return jsonArray;
            });
        }
    }

    @Inject(method = "block", remap = false, at = @At("RETURN"))
    private static void handleBlock(ServerPlayer player, BlockPos pos, CallbackInfo ci) {
        if (GlobalStates.KUBEDEX != null) {
            Registry<Block> blockRegistry = BuiltInRegistries.BLOCK;
            var blockState = player.level().getBlockState(pos);

            if (blockState.isAir()) return;
            KubeJSWeb.broadcastEvent(GlobalStates.KUBEDEX, "block", () -> {
                JsonObject payload = new JsonObject();

                // Block ID
                payload.addProperty("id", blockRegistry.getKey(blockState.getBlock()).toString());

                // Properties
                if (!blockState.getValues().isEmpty()) {
                    JsonArray properties = new JsonArray();
                    blockState.getValues()
                            .entrySet()
                            .stream()
                            .map(StateHolder.PROPERTY_ENTRY_TO_STRING_FUNCTION)
                            .map(JsonPrimitive::new)
                            .forEach(properties::add);
                    payload.add("properties", properties);
                }

                return payload;
            });
        }
    }
}
