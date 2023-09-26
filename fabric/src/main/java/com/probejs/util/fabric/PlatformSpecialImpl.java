package com.probejs.util.fabric;

import com.probejs.compiler.formatter.formatter.IFormatter;
import com.probejs.jdoc.document.DocumentClass;
import com.probejs.util.PlatformSpecial;
import dev.architectury.hooks.fluid.FluidStackHooks;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class PlatformSpecialImpl extends PlatformSpecial {

    @NotNull
    @Override
    public List<ResourceLocation> getIngredientTypes() {
        //Custom Ingredients are not supported by fabric?
        return List.of();
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public List<IFormatter> getPlatformFormatters() {
        return platformFormatters;
    }

    @NotNull
    @Override
    public List<DocumentClass> getPlatformDocuments(List<DocumentClass> globalClasses) {
        return super.getPlatformDocuments(globalClasses);
    }

    @Override
    public TextureAtlasSprite getFluidSprite(Fluid fluid) {
        return FluidStackHooks.getStillTexture(fluid);
    }
}
