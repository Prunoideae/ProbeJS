package moe.wolfgirl.probejs.docs.assignments;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.latvian.mods.unit.Unit;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.docs.Primitives;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.TemporalAmount;
import java.util.*;

public class JavaPrimitives extends ProbeJSPlugin {
    @Override
    public void assignType(ScriptDump scriptDump) {
        scriptDump.assignType(List.class, Types.generic("E").asArray());
        scriptDump.assignType(Map.class, Types.primitive("{[key: string]: V}"));
        scriptDump.assignType(Iterable.class, Types.generic("T").asArray());
        scriptDump.assignType(Collection.class, Types.generic("E").asArray());
        scriptDump.assignType(Set.class, Types.generic("E").asArray());
        scriptDump.assignType(UUID.class, Types.STRING);
        scriptDump.assignType(JsonObject.class, Types.OBJECT);
        scriptDump.assignType(JsonArray.class, Types.ANY.asArray());
        scriptDump.assignType(JsonPrimitive.class, Types.NUMBER);
        scriptDump.assignType(JsonPrimitive.class, Types.STRING);
        scriptDump.assignType(JsonPrimitive.class, Types.BOOLEAN);
        scriptDump.assignType(JsonPrimitive.class, Types.NULL);
        scriptDump.assignType(JsonElement.class, Types.type(JsonObject.class));
        scriptDump.assignType(JsonElement.class, Types.type(JsonArray.class));
        scriptDump.assignType(JsonElement.class, Types.type(JsonPrimitive.class));
        scriptDump.assignType(Path.class, Types.STRING);
        scriptDump.assignType(File.class, Types.type(Path.class));
        scriptDump.assignType(Unit.class, Types.STRING);
        scriptDump.assignType(Unit.class, Types.NUMBER);
        scriptDump.assignType(TemporalAmount.class, Types.STRING);
        scriptDump.assignType(TemporalAmount.class, Types.NUMBER);
        scriptDump.assignType(Duration.class, Types.type(TemporalAmount.class));
        scriptDump.assignType(ResourceLocation.class, Types.STRING);
        scriptDump.assignType(CompoundTag.class, Types.OBJECT);
        scriptDump.assignType(CollectionTag.class, Types.ANY.asArray());
        scriptDump.assignType(ListTag.class, Types.ANY.asArray());
        scriptDump.assignType(Tag.class, Types.STRING);
        scriptDump.assignType(Tag.class, Types.NUMBER);
        scriptDump.assignType(Tag.class, Types.BOOLEAN);
        scriptDump.assignType(Tag.class, Types.OBJECT);
        scriptDump.assignType(Tag.class, Types.ANY.asArray());
        scriptDump.assignType(BlockPos.class, Types.arrayOf(Primitives.INTEGER, Primitives.INTEGER, Primitives.INTEGER));
        scriptDump.assignType(Vec3.class, Types.arrayOf(Primitives.DOUBLE, Primitives.DOUBLE, Primitives.DOUBLE));
        scriptDump.assignType(MobCategory.class, Types.STRING);
        scriptDump.assignType(AABB.class, Types.arrayOf());
        scriptDump.assignType(AABB.class, Types.arrayOf(Primitives.DOUBLE, Primitives.DOUBLE, Primitives.DOUBLE));
        scriptDump.assignType(AABB.class, Types.arrayOf(Primitives.DOUBLE, Primitives.DOUBLE, Primitives.DOUBLE,
                Primitives.DOUBLE, Primitives.DOUBLE, Primitives.DOUBLE));
        scriptDump.assignType(IntProvider.class, Primitives.INTEGER);
        scriptDump.assignType(IntProvider.class, Types.arrayOf(Primitives.INTEGER, Primitives.INTEGER));
        scriptDump.assignType(IntProvider.class, Types.object()
                .member("bounds", Types.arrayOf(Primitives.INTEGER, Primitives.INTEGER))
                .build());
        scriptDump.assignType(IntProvider.class, Types.object()
                .member("min", Primitives.INTEGER)
                .member("max", Primitives.INTEGER)
                .build());
        scriptDump.assignType(IntProvider.class, Types.object()
                .member("min_inclusive", Primitives.INTEGER)
                .member("max_inclusive", Primitives.INTEGER)
                .build());
        scriptDump.assignType(IntProvider.class, Types.object()
                .member("value", Primitives.INTEGER)
                .build());
        scriptDump.assignType(IntProvider.class, Types.object()
                .member("clamped", Types.type(IntProvider.class))
                .build());
        scriptDump.assignType(IntProvider.class, Types.object()
                .member("clamped_normal", Types.type(IntProvider.class))
                .build());
        scriptDump.assignType(NumberProvider.class, Primitives.DOUBLE);
        scriptDump.assignType(NumberProvider.class, Types.arrayOf(Primitives.DOUBLE, Primitives.DOUBLE));
        scriptDump.assignType(NumberProvider.class, Types.object()
                .member("min", Primitives.DOUBLE)
                .member("max", Primitives.DOUBLE)
                .build());
        scriptDump.assignType(NumberProvider.class, Types.object()
                .member("n", Primitives.DOUBLE)
                .member("p", Primitives.DOUBLE)
                .build());
        scriptDump.assignType(NumberProvider.class, Types.object()
                .member("value", Primitives.DOUBLE)
                .build());
    }
}
