package moe.wolfgirl.util.fabric;

import moe.wolfgirl.docs.formatter.formatter.IFormatter;
import moe.wolfgirl.jdoc.document.DocumentClass;
import moe.wolfgirl.util.PlatformSpecial;
import dev.architectury.hooks.fluid.FluidStackHooks;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
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
