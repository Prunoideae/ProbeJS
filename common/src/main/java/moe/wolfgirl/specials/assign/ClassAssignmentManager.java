package moe.wolfgirl.specials.assign;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import dev.latvian.mods.kubejs.block.state.BlockStatePredicate;
import dev.latvian.mods.kubejs.core.PlayerSelector;
import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.level.gen.filter.biome.BiomeFilter;
import dev.latvian.mods.kubejs.level.gen.filter.mob.MobFilter;
import dev.latvian.mods.kubejs.player.PlayerStatsJS;
import dev.latvian.mods.kubejs.recipe.InputReplacement;
import dev.latvian.mods.kubejs.recipe.OutputReplacement;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import dev.latvian.mods.kubejs.recipe.filter.RecipeFilter;
import dev.latvian.mods.kubejs.recipe.ingredientaction.IngredientActionFilter;
import dev.latvian.mods.kubejs.typings.desc.DescriptionContext;
import dev.latvian.mods.kubejs.typings.desc.GenericDescJS;
import dev.latvian.mods.kubejs.typings.desc.PrimitiveDescJS;
import dev.latvian.mods.kubejs.typings.desc.TypeDescJS;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

/**
 * Must avoid circular type dependency as TypeScript will be not happy.
 */
public class ClassAssignmentManager {
    public static final Multimap<Class<?>, TypeDescJS> ASSIGNMENTS = ArrayListMultimap.create();

    public static final PrimitiveDescJS REGEXP = new PrimitiveDescJS("RegExp");

    public static TypeDescJS tagOf(Class<?> clazz, DescriptionContext context) {
        return new GenericDescJS(context.javaType(TagKey.class), context.javaType(clazz));
    }

    public static void init(DescriptionContext context) {
        // BlockPos
        ASSIGNMENTS.put(BlockPos.class, TypeDescJS.fixedArray(
                TypeDescJS.NUMBER, TypeDescJS.NUMBER, TypeDescJS.NUMBER
        ));
        ASSIGNMENTS.put(BlockPos.class, context.javaType(Vec3.class));

        // Vec3
        ASSIGNMENTS.put(Vec3.class, TypeDescJS.fixedArray(
                TypeDescJS.NUMBER, TypeDescJS.NUMBER, TypeDescJS.NUMBER
        ));

        // ItemLike
        ASSIGNMENTS.put(ItemLike.class, context.javaType(Item.class));

        // MobCategory
        ASSIGNMENTS.put(MobCategory.class, TypeDescJS.STRING);

        // AABB
        ASSIGNMENTS.put(AABB.class, TypeDescJS.fixedArray(
                TypeDescJS.NUMBER, TypeDescJS.NUMBER, TypeDescJS.NUMBER
        ));
        ASSIGNMENTS.put(AABB.class, TypeDescJS.fixedArray(
                TypeDescJS.NUMBER, TypeDescJS.NUMBER, TypeDescJS.NUMBER,
                TypeDescJS.NUMBER, TypeDescJS.NUMBER, TypeDescJS.NUMBER
        ));

        // IntProvider
        ASSIGNMENTS.put(IntProvider.class, TypeDescJS.NUMBER);
        ASSIGNMENTS.put(IntProvider.class, TypeDescJS.fixedArray(
                TypeDescJS.NUMBER, TypeDescJS.NUMBER
        ));
        ASSIGNMENTS.put(IntProvider.class, TypeDescJS.object()
                .add("clamped", context.javaType(IntProvider.class))
                .add("bounds", TypeDescJS.fixedArray(
                        TypeDescJS.NUMBER, TypeDescJS.NUMBER
                ))
        );
        ASSIGNMENTS.put(IntProvider.class, TypeDescJS.object()
                .add("clamped", context.javaType(IntProvider.class))
                .add("min", TypeDescJS.NUMBER)
                .add("max", TypeDescJS.NUMBER)
        );
        ASSIGNMENTS.put(IntProvider.class, TypeDescJS.object()
                .add("clamped", context.javaType(IntProvider.class))
                .add("min_inclusive", TypeDescJS.NUMBER)
                .add("max_inclusive", TypeDescJS.NUMBER)
        );
        ASSIGNMENTS.put(IntProvider.class, TypeDescJS.object()
                .add("clamped", context.javaType(IntProvider.class))
                .add("value", TypeDescJS.NUMBER)
        );
        ASSIGNMENTS.put(IntProvider.class, TypeDescJS.object()
                .add("clamped_normal", context.javaType(IntProvider.class))
                .add("bounds", TypeDescJS.fixedArray(
                        TypeDescJS.NUMBER, TypeDescJS.NUMBER
                ))
                .add("mean", TypeDescJS.NUMBER)
                .add("deviation", TypeDescJS.NUMBER)
        );
        ASSIGNMENTS.put(IntProvider.class, TypeDescJS.object()
                .add("clamped_normal", context.javaType(IntProvider.class))
                .add("min", TypeDescJS.NUMBER)
                .add("max", TypeDescJS.NUMBER)
                .add("mean", TypeDescJS.NUMBER)
                .add("deviation", TypeDescJS.NUMBER)
        );
        ASSIGNMENTS.put(IntProvider.class, TypeDescJS.object()
                .add("clamped_normal", context.javaType(IntProvider.class))
                .add("min_inclusive", TypeDescJS.NUMBER)
                .add("max_inclusive", TypeDescJS.NUMBER)
                .add("mean", TypeDescJS.NUMBER)
                .add("deviation", TypeDescJS.NUMBER)
        );
        ASSIGNMENTS.put(IntProvider.class, TypeDescJS.object()
                .add("clamped_normal", context.javaType(IntProvider.class))
                .add("value", TypeDescJS.NUMBER)
                .add("mean", TypeDescJS.NUMBER)
                .add("deviation", TypeDescJS.NUMBER)
        );

        // NumberProvider
        ASSIGNMENTS.put(NumberProvider.class, TypeDescJS.NUMBER);
        ASSIGNMENTS.put(NumberProvider.class, TypeDescJS.fixedArray(
                TypeDescJS.NUMBER, TypeDescJS.NUMBER
        ));
        ASSIGNMENTS.put(NumberProvider.class, TypeDescJS.object()
                .add("min", TypeDescJS.NUMBER)
                .add("max", TypeDescJS.NUMBER)
        );
        ASSIGNMENTS.put(NumberProvider.class, TypeDescJS.object()
                .add("n", TypeDescJS.NUMBER)
                .add("p", TypeDescJS.NUMBER)
        );
        ASSIGNMENTS.put(NumberProvider.class, TypeDescJS.object()
                .add("value", TypeDescJS.NUMBER)
        );

        // ItemStack
        ASSIGNMENTS.put(ItemStack.class, context.javaType(Item.class));
        ASSIGNMENTS.put(ItemStack.class, TypeDescJS.object()
                .add("item", context.javaType(Item.class))
                .add("count", TypeDescJS.NUMBER, true)
                .add("nbt", context.javaType(CompoundTag.class), true)
        );
        ASSIGNMENTS.put(ItemStack.class, REGEXP);

        // Ingredient
        ASSIGNMENTS.put(Ingredient.class, context.javaType(ItemStack.class));
        ASSIGNMENTS.put(Ingredient.class, REGEXP);
        ASSIGNMENTS.put(Ingredient.class, context.javaType(Ingredient.class).asArray());
        ASSIGNMENTS.put(Ingredient.class, new PrimitiveDescJS("\"*\""));
        ASSIGNMENTS.put(Ingredient.class, tagOf(Item.class, context));
        ASSIGNMENTS.put(Ingredient.class, new PrimitiveDescJS("`@${Special.Mod}`"));
        ASSIGNMENTS.put(Ingredient.class, new PrimitiveDescJS("`%${Special.CreativeModeTab}`"));

        // InputReplacement
        ASSIGNMENTS.put(InputReplacement.class, context.javaType(InputItem.class));
        // OutputReplacement
        ASSIGNMENTS.put(OutputReplacement.class, context.javaType(OutputItem.class));

        // InputItem
        ASSIGNMENTS.put(InputItem.class, context.javaType(Ingredient.class));
        // OutputItem
        ASSIGNMENTS.put(OutputItem.class, context.javaType(ItemStack.class));
        ASSIGNMENTS.put(ItemStack.class, TypeDescJS.object()
                .add("item", context.javaType(Item.class))
                .add("count", TypeDescJS.NUMBER, true)
                .add("nbt", context.javaType(CompoundTag.class), true)
                .add("chance", TypeDescJS.NUMBER, true)
                .add("minRolls", TypeDescJS.NUMBER, true)
                .add("maxRolls", TypeDescJS.NUMBER, true)
        );

        // BlockStatePredicate
        ASSIGNMENTS.put(BlockStatePredicate.class, context.javaType(BlockStatePredicate.class).asArray());
        ASSIGNMENTS.put(BlockStatePredicate.class, TypeDescJS.object()
                .add("or", context.javaType(BlockStatePredicate.class), true)
                .add("not", context.javaType(BlockStatePredicate.class), true)
        );
        ASSIGNMENTS.put(BlockStatePredicate.class, context.javaType(Block.class));
        ASSIGNMENTS.put(BlockStatePredicate.class, context.javaType(BlockState.class));
        ASSIGNMENTS.put(BlockStatePredicate.class, tagOf(Block.class, context));
        ASSIGNMENTS.put(BlockStatePredicate.class, REGEXP);
        ASSIGNMENTS.put(BlockStatePredicate.class, new PrimitiveDescJS("\"*\""));
        ASSIGNMENTS.put(BlockStatePredicate.class, new PrimitiveDescJS("\"-\""));

        // RuleTest
        ASSIGNMENTS.put(RuleTest.class, context.javaType(BlockStatePredicate.class));
        ASSIGNMENTS.put(RuleTest.class, context.javaType(CompoundTag.class));

        // BiomeFilter
        ASSIGNMENTS.put(BiomeFilter.class, new PrimitiveDescJS("Special.Biome"));
        ASSIGNMENTS.put(BiomeFilter.class, new PrimitiveDescJS("`#${Special.BiomeTag}`"));
        ASSIGNMENTS.put(BiomeFilter.class, new PrimitiveDescJS("\"*\""));
        ASSIGNMENTS.put(BiomeFilter.class, new PrimitiveDescJS("\"-\""));
        ASSIGNMENTS.put(BiomeFilter.class, REGEXP);
        ASSIGNMENTS.put(BiomeFilter.class, context.javaType(BiomeFilter.class).asArray());
        ASSIGNMENTS.put(BiomeFilter.class, TypeDescJS.object()
                .add("or", context.javaType(BiomeFilter.class), true)
                .add("not", context.javaType(BiomeFilter.class), true)
                .add("id", new PrimitiveDescJS("Special.Biome"), true)
                .add("type", new PrimitiveDescJS("Special.Biome"), true)
                .add("tag", new PrimitiveDescJS("`#${Special.BiomeTag}`"), true)
        );

        // MobFilter
        ASSIGNMENTS.put(MobFilter.class, new PrimitiveDescJS("\"*\""));
        ASSIGNMENTS.put(MobFilter.class, new PrimitiveDescJS("\"-\""));
        ASSIGNMENTS.put(MobFilter.class, context.javaType(EntityType.class));
        ASSIGNMENTS.put(MobFilter.class, tagOf(EntityType.class, context));
        ASSIGNMENTS.put(MobFilter.class, REGEXP);
        ASSIGNMENTS.put(MobFilter.class, context.javaType(MobFilter.class).asArray());
        ASSIGNMENTS.put(MobFilter.class, TypeDescJS.object()
                .add("or", context.javaType(MobFilter.class), true)
                .add("not", context.javaType(MobFilter.class), true)
                .add("id", context.javaType(EntityType.class), true)
                .add("type", context.javaType(EntityType.class), true)
                .add("tag", TypeDescJS.STRING, true)
        );

        // FluidStackJS
        ASSIGNMENTS.put(FluidStackJS.class, new PrimitiveDescJS("\"-\""));
        ASSIGNMENTS.put(FluidStackJS.class, new PrimitiveDescJS("\"empty\""));
        ASSIGNMENTS.put(FluidStackJS.class, new PrimitiveDescJS("\"minecraft:empty\""));
        ASSIGNMENTS.put(FluidStackJS.class, context.javaType(Fluid.class));
        ASSIGNMENTS.put(FluidStackJS.class, TypeDescJS.object()
                .add("fluid", context.javaType(Fluid.class))
                .add("amount", TypeDescJS.NUMBER, true)
                .add("nbt", context.javaType(CompoundTag.class))
        );

        // RecipeFilter
        ASSIGNMENTS.put(RecipeFilter.class, new PrimitiveDescJS("\"*\""));
        ASSIGNMENTS.put(RecipeFilter.class, new PrimitiveDescJS("\"-\""));
        ASSIGNMENTS.put(RecipeFilter.class, REGEXP);
        ASSIGNMENTS.put(RecipeFilter.class, context.javaType(RecipeFilter.class).asArray());
        ASSIGNMENTS.put(RecipeFilter.class, TypeDescJS.object()
                .add("or", context.javaType(RecipeFilter.class), true)
                .add("not", context.javaType(RecipeFilter.class), true)
                .add("id", new PrimitiveDescJS("Special.RecipeId"), true)
                .add("type", new PrimitiveDescJS("Special.RecipeSerializer"), true)
                .add("group", TypeDescJS.STRING, true)
                .add("mod", new PrimitiveDescJS("Special.Mod"), true)
                .add("input", context.javaType(ReplacementMatch.class), true)
                .add("output", context.javaType(ReplacementMatch.class), true)
        );

        // IngredientActionFilter
        ASSIGNMENTS.put(IngredientActionFilter.class, TypeDescJS.NUMBER);
        ASSIGNMENTS.put(IngredientActionFilter.class, context.javaType(Ingredient.class));
        ASSIGNMENTS.put(IngredientActionFilter.class, TypeDescJS.object()
                .add("item", context.javaType(Ingredient.class))
                .add("index", TypeDescJS.NUMBER)
        );

        // Tier
        ASSIGNMENTS.put(Tier.class, TypeDescJS.STRING);

        // ArmorMaterial
        ASSIGNMENTS.put(ArmorMaterial.class, TypeDescJS.STRING);

        // PlayerSelector
        ASSIGNMENTS.put(PlayerSelector.class, context.javaType(UUID.class));
        ASSIGNMENTS.put(PlayerSelector.class, TypeDescJS.STRING);

        // EntitySelector
        ASSIGNMENTS.put(EntitySelector.class, TypeDescJS.STRING);

        // ReplacementMatch
        ASSIGNMENTS.put(ReplacementMatch.class, context.javaType(Ingredient.class));

        // PlayerStatsJS
        ASSIGNMENTS.put(PlayerStatsJS.class, TypeDescJS.STRING);
    }
}
