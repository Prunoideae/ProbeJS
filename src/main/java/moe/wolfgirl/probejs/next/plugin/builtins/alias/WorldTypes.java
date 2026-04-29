package moe.wolfgirl.probejs.next.plugin.builtins.alias;

import dev.latvian.mods.kubejs.block.BlockTintFunction;
import dev.latvian.mods.kubejs.block.MapColorHelper;
import dev.latvian.mods.kubejs.block.state.BlockStatePredicate;
import dev.latvian.mods.kubejs.color.KubeColor;
import dev.latvian.mods.kubejs.item.ItemTintFunction;
import dev.latvian.mods.kubejs.plugin.builtin.wrapper.ColorWrapper;
import dev.latvian.mods.kubejs.recipe.match.ReplacementMatch;
import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.next.typescript.base.AliasRegistrar;
import moe.wolfgirl.probejs.next.typescript.document.Types;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import moe.wolfgirl.probejs.next.typescript.document.base.Type;
import moe.wolfgirl.probejs.next.typescript.document.types.special.NamespacedType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.stats.Stat;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class WorldTypes extends ProbeJSPlugin {

    @Override
    public void addTypeAlias(AliasRegistrar registrar) {
        registrar.addInputAlias(BlockState.class, Block.class);
        registrar.addInputAlias(BlockStatePredicate.class, Types.clazz(BlockStatePredicate.class).asInput().asArray());
        registrar.addInputAlias(BlockStatePredicate.class, Types.object(builder -> {
            builder.param("or", true, Types.clazz(BlockStatePredicate.class).asInput());
            builder.param("not", true, Types.clazz(BlockStatePredicate.class).asInput());
        }));
        registrar.addInputAlias(BlockStatePredicate.class, Block.class);
        registrar.addInputAlias(BlockStatePredicate.class, RegistryTypes.tag("Block"));
        registrar.addInputAlias(BlockStatePredicate.class, Types.REGEXP);
        registrar.addInputAlias(BlockStatePredicate.class, Types.literal("*"));
        registrar.addInputAlias(BlockStatePredicate.class, Types.literal("-"));
        registrar.addInputAlias(RuleTest.class, BlockStatePredicate.class);
        registrar.addInputAlias(RuleTest.class, CompoundTag.class);
        registrar.addInputAlias(MobCategory.class, Types.STRING);
        registrar.addInputAlias(LootContext.EntityTarget.class, Types.STRING);
        registrar.addInputAlias(CopyNameFunction.NameSource.class, Types.STRING);
        registrar.addInputAlias(BiomeFilter.class, RegistryTypes.object("WorldgenBiome"));
        registrar.addInputAlias(BiomeFilter.class, Types.REGEXP);
        registrar.addInputAlias(BiomeFilter.class, Types.clazz(BiomeFilter.class).asInput().asArray());
        registrar.addInputAlias(BiomeFilter.class, Types.object(builder -> {
            builder.param("or", true, Types.clazz(BiomeFilter.class).asInput());
            builder.param("not", true, Types.clazz(BiomeFilter.class).asInput());
            builder.param("id", true, RegistryTypes.object("WorldgenBiome"));
            builder.param("type", true, RegistryTypes.object("WorldgenBiome"));
            builder.param("tag", true, RegistryTypes.tag("WorldgenBiome"));
        }));
        registrar.addInputAlias(Tier.class, Types.STRING);
        registrar.addInputAlias(ArmorMaterial.class, Types.STRING);
        registrar.addInputAlias(EntitySelector.class, Types.STRING);
        registrar.addInputAlias(ReplacementMatch.class, Ingredient.class);
        registrar.addInputAlias(Stat.class, Types.STRING);
        registrar.addInputAlias(MapColorHelper.class, Types.STRING);
        registrar.addInputAlias(MapColorHelper.class, Types.NUMBER);
        registrar.addInputAlias(SoundType.class, Types.STRING);
        registrar.addInputAlias(ParticleOptions.class, Types.STRING);
        registrar.addInputAlias(ItemTintFunction.class, Types.clazz(ItemTintFunction.class).asInput().asArray());
        registrar.addInputAlias(ItemTintFunction.class, Types.STRING);
        registrar.addInputAlias(ItemTintFunction.class, Types.lambda(builder -> {
            builder.param("stack", Types.clazz(Item.class));
            builder.param("index", Types.NUMBER);
            builder.returns(Types.clazz(KubeColor.class).asInput());
        }));
        registrar.addInputAlias(BlockTintFunction.class, Types.clazz(BlockTintFunction.class).asInput().asArray());
        registrar.addInputAlias(BlockTintFunction.class, Types.STRING);
        registrar.addInputAlias(BlockTintFunction.class, Types.lambda(builder -> {
            builder.param("state", Types.clazz(BlockState.class));
            builder.param("level", Types.clazz(BlockAndTintGetter.class));
            builder.param("pos", Types.clazz(BlockPos.class).asInput());
            builder.param("index", Types.NUMBER);
            builder.returns(Types.clazz(KubeColor.class).asInput());
        }));

        registrar.addInputAlias(Component.class, Types.STRING);
        registrar.addInputAlias(Component.class, Types.object(builder -> {
            builder.param("text", true, Types.STRING);
            builder.param("translate", true, Types.STRING); // TODO: Add lang key type
            builder.param("with", true, Types.ANY.asArray());
            builder.param("color", true, Types.clazz(KubeColor.class).asInput());
            builder.param("bold", true, Types.BOOLEAN);
            builder.param("italic", true, Types.BOOLEAN);
            builder.param("underlined", true, Types.BOOLEAN);
            builder.param("strikethrough", true, Types.BOOLEAN);
            builder.param("obfuscated", true, Types.BOOLEAN);
            builder.param("insertion", true, Types.STRING);
            builder.param("font", true, Types.STRING);
            builder.param("click", true, Types.clazz(ClickEvent.class).asInput());
            builder.param("hover", true, Types.clazz(Component.class).asInput());
            builder.param("extra", true, Types.clazz(Component.class).asInput().asArray());
        }));
        registrar.addInputAlias(Component.class, Types.clazz(Component.class).asInput().asArray());

        registrar.addInputAlias(MutableComponent.class, Types.STRING);
        registrar.addInputAlias(MutableComponent.class, Types.object(builder -> {
            builder.param("text", true, Types.STRING);
            builder.param("translate", true, Types.STRING); // TODO: Add lang key type
            builder.param("with", true, Types.ANY.asArray());
            builder.param("color", true, Types.clazz(KubeColor.class).asInput());
            builder.param("bold", true, Types.BOOLEAN);
            builder.param("italic", true, Types.BOOLEAN);
            builder.param("underlined", true, Types.BOOLEAN);
            builder.param("strikethrough", true, Types.BOOLEAN);
            builder.param("obfuscated", true, Types.BOOLEAN);
            builder.param("insertion", true, Types.STRING);
            builder.param("font", true, Types.STRING);
            builder.param("click", true, Types.clazz(ClickEvent.class).asInput());
            builder.param("hover", true, Types.clazz(MutableComponent.class).asInput());
            builder.param("extra", true, Types.clazz(MutableComponent.class).asInput().asArray());
        }));
        registrar.addInputAlias(MutableComponent.class, Types.clazz(MutableComponent.class).asInput().asArray());

        for (String color : ColorWrapper.MAP.keySet()) {
            registrar.addInputAlias(KubeColor.class, Types.literal(color.toLowerCase()));
            registrar.addInputAlias(TextColor.class, Types.literal(color.toLowerCase()));
        }
        registrar.addInputAlias(KubeColor.class, Types.raw("`#${string}`"));
        registrar.addInputAlias(KubeColor.class, Types.NUMBER);
        registrar.addInputAlias(TextColor.class, Types.raw("`#${string}`"));
        registrar.addInputAlias(TextColor.class, Types.NUMBER);


        registrar.addInputAlias(ClickEvent.class, Types.object(builder -> {
            builder.param("action", Types.clazz(ClickEvent.Action.class).asInput());
            builder.param("value", Types.STRING);
        }));

        registrar.addInputAlias(DataComponentMap.class, Types.STRING);
        registrar.addInputAlias(ItemEnchantments.class, new EnchantmentType());
    }

    private static class EnchantmentType extends Type {
        private final static NamespacedType ENCHANTMENT = RegistryTypes.object("Enchantment");

        @Override
        public Set<ClassPath> getImports() {
            return ENCHANTMENT.getImports();
        }

        @Override
        public List<String> format(int indent) {
            return List.of("{[key in %s]?: number}".formatted(ENCHANTMENT.first()));
        }

        @Override
        public Collection<Code> getContainedTypes() {
            return List.of();
        }
    }
}
