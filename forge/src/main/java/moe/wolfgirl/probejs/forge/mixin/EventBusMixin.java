package moe.wolfgirl.probejs.forge.mixin;

import moe.wolfgirl.probejs.forge.GlobalStates;
import moe.wolfgirl.probejs.forge.ProbeJSForge;
import net.minecraftforge.eventbus.EventBus;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.IEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EventBus.class)
public class EventBusMixin {

    /*
     * So we sneak peek all registered event listeners
     */
    @Inject(method = "post(Lnet/minecraftforge/eventbus/api/Event;)Z", at = @At("HEAD"), remap = false)
    public void addToListeners(Event event, CallbackInfoReturnable<Boolean> cir) {
        GlobalStates.KNOWN_EVENTS.add(event.getClass());
    }
}
