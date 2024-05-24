package moe.wolfgirl.specials.special;

import moe.wolfgirl.ProbeJS;
import moe.wolfgirl.docs.formatter.formatter.IFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.stream.Collectors;

public class FormatterModel implements IFormatter {
    @Override
    public List<String> format(Integer indent, Integer stepIndent) {

        return List.of("%stype BakedModel = %s".formatted(" ".repeat(indent),
                Minecraft.getInstance().getModelManager().bakedRegistry.keySet()
                        .stream()
                        .map(ResourceLocation::toString)
                        .map(s -> s.split("#")[0])
                        .collect(Collectors.toSet())
                        .stream()
                        .map(ProbeJS.GSON::toJson)
                        .collect(Collectors.joining(" | "))
        ));
    }
}
