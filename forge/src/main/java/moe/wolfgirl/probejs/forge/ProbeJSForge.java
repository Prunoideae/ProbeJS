package moe.wolfgirl.probejs.forge;

import moe.wolfgirl.probejs.ProbeJS;
import dev.architectury.platform.forge.EventBuses;
import moe.wolfgirl.probejs.forge.docs.ForgeEventDoc;
import moe.wolfgirl.probejs.next.docs.ProbeBuiltinDocs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ProbeJS.MOD_ID)
public class ProbeJSForge {
    public static IEventBus MOD_EVENT_BUS;
    public static IEventBus EVENT_BUS;

    public ProbeJSForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(ProbeJS.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        ProbeJS.init();

        MOD_EVENT_BUS = FMLJavaModLoadingContext.get().getModEventBus();
        EVENT_BUS = MinecraftForge.EVENT_BUS;
        ProbeBuiltinDocs.BUILTIN_DOCS.add(new ForgeEventDoc());
    }
}
