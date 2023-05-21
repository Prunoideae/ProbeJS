package com.probejs.recipe.desc.impl.simple;

import com.probejs.recipe.desc.DescriptionTyped;
import net.minecraft.world.item.crafting.Ingredient;

public class DescriptionItemInput extends DescriptionTyped<Ingredient> {
    @Override
    public Class<Ingredient> getType() {
        return Ingredient.class;
    }
}
