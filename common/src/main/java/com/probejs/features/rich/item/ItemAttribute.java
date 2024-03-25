package com.probejs.features.rich.item;

import com.probejs.util.json.JObject;
import com.probejs.util.json.JPrimitive;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.CropBlock;
import org.jetbrains.annotations.Nullable;

public class ItemAttribute {
    private final ItemStack item;

    public ItemAttribute(ItemStack item) {
        this.item = item;
    }

    private String determineToolType() {
        Item itemRepr = item.getItem();
        if (itemRepr instanceof SwordItem) {
            return "sword";
        } else if (itemRepr instanceof PickaxeItem) {
            return "pickaxe";
        } else if (itemRepr instanceof ShovelItem) {
            return "shovel";
        } else if (itemRepr instanceof AxeItem) {
            return "axe";
        } else if (itemRepr instanceof HoeItem) {
            return "hoe";
        } else if (itemRepr instanceof ShearsItem) {
            return "shears";
        } else if (itemRepr instanceof TridentItem) {
            return "trident";
        } else if (itemRepr instanceof BowItem) {
            return "bow";
        } else if (itemRepr instanceof CrossbowItem) {
            return "crossbow";
        } else if (itemRepr instanceof ShieldItem) {
            return "shield";
        } else if (itemRepr instanceof ArmorItem) {
            return "armor";
        }
        return null;
    }

    @Nullable
    public JObject serialize() {
        JObject object;

        try {
            object = JObject.create()
                    .add("id", new JPrimitive(item.kjs$getId()))
                    .add("localized", new JPrimitive(item.getHoverName().getString()))
                    .add("maxDamage", new JPrimitive(item.getMaxDamage()))
                    .add("maxStackSize", new JPrimitive(item.getMaxStackSize()));
        } catch (Throwable ignored) {
            return null;
        }
        var toolType = determineToolType();
        if (toolType != null) {
            object.add("toolType", new JPrimitive(toolType));
        }
        var itemRepr = item.getItem();
        var food = itemRepr.getFoodProperties();
        if (food != null) {
            object.add("food", JObject.create()
                    .add("nutrition", new JPrimitive(food.getNutrition()))
                    .add("saturation", new JPrimitive(food.getSaturationModifier()))
                    .add("alwaysEdible", new JPrimitive(food.canAlwaysEat())));
        }
        if (itemRepr instanceof BlockItem blockItem) {
            object.add("block", JObject.create()
                    .add("crop", new JPrimitive(blockItem.getBlock() instanceof CropBlock)));
        }
        return object;
    }
}
