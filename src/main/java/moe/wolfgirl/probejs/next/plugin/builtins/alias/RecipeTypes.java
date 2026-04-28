package moe.wolfgirl.probejs.next.plugin.builtins.alias;

import dev.latvian.mods.kubejs.item.ItemPredicate;
import moe.wolfgirl.probejs.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.next.typescript.base.AliasRegistrar;
import moe.wolfgirl.probejs.next.typescript.document.Types;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

public class RecipeTypes extends ProbeJSPlugin {

    @Override
    public void addTypeAlias(AliasRegistrar registrar) {
        registrar.addInputAlias(ItemLike.class, Item.class);
        registrar.addInputAlias(ItemPredicate.class, Types.literal("*"));
        registrar.addInputAlias(ItemPredicate.class, Types.literal("-"));
        // lambda not supported (yet


    }
}
