package moe.wolfgirl.probejs.mixins;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.net.RequestItemKubedexPayload;
import moe.wolfgirl.probejs.GlobalStates;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.utils.JsonUtils;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashSet;
import java.util.List;

@Mixin(value = RequestItemKubedexPayload.class, remap = false)
public class KubedexMixin {

    @Shadow
    @Final
    private List<ItemStack> stacks;

    @Shadow
    @Final
    private List<Integer> slots;

    @Inject(method = "handle", remap = false, at = @At("RETURN"))
    public void handle(IPayloadContext ctx, CallbackInfo ci) {
        if (ctx.player() instanceof ServerPlayer serverPlayer && serverPlayer.hasPermissions(2)) {
            ctx.enqueueWork(() -> {
                var ops = serverPlayer.server.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                var allStacks = new LinkedHashSet<>(stacks);

                for (int s : slots) {
                    if (s >= 0 && s < serverPlayer.getInventory().getContainerSize()) {
                        var item = serverPlayer.getInventory().getItem(s);

                        if (!item.isEmpty()) {
                            allStacks.add(item);
                        }
                    }
                }

                if (GlobalStates.SERVER != null) {
                    GlobalStates.SERVER.broadcast("accept_items", JsonUtils.parseObject(
                            allStacks.stream().map(s -> s.kjs$toItemString0(ops)).toList()
                    ));
                }
            });
        }
    }
}
