package moe.wolfgirl.probejs.mixins;


import moe.wolfgirl.probejs.GlobalStates;
import net.neoforged.bus.EventBus;
import net.neoforged.bus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EventBus.class)
public class EventBusMixin {

    /*
     * So we sneak peek all registered event listeners
     */
    @Inject(method = "post(Lnet/neoforged/bus/api/Event;)Lnet/neoforged/bus/api/Event;", at = @At("HEAD"), remap = false)
    public void addToListeners(Event event, CallbackInfoReturnable<Boolean> cir) {
        GlobalStates.KNOWN_EVENTS.add(event.getClass());
    }
}
