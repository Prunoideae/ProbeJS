package com.probejs.rich.item;

import com.mojang.blaze3d.platform.NativeImage;
import com.probejs.ProbeJS;
import com.probejs.ProbePaths;
import com.probejs.rich.image.ImageHelper;
import com.probejs.util.json.JArray;
import dev.latvian.mods.kubejs.bindings.ItemWrapper;
import net.minecraft.world.item.ItemStack;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class RichItemCompiler {
    public static void compile() throws IOException {
        JArray array = new JArray()
                .addAll(ItemWrapper.getList()
                        .stream().map(ItemAttribute::new)
                        .map(ItemAttribute::serialize));
        Path richFile = ProbePaths.WORKSPACE_SETTINGS.resolve("item-attributes.json");
        BufferedWriter writer = Files.newBufferedWriter(richFile);
        writer.write(ProbeJS.GSON.toJson(array.serialize()));
        writer.close();
    }

    public static void render() throws IOException {
        for (ItemStack itemStack : ItemWrapper.getList()) {
            NativeImage image = ImageHelper.getFromItem(itemStack, ImageHelper.init());
            Path path = ProbePaths.RICH.resolve(itemStack.kjs$getIdLocation().getNamespace());
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            String name = itemStack.kjs$getIdLocation().getPath().replace("/", "_");
            image.writeToFile(path.resolve(name + ".png"));
            image.close();
        }
    }
}
