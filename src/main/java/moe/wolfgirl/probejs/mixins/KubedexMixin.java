package moe.wolfgirl.probejs.mixins;

import dev.latvian.mods.kubejs.kubedex.KubedexPayloadHandler;
import moe.wolfgirl.probejs.GlobalStates;
import moe.wolfgirl.probejs.utils.JsonUtils;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(value = KubedexPayloadHandler.class, remap = false)
public class KubedexMixin {


    @Inject(method = "itemStacks", remap = false, at = @At("RETURN"))
    private static void handle(ServerPlayer player, Collection<ItemStack> stacks, CallbackInfo ci) {
        var ops = player.server.registryAccess().createSerializationContext(NbtOps.INSTANCE);
        if (GlobalStates.SERVER != null) {
            GlobalStates.SERVER.broadcast("accept_items", JsonUtils.parseObject(
                    stacks.stream().map(s -> s.kjs$toItemString0(ops)).toList()
            ));
        }
    }
}
