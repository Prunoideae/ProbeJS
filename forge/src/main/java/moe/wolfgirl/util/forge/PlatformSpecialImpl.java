package moe.wolfgirl.util.forge;

import com.google.common.collect.BiMap;
import moe.wolfgirl.ProbeJS;
import moe.wolfgirl.docs.DocCompiler;
import moe.wolfgirl.docs.formatter.formatter.IFormatter;
import moe.wolfgirl.jdoc.document.DocumentClass;
import moe.wolfgirl.util.PlatformSpecial;
import moe.wolfgirl.util.special_docs.forge.ForgeEventDocument;
import dev.architectury.hooks.fluid.FluidStackHooks;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.eventbus.ListenerList;
import net.minecraftforge.eventbus.LockHelper;
import net.minecraftforge.eventbus.api.EventListenerHelper;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Collectors;

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
        return platformFormatters;
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

            eventClasses.forEach(eventClass -> DocCompiler.CapturedClasses.capturedRawEvents.put(eventClass.getName(), eventClass));
        } catch (Throwable e) {
            ProbeJS.LOGGER.error("Failed to load events from Forge", e);
        }
    }

    @Override
    public TextureAtlasSprite getFluidSprite(Fluid fluid) {
        return FluidStackHooks.getStillTexture(fluid);
    }

    @Override
    public List<File> getModFiles() {
        ModList modList = ModList.get();
        return modList.getModFiles().stream()
                .map(fileInfo -> fileInfo.getFile().getFilePath())
                .map(Path::toFile)
                .collect(Collectors.toList());
    }
}
