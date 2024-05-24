package moe.wolfgirl.specials.special;

import moe.wolfgirl.ProbeJS;
import moe.wolfgirl.docs.formatter.formatter.IFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import java.util.List;
import java.util.stream.Collectors;

public class FormatterTexture implements IFormatter {
    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        //Must ensure that this is executed only on client side
        return List.of(
                "%stype RawTexture = %s;".formatted(" ".repeat(indent),
                        Minecraft.getInstance().getTextureManager().byPath
                                .keySet()
                                .stream()
                                .map(ResourceLocation::toString)
                                .map(ProbeJS.GSON::toJson)
                                .collect(Collectors.joining(" | "))
                ),
                "%stype Texture = %s;".formatted(" ".repeat(indent),
                        Minecraft.getInstance().getModelManager()
                                .getAtlas(InventoryMenu.BLOCK_ATLAS)
                                .texturesByName
                                .keySet()
                                .stream()
                                .map(ResourceLocation::toString)
                                .map(ProbeJS.GSON::toJson)
                                .collect(Collectors.joining(" | "))
                )
        );
    }
}
