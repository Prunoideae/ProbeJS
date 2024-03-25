package com.probejs.features.rich.fluid;

import com.probejs.util.json.JObject;
import com.probejs.util.json.JPrimitive;
import dev.architectury.fluid.FluidStack;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;

import java.util.Objects;

public class FluidAttribute {
    private final Fluid fluid;
    private final FluidStack fluidStack;


    public FluidAttribute(Fluid fluid) {
        this.fluid = fluid;
        this.fluidStack = FluidStack.create(fluid, FluidStack.bucketAmount());
    }

    public JObject serialize() {
        String fluidName = "Unknown Fluid";
        try {
            fluidName = fluidStack.getName().getString();
        } catch (Exception e) {
            try {
                fluidName = FluidStackJS.of(fluidStack).getId();
            } catch (Exception ignored) {
            }
        }
        return JObject.create()
                .add("id", JPrimitive.create(Objects.requireNonNull(RegistryInfo.FLUID.getVanillaRegistry().getKey(fluid)).toString()))
                .add("localized", JPrimitive.create(fluidName))
                .add("hasBucket", JPrimitive.create(fluid.getBucket() != Items.AIR))
                .add("hasBlock", JPrimitive.create(fluid.defaultFluidState().createLegacyBlock().getBlock() != Blocks.AIR))
                .add("bucketItem", JPrimitive.create(Objects.requireNonNull(RegistryInfo.ITEM.getVanillaRegistry().getKey(fluid.getBucket())).toString()));
    }
}
