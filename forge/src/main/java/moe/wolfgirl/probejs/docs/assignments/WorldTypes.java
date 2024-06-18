package moe.wolfgirl.probejs.docs.assignments;

import dev.latvian.mods.kubejs.block.BlockTintFunction;
import dev.latvian.mods.kubejs.block.MapColorHelper;
import dev.latvian.mods.kubejs.block.state.BlockStatePredicate;
import dev.latvian.mods.kubejs.core.PlayerSelector;
import dev.latvian.mods.kubejs.item.ItemTintFunction;
import dev.latvian.mods.kubejs.level.gen.filter.mob.MobFilter;
import dev.latvian.mods.kubejs.recipe.ReplacementMatch;
import dev.latvian.mods.kubejs.util.NotificationBuilder;
import dev.latvian.mods.rhino.mod.util.color.Color;
import dev.latvian.mods.rhino.mod.wrapper.ColorWrapper;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.docs.Primitives;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.CopyNameFunction;

public class WorldTypes extends ProbeJSPlugin {
    @Override
    public void assignType(ScriptDump scriptDump) {
        scriptDump.assignType(BlockStatePredicate.class, Types.type(BlockStatePredicate.class).asArray());
        scriptDump.assignType(BlockStatePredicate.class, Types.object()
                .member("or?", Types.type(BlockStatePredicate.class))
                .member("not?", Types.type(BlockStatePredicate.class))
                .build());
        scriptDump.assignType(BlockStatePredicate.class, Types.type(Block.class));
        scriptDump.assignType(BlockStatePredicate.class, Types.primitive("Special.BlockTag"));
        scriptDump.assignType(BlockStatePredicate.class, Types.primitive("RegExp"));
        scriptDump.assignType(BlockStatePredicate.class, Types.literal("*"));
        scriptDump.assignType(BlockStatePredicate.class, Types.literal("-"));
        scriptDump.assignType(RuleTest.class, Types.ignoreContext(Types.type(BlockStatePredicate.class), BaseType.FormatType.RETURN));
        scriptDump.assignType(RuleTest.class, Types.type(CompoundTag.class));
        scriptDump.assignType(MobCategory.class, Types.STRING);
        scriptDump.assignType(LootContext.EntityTarget.class, Types.STRING);
        scriptDump.assignType(CopyNameFunction.NameSource.class, Types.STRING);
        scriptDump.assignType(BiomeFilter.class, Types.primitive("Special.Biome"));
        scriptDump.assignType(BiomeFilter.class, Types.primitive("RegExp"));
        scriptDump.assignType(BiomeFilter.class, Types.type(BiomeFilter.class).asArray());
        scriptDump.assignType(BiomeFilter.class, Types.object()
                .member("or?", Types.type(BiomeFilter.class))
                .member("not?", Types.type(BiomeFilter.class))
                .member("id?", Types.primitive("Special.Biome"))
                .member("type?", Types.primitive("Special.Biome"))
                .member("tag", Types.primitive("Special.BiomeTag"))
                .build());
        scriptDump.assignType(MobFilter.class, Types.primitive("Special.EntityType"));
        scriptDump.assignType(MobFilter.class, Types.type(MobFilter.class).asArray());
        scriptDump.assignType(MobFilter.class, Types.object()
                .member("or?", Types.type(MobFilter.class))
                .member("not?", Types.type(MobFilter.class))
                .member("id?", Types.primitive("Special.EntityType"))
                .member("type?", Types.primitive("Special.EntityType"))
                .member("category", Types.STRING)
                .build());
        scriptDump.assignType(Tier.class, Types.STRING);
        scriptDump.assignType(ArmorMaterial.class, Types.STRING);
        scriptDump.assignType(PlayerSelector.class, Types.STRING);
        scriptDump.assignType(EntitySelector.class, Types.STRING);
        scriptDump.assignType(ReplacementMatch.class, Types.type(Ingredient.class));
        scriptDump.assignType(Stat.class, Types.STRING);
        scriptDump.assignType(NotificationBuilder.class, Types.lambda()
                .param("builder", Types.type(NotificationBuilder.class))
                .build());
        scriptDump.assignType(NotificationBuilder.class, Types.type(Component.class));
        scriptDump.assignType(MapColorHelper.class, Types.STRING);
        scriptDump.assignType(MapColorHelper.class, Types.NUMBER);
        scriptDump.assignType(SoundType.class, Types.STRING);
        scriptDump.assignType(ParticleOptions.class, Types.STRING);
        scriptDump.assignType(ItemTintFunction.class, Types.type(ItemTintFunction.class).asArray());
        scriptDump.assignType(ItemTintFunction.class, Types.STRING);
        scriptDump.assignType(ItemTintFunction.class, Types.lambda()
                .param("stack", Types.type(ItemStack.class))
                .param("index", Primitives.INTEGER)
                .build());
        scriptDump.assignType(BlockTintFunction.class, Types.type(BlockTintFunction.class).asArray());
        scriptDump.assignType(BlockTintFunction.class, Types.STRING);
        scriptDump.assignType(BlockTintFunction.class, Types.lambda()
                .param("state", Types.type(ItemStack.class))
                .param("level", Types.type(BlockAndTintGetter.class))
                .param("pos", Types.type(BlockPos.class))
                .param("index", Primitives.INTEGER)
                .returnType(Types.type(Color.class))
                .build());

        scriptDump.assignType(Component.class, Types.STRING);
        scriptDump.assignType(Component.class, Types.object()
                .member("text?", Types.STRING)
                .member("translate?", Types.primitive("Special.LangKey"))
                .member("with?", Types.ANY.asArray())
                .member("color?", Types.type(Color.class))
                .member("bold?", Types.BOOLEAN)
                .member("italic?", Types.BOOLEAN)
                .member("underlined?", Types.BOOLEAN)
                .member("strikethrough?", Types.BOOLEAN)
                .member("obfuscated?", Types.BOOLEAN)
                .member("insertion?", Types.STRING)
                .member("font?", Types.STRING)
                .member("click?", Types.type(ClickEvent.class))
                .member("hover?", Types.type(Component.class))
                .member("extra?", Types.type(Component.class).asArray())
                .build());
        scriptDump.assignType(Component.class, Types.type(Component.class).asArray());

        scriptDump.assignType(MutableComponent.class, Types.STRING);
        scriptDump.assignType(MutableComponent.class, Types.object()
                .member("text?", Types.STRING)
                .member("translate?", Types.primitive("Special.LangKey"))
                .member("with?", Types.ANY.asArray())
                .member("color?", Types.type(Color.class))
                .member("bold?", Types.BOOLEAN)
                .member("italic?", Types.BOOLEAN)
                .member("underlined?", Types.BOOLEAN)
                .member("strikethrough?", Types.BOOLEAN)
                .member("obfuscated?", Types.BOOLEAN)
                .member("insertion?", Types.STRING)
                .member("font?", Types.STRING)
                .member("click?", Types.type(ClickEvent.class))
                .member("hover?", Types.type(MutableComponent.class))
                .member("extra?", Types.type(MutableComponent.class).asArray())
                .build());
        scriptDump.assignType(MutableComponent.class, Types.type(MutableComponent.class).asArray());

        BaseType[] predefinedColors = ColorWrapper.MAP.keySet().stream().map(Types::literal).toArray(BaseType[]::new);
        scriptDump.assignType(Color.class, Types.or(predefinedColors));
        scriptDump.assignType(Color.class, Types.primitive("`#${string}`"));
        scriptDump.assignType(Color.class, Primitives.INTEGER);

        scriptDump.assignType(TextColor.class, Types.or(predefinedColors));
        scriptDump.assignType(TextColor.class, Types.primitive("`#${string}`"));
        scriptDump.assignType(TextColor.class, Primitives.INTEGER);

        BaseType[] actions = new BaseType[]{
                Types.literal("open_url"),
                Types.literal("open_file"),
                Types.literal("run_command"),
                Types.literal("suggest_command"),
                Types.literal("change_page"),
                Types.literal("copy_to_clipboard"),
        };
        scriptDump.assignType(ClickEvent.class, Types.object()
                .member("action", Types.or(actions))
                .member("value", Types.STRING)
                .build());
    }
}
