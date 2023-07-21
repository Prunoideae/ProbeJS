package com.probejs.util.forge;

import com.google.common.collect.BiMap;
import com.probejs.ProbeJS;
import com.probejs.compiler.DocCompiler;
import com.probejs.compiler.formatter.formatter.IFormatter;
import com.probejs.jdoc.document.DocumentClass;
import com.probejs.util.PlatformSpecial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.eventbus.ListenerList;
import net.minecraftforge.eventbus.LockHelper;
import net.minecraftforge.eventbus.api.EventListenerHelper;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;

public class PlatformSpecialImpl extends PlatformSpecial {
    private static Field ingredientInst = null;

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public List<ResourceLocation> getIngredientTypes() {
        if (ingredientInst == null) {
            Field ingredients;
            try {
                ingredients = CraftingHelper.class.getDeclaredField("ingredients");
                ingredients.setAccessible(true);
            } catch (NoSuchFieldException e) {
                return List.of();
            }
            ingredientInst = ingredients;
        }

        try {
            BiMap<ResourceLocation, IIngredientSerializer<?>> ingredientValue = (BiMap<ResourceLocation, IIngredientSerializer<?>>) ingredientInst.get(null);
            return ingredientValue.keySet().stream().toList();
        } catch (IllegalAccessException e) {
            return List.of();
        }
    }

    @NotNull
    @Override
    @SuppressWarnings("unchecked")
    public List<IFormatter> getPlatformFormatters() {
        return List.of();
    }

    @NotNull
    @Override
    public List<DocumentClass> getPlatformDocuments(List<DocumentClass> globalClasses) {
        List<DocumentClass> superDocuments = super.getPlatformDocuments(globalClasses);
        try {
            superDocuments.add(ForgeEventDocument.loadForgeEventDocument());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return superDocuments;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void preCompile() {
        // Uses reflection to acquire current list of events
        // God help us if this breaks
        try {
            Field listenersField = EventListenerHelper.class.getDeclaredField("listeners");
            listenersField.setAccessible(true);
            LockHelper<Class<?>, ListenerList> listeners = (LockHelper<Class<?>, ListenerList>) listenersField.get(null);

            Field lockField = LockHelper.class.getDeclaredField("lock");
            Field mapField = LockHelper.class.getDeclaredField("map");
            lockField.setAccessible(true);
            mapField.setAccessible(true);

            ReadWriteLock lock = (ReadWriteLock) lockField.get(listeners);
            Map<Class<?>, ListenerList> map = (Map<Class<?>, ListenerList>) mapField.get(listeners);

            var readLock = lock.readLock();
            readLock.lock();

            Set<Class<?>> eventClasses = map.keySet();

            readLock.unlock();

            eventClasses.forEach(eventClass -> {
                DocCompiler.CapturedClasses.capturedRawEvents.put(eventClass.getName(), eventClass);
            });
        } catch (Throwable e) {
            ProbeJS.LOGGER.error("Failed to load events from Forge", e);
        }
    }

    @Override
    public TextureAtlasSprite getFluidSprite(Fluid fluid) {
        var properties = IClientFluidTypeExtensions.of(fluid);
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(properties.getStillTexture(new FluidStack(fluid, 1000)));
    }
}
