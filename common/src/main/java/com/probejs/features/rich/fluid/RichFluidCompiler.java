package com.probejs.features.rich.fluid;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import com.probejs.ProbeJS;
import com.probejs.ProbePaths;
import com.probejs.features.rich.ImageHelper;
import com.probejs.util.json.JArray;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RichFluidCompiler {
    public static void compile() throws IOException {
        JArray fluidArray = JArray.create()
                .addAll(RegistryInfo.FLUID.entrySet().stream()
                        .map(Map.Entry::getValue)
                        .map(FluidAttribute::new)
                        .map(FluidAttribute::serialize));
        Path richFile = ProbePaths.WORKSPACE_SETTINGS.resolve("fluid-attributes.json");
        BufferedWriter writer = Files.newBufferedWriter(richFile);
        writer.write(ProbeJS.GSON.toJson(fluidArray.serialize()));
        writer.close();
    }

    public static void render(List<Pair<Fluid, Path>> fluids) throws IOException {
        RenderTarget frameBuffer = ImageHelper.init();
        for (Pair<Fluid, Path> pair : fluids) {
            NativeImage image = ImageHelper.getFromFluid(pair.getFirst(), frameBuffer);
            image.writeToFile(pair.getSecond());
            image.close();
            frameBuffer.clear(false);
        }
        frameBuffer.destroyBuffers();

    }

    public static List<Pair<Fluid, Path>> resolve() {
        ArrayList<Pair<Fluid, Path>> fluids = new ArrayList<>();
        var registry = RegistryInfo.FLUID.getVanillaRegistry();
        for (Fluid fluid : registry) {
            ResourceLocation id = registry.getKey(fluid);
            if (id == null) continue;
            Path path = ProbePaths.RICH_FLUID.resolve(id.getNamespace());
            if (!Files.exists(path)) {
                try {
                    Files.createDirectories(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            String name = id.getPath().replace("/", "_");
            if (path.resolve(name + ".png").toFile().exists()) continue;
            fluids.add(Pair.of(fluid, path.resolve(name + ".png")));
        }
        return fluids;
    }
}
