package moe.wolfgirl.probejs.features.rich.item;

import moe.wolfgirl.probejs.util.json.JArray;
import moe.wolfgirl.probejs.util.json.JObject;
import moe.wolfgirl.probejs.util.json.JPrimitive;
import dev.latvian.mods.kubejs.core.ItemKJS;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;

public class ItemTagAttribute {
    private final String id;
    private final List<Item> itemsOfTag;


    public ItemTagAttribute(TagKey<Item> itemTag) {
        this.id = itemTag.location().toString();
        this.itemsOfTag = new ArrayList<>();
        for (Holder<Item> holder : RegistryInfo.ITEM.getVanillaRegistry().getTagOrEmpty(itemTag)) {
            itemsOfTag.add(holder.value());
        }
    }

    public JObject serialize() {
        return JObject.create()
                .add("id", new JPrimitive(id))
                .add("items", JArray.create()
                        .addAll(itemsOfTag.stream()
                                .map(ItemKJS::kjs$getId)
                                .map(JPrimitive::new))
                );
    }
}
