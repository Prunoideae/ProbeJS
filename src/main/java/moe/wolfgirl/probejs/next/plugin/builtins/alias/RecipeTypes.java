package moe.wolfgirl.probejs.next.plugin.builtins.alias;

import dev.latvian.mods.kubejs.item.ItemPredicate;
import dev.latvian.mods.kubejs.recipe.filter.RecipeFilter;
import moe.wolfgirl.probejs.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.next.typescript.base.AliasRegistrar;
import moe.wolfgirl.probejs.next.typescript.document.Types;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

public class RecipeTypes extends ProbeJSPlugin {

    @Override
    public void addTypeAlias(AliasRegistrar registrar) {
        registrar.addInputAlias(ItemLike.class, Item.class);

        registrar.addInputAlias(ItemPredicate.class, Ingredient.class);
        registrar.addInputAlias(ItemPredicate.class, Types.literal("*"));
        registrar.addInputAlias(ItemPredicate.class, Types.literal("-"));
        registrar.addInputAlias(ItemPredicate.class, Types.lambda(builder -> {
            builder.param("item", Types.clazz(Item.class));
            builder.returns(Types.BOOLEAN);
        }));

        registrar.addInputAlias(ItemStack.class, Item.class);
        registrar.addInputAlias(ItemStack.class, Types.object(builder -> {
            builder.param("item", RegistryTypes.object("Item"));
            builder.param("count", true, Types.NUMBER);
        }));

        registrar.addInputAlias(Ingredient.class, ItemStack.class);
        registrar.addInputAlias(Ingredient.class, Types.clazz(Ingredient.class).asArray());
        registrar.addInputAlias(Ingredient.class, Types.REGEXP);
        registrar.addInputAlias(Ingredient.class, Types.literal("*"));
        registrar.addInputAlias(Ingredient.class, Types.literal("-"));
        registrar.addInputAlias(Ingredient.class, Types.wrapped("`#${%s}`", RegistryTypes.tag("Item")));
        // scriptDump.assignType(Ingredient.class, Types.primitive("`@${Special.Mod}`"));
        // scriptDump.assignType(Ingredient.class, Types.primitive("`%${Special.CreativeModeTab}`"));

        registrar.addInputAlias(SizedIngredient.class, ItemStack.class);
        registrar.addInputAlias(SizedIngredient.class, Ingredient.class);

        registrar.addInputAlias(RecipeFilter.class, Types.REGEXP);
        registrar.addInputAlias(RecipeFilter.class, Types.literal("*"));
        registrar.addInputAlias(RecipeFilter.class, Types.literal("-"));
        registrar.addInputAlias(RecipeFilter.class, Types.clazz(RecipeFilter.class).asArray());

        registrar.addInputAlias(RecipeFilter.class, Types.object(builder -> {
            builder.param("or", true, Types.clazz(RecipeFilter.class).asInput().asArray());
            builder.param("not", true, Types.clazz(RecipeFilter.class).asInput());
            builder.param("id", true, Types.STRING); // TODO: RecipeId
            builder.param("type", true, RegistryTypes.object("RecipeSerializer"));
            builder.param("group", true, Types.STRING);
            builder.param("mod", true, Types.STRING); // TODO: ModId
            builder.param("input", true, Types.clazz(Ingredient.class).asInput());
            builder.param("output", true, Types.clazz(ItemStack.class).asInput());
        }));

        registrar.addInputAlias(FluidStack.class, Fluid.class);
        registrar.addInputAlias(FluidStack.class, Types.literal("-"));
        registrar.addInputAlias(FluidStack.class, Types.object(builder -> {
            builder.param("fluid", RegistryTypes.object("Fluid"));
            builder.param("amount", true, Types.NUMBER);
        }));

        registrar.addInputAlias(FluidIngredient.class, Fluid.class);
        registrar.addInputAlias(FluidIngredient.class, Types.REGEXP);
        registrar.addInputAlias(FluidIngredient.class, Types.wrapped("`#${%s}`", RegistryTypes.tag("Fluid")));
        // scriptDump.assignType(FluidIngredient.class, Types.primitive("`@${Special.Mod}`"));
    }
}
