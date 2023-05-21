package com.probejs.recipe.desc.impl.simple;

import com.probejs.recipe.desc.DescriptionTyped;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;

public class DescriptionFluidStack extends DescriptionTyped<FluidStackJS> {
    @Override
    public Class<FluidStackJS> getType() {
        return FluidStackJS.class;
    }
}
