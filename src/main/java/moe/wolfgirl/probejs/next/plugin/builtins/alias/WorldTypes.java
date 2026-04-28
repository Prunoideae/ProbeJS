package moe.wolfgirl.probejs.next.plugin.builtins.alias;

import moe.wolfgirl.probejs.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.next.typescript.base.AliasRegistrar;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public class WorldTypes extends ProbeJSPlugin {

    @Override
    public void addTypeAlias(AliasRegistrar registrar) {
        registrar.addInputAlias(ItemLike.class, Item.class);

    }
}
