package com.probejs.recipe.desc.impl.simple;

import com.google.gson.JsonObject;
import com.probejs.recipe.desc.DescriptionTyped;
import net.minecraft.world.item.ItemStack;

public class DescriptionItemOutput extends DescriptionTyped<ItemStack> {
    @Override
    public void deserialize(JsonObject json) {

    }

    @Override
    public Class<ItemStack> getType() {
        return ItemStack.class;
    }
}
