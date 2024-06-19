package moe.wolfgirl.probejs.docs.assignments;


import dev.latvian.mods.kubejs.recipe.filter.RecipeFilter;
import moe.wolfgirl.probejs.docs.Primitives;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

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
                .member("input?", Types.type(Ingredient.class))
                .member("output?", Types.type(ItemStack.class))
                .build());
        scriptDump.assignType(FluidStack.class, Types.type(Fluid.class));
        scriptDump.assignType(FluidStack.class, Types.literal("-"));
        scriptDump.assignType(FluidStack.class, Types.object()
                .member("fluid", Types.primitive("Special.Fluid"))
                .member("amount?", Primitives.INTEGER)
                .member("nbt?", Types.OBJECT)
                .build());
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        return Set.of(RecipeFilter.class);
    }
}
