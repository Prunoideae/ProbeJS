package com.probejs.recipe.desc.impl.simple;

import com.probejs.recipe.desc.SimpleDescription;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;

public class DescriptionFluidStack extends SimpleDescription<FluidStackJS> {
    @Override
    public Class<FluidStackJS> getType() {
        return FluidStackJS.class;
    }
}
