package com.probejs.rich.item;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import com.probejs.ProbeJS;
import com.probejs.ProbePaths;
import com.probejs.rich.image.ImageHelper;
import com.probejs.util.json.JArray;
import dev.latvian.mods.kubejs.bindings.ItemWrapper;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class RichItemCompiler {
    public static void compile() throws IOException {
        JArray itemArray = JArray.create()
                .addAll(ItemWrapper.getList()
                        .stream().map(ItemAttribute::new)
                        .map(ItemAttribute::serialize));
        Path richFile = ProbePaths.WORKSPACE_SETTINGS.resolve("item-attributes.json");
        BufferedWriter writer = Files.newBufferedWriter(richFile);
        writer.write(ProbeJS.GSON.toJson(itemArray.serialize()));
        writer.close();

        JArray tagArray = JArray.create()
                .addAll(Registry.ITEM.getTags()
                        .map(Pair::getFirst)
                        .map(ItemTagAttribute::new)
                        .map(ItemTagAttribute::serialize)
                );

        Path richTagFile = ProbePaths.WORKSPACE_SETTINGS.resolve("item-tag-attributes.json");
        BufferedWriter tagWriter = Files.newBufferedWriter(richTagFile);
        tagWriter.write(ProbeJS.GSON.toJson(tagArray.serialize()));
        tagWriter.close();

    }

    public static void render(List<Pair<ItemStack, Path>> items) throws IOException {
        RenderTarget frameBuffer = ImageHelper.init();
        for (Pair<ItemStack, Path> pair : items) {
            NativeImage image = ImageHelper.getFromItem(pair.getFirst(), frameBuffer);
            image.writeToFile(pair.getSecond());
            image.close();
            frameBuffer.clear(false);
        }
    }
}
