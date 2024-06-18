package moe.wolfgirl.probejs.docs.assignments;

import dev.latvian.mods.kubejs.fluid.FluidStackJS;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import dev.latvian.mods.kubejs.recipe.InputReplacement;
import dev.latvian.mods.kubejs.recipe.OutputReplacement;
import dev.latvian.mods.kubejs.recipe.filter.RecipeFilter;
import dev.latvian.mods.kubejs.recipe.ingredientaction.IngredientActionFilter;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.docs.Primitives;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;

import java.util.Set;

public class RecipeTypes extends ProbeJSPlugin {
    @Override
    public void assignType(ScriptDump scriptDump) {
        scriptDump.assignType(ItemStack.class, Types.type(Item.class));
        scriptDump.assignType(ItemStack.class, Types.object()
                .member("item", Types.primitive("Special.Item"))
                .member("count?", Primitives.INTEGER)
                .member("nbt?", Types.primitive("{}"))
                .build());

        scriptDump.assignType(Ingredient.class, Types.type(ItemStack.class));
        scriptDump.assignType(Ingredient.class, Types.type(Ingredient.class).asArray());

        scriptDump.assignType(Ingredient.class, Types.primitive("RegExp"));
        scriptDump.assignType(Ingredient.class, Types.literal("*"));
        scriptDump.assignType(Ingredient.class, Types.literal("-"));
        scriptDump.assignType(Ingredient.class, Types.primitive("`#${Special.ItemTag}`"));
        scriptDump.assignType(Ingredient.class, Types.primitive("`@${Special.Mod}`"));
        scriptDump.assignType(Ingredient.class, Types.primitive("`%${Special.CreativeModeTab}`"));

        scriptDump.assignType(InputItem.class, Types.type(Ingredient.class));
        scriptDump.assignType(InputItem.class, Types.primitive("`${number}x ${Special.Item}`"));

        scriptDump.assignType(OutputItem.class, Types.type(ItemStack.class));
        scriptDump.assignType(OutputItem.class, Types.object()
                .member("item", Types.primitive("Special.Item"))
                .member("chance", Primitives.DOUBLE)
                .build());
        scriptDump.assignType(OutputItem.class, Types.object()
                .member("item", Types.primitive("Special.Item"))
                .member("minRolls", Primitives.INTEGER)
                .member("maxRolls", Primitives.INTEGER)
                .build());

        scriptDump.assignType(InputReplacement.class, Types.type(InputItem.class));
        scriptDump.assignType(OutputReplacement.class, Types.type(OutputItem.class));

        scriptDump.assignType(RecipeFilter.class, Types.primitive("RegExp"));
        scriptDump.assignType(RecipeFilter.class, Types.literal("*"));
        scriptDump.assignType(RecipeFilter.class, Types.literal("-"));
        scriptDump.assignType(RecipeFilter.class, Types.type(RecipeFilter.class).asArray());

        scriptDump.assignType(RecipeFilter.class, Types.object()
                .member("or?", Types.type(RecipeFilter.class))
                .member("not?", Types.type(RecipeFilter.class))
                .member("id?", Types.primitive("Special.RecipeId"))
                .member("type?", Types.primitive("Special.RecipeType"))
                .member("group?", Types.STRING)
                .member("mod?", Types.primitive("Special.Mod"))
                .member("input?", Types.type(InputItem.class))
                .member("output?", Types.type(OutputItem.class))
                .build());
        scriptDump.assignType(FluidStackJS.class, Types.type(Fluid.class));
        scriptDump.assignType(FluidStackJS.class, Types.literal("-"));
        scriptDump.assignType(FluidStackJS.class, Types.object()
                .member("fluid", Types.primitive("Special.Fluid"))
                .member("amount?", Primitives.INTEGER)
                .member("nbt?", Types.OBJECT)
                .build());

        scriptDump.assignType(IngredientActionFilter.class, Primitives.INTEGER);
        scriptDump.assignType(IngredientActionFilter.class, Types.type(Ingredient.class));
        scriptDump.assignType(IngredientActionFilter.class, Types.object()
                .member("item?", Types.type(Ingredient.class))
                .member("index?", Primitives.INTEGER)
                .build());
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        return Set.of(RecipeFilter.class);
    }
}
