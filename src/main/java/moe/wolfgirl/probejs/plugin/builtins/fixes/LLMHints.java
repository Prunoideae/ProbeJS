package moe.wolfgirl.probejs.plugin.builtins.fixes;

import dev.latvian.mods.kubejs.entity.AfterLivingEntityHurtKubeEvent;
import dev.latvian.mods.kubejs.entity.BeforeLivingEntityHurtKubeEvent;
import dev.latvian.mods.kubejs.entity.LivingEntityDeathKubeEvent;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import moe.wolfgirl.probejs.misc.llm.NotesToLLM;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.List;

public class LLMHints extends ProbeJSPlugin {
    @Override
    public void addUsageHintsForAgents(NotesToLLM.Registry registry) {
        registry.register(RecipesKubeEvent.class, List.of(
                "Focus on `getRecipes()` or the `get recipes()` function as they are the main way to add recipes.",
                "Recipe event contains shortcut for vanilla recipe types, such as `shaped`, `shapeless`.",
                "On the other hand, other recipes are registered using `recipes`, e.g. `event.recipes.create.mixing(...)`.",
                "Recipe IDs are automatically generated and do not need to be specified unless necessary.",
                "`DocumentedRecipes` in `@side-only/server/events/recipes` stores all the registered recipes, this is also the return type of `getRecipes()` or `recipes`."
        ));

        registry.register(ResourceKey.class, List.of(
                "`ResourceKey` is a reference to a registry entry. KubeJS has special type wrapper for `ResourceKey<T>`",
                "that allows you to use string literals as `ResourceKey`. For example, \"minecraf:apple\" can be used as a `ResourceKey<Item>`.",
                "Check available literals for registry entries using the #tool:prunoideae.probejs/listRegistries #tool:prunoideae.probejs/queryRegistryObjectsByRegex #tool:prunoideae.probejs/queryTaggedObjects tools."
        ));

        registry.register(TagKey.class, List.of(
                "`TagKey` is a reference to a tag. KubeJS has special type wrapper for `TagKey<T>`",
                "that allows you to use string literals as `TagKey`. For example, \"forge:ores\" can be used as a `TagKey<Item>`.",
                "Check available literals for tags using the #tool:prunoideae.probejs/listRegistries and #tool:prunoideae.probejs/queryTagsByRegex tools."
        ));

        registry.register(Holder.class, List.of(
                "`Holder` is a reference that may be either direct references or tags. KubeJS has special type wrapper for `Holder<T>`",
                "that allows you to use string literals as `Holder`. For example, \"minecraft:apple\" can be used as a `Holder<Item>`, and so can \"forge:ores\".",
                "Check available literals for registry entries and tags using the #tool:prunoideae.probejs/listRegistries, #tool:prunoideae.probejs/queryRegistryObjectsByRegex and #tool:prunoideae.probejs/queryTagsByRegex tools."
        ));

        registry.register(HolderSet.class, List.of(
                "`HolderSet` is a set of `Holder`s that may be either direct references or tags. KubeJS has special type wrapper for `HolderSet<T>`",
                "that allows you to use string literals as `HolderSet`. For example, [\"minecraft:apple\"] can be used as a `HolderSet<Item>`, and so can [\"forge:ores\"]",
                "Check available literals for registry entries and tags using the #tool:prunoideae.probejs/listRegistries, #tool:prunoideae.probejs/queryRegistryObjectsByRegex and #tool:prunoideae.probejs/queryTagsByRegex tools."
        ));

        registry.register(LivingEntityDeathKubeEvent.class, List.of("The `player` and `getPlayer` casts the entity to `Player` and return null if the cast fails. Check for `DamageSource` to find the source of damage."));
        registry.register(BeforeLivingEntityHurtKubeEvent.class, List.of("The `player` and `getPlayer` casts the entity to `Player` and return null if the cast fails. Check for `DamageSource` to find the source of damage."));
        registry.register(AfterLivingEntityHurtKubeEvent.class, List.of("The `player` and `getPlayer` casts the entity to `Player` and return null if the cast fails. Check for `DamageSource` to find the source of damage."));

        registry.register(MobEffectInstance.class, List.of("Check MobEffectUtils binding for easier way to create MobEffectInstance."));
    }
}
