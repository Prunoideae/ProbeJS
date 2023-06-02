package com.probejs.recipe.desc.impl.simple;

import com.probejs.recipe.desc.SimpleDescription;
import net.minecraft.world.item.crafting.Ingredient;

public class DescriptionItemInput extends SimpleDescription<Ingredient> {
    @Override
    public Class<Ingredient> getType() {
        return Ingredient.class;
    }
}
