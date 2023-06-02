package com.probejs.recipe.desc.impl.simple;

import com.probejs.recipe.desc.SimpleDescription;
import net.minecraft.world.item.ItemStack;

public class DescriptionItemOutput extends SimpleDescription<ItemStack> {
    @Override
    public Class<ItemStack> getType() {
        return ItemStack.class;
    }
}
