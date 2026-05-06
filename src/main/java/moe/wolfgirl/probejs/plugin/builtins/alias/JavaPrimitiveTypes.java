package moe.wolfgirl.probejs.plugin.builtins.alias;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import dev.latvian.mods.kubejs.util.TickDuration;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.typescript.base.AliasRegistrar;
import moe.wolfgirl.probejs.typescript.document.Types;
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

public class JavaPrimitiveTypes extends ProbeJSPlugin {
    @Override
    public void addTypeAlias(AliasRegistrar registrar) {
        registrar.addInputAlias(List.class, Types.variable("E").asArray());
        registrar.addInputAlias(Map.class, Types.raw("{[key: string]: V}"));
        registrar.addInputAlias(Iterable.class, Types.variable("T").asArray());
        registrar.addInputAlias(Collection.class, Types.variable("E").asArray());
        registrar.addInputAlias(Set.class, Types.variable("E").asArray());
        registrar.addInputAlias(UUID.class, Types.STRING);
        registrar.addInputAlias(JsonObject.class, Types.OBJECT);
        registrar.addInputAlias(JsonArray.class, Types.ANY.asArray());
        registrar.addInputAlias(JsonPrimitive.class, Types.NUMBER);
        registrar.addInputAlias(JsonPrimitive.class, Types.STRING);
        registrar.addInputAlias(JsonPrimitive.class, Types.BOOLEAN);
        registrar.addInputAlias(JsonPrimitive.class, Types.raw("null"));
        registrar.addInputAlias(JsonElement.class, Types.clazz(JsonObject.class));
        registrar.addInputAlias(JsonElement.class, Types.clazz(JsonArray.class));
        registrar.addInputAlias(JsonElement.class, Types.clazz(JsonPrimitive.class));
        registrar.addInputAlias(Path.class, Types.STRING);
        registrar.addInputAlias(File.class, Types.clazz(Path.class));
        registrar.addInputAlias(TemporalAmount.class, Types.STRING);
        registrar.addInputAlias(TemporalAmount.class, Types.NUMBER);
        registrar.addInputAlias(Duration.class, Types.clazz(TemporalAmount.class));
        registrar.addInputAlias(ResourceLocation.class, Types.STRING);
        registrar.addInputAlias(CompoundTag.class, Types.OBJECT);
        registrar.addInputAlias(CollectionTag.class, Types.ANY.asArray());
        registrar.addInputAlias(ListTag.class, Types.ANY.asArray());
        registrar.addInputAlias(Tag.class, Types.STRING);
        registrar.addInputAlias(Tag.class, Types.NUMBER);
        registrar.addInputAlias(Tag.class, Types.BOOLEAN);
        registrar.addInputAlias(Tag.class, Types.OBJECT);
        registrar.addInputAlias(Tag.class, Types.ANY.asArray());
        registrar.addInputAlias(BlockPos.class, Types.fixedArray(b -> b
                .param("x", Types.NUMBER)
                .param("y", Types.NUMBER)
                .param("z", Types.NUMBER)));
        registrar.addInputAlias(Vec3.class, Types.fixedArray(b -> b
                .param("x", Types.NUMBER)
                .param("y", Types.NUMBER)
                .param("z", Types.NUMBER)));
        registrar.addInputAlias(MobCategory.class, Types.STRING);
        registrar.addInputAlias(AABB.class, Types.raw("[]"));
        registrar.addInputAlias(AABB.class, Types.fixedArray(b -> b
                .param("x", Types.NUMBER)
                .param("y", Types.NUMBER)
                .param("z", Types.NUMBER)));
        registrar.addInputAlias(AABB.class, Types.fixedArray(b -> b
                .param("x1", Types.NUMBER)
                .param("y1", Types.NUMBER)
                .param("z1", Types.NUMBER)
                .param("x2", Types.NUMBER)
                .param("y2", Types.NUMBER)
                .param("z2", Types.NUMBER)));
        registrar.addInputAlias(IntProvider.class, Types.NUMBER);
        registrar.addInputAlias(IntProvider.class, Types.fixedArray(b -> b
                .param("min", Types.NUMBER)
                .param("max", Types.NUMBER)));
        registrar.addInputAlias(IntProvider.class, Types.object(b -> b
                .param("bounds", Types.fixedArray(bb -> bb
                        .param("min", Types.NUMBER)
                        .param("max", Types.NUMBER)))));
        registrar.addInputAlias(IntProvider.class, Types.object(b -> b
                .param("min", Types.NUMBER)
                .param("max", Types.NUMBER)));
        registrar.addInputAlias(IntProvider.class, Types.object(b -> b
                .param("min_inclusive", Types.NUMBER)
                .param("max_inclusive", Types.NUMBER)));
        registrar.addInputAlias(IntProvider.class, Types.object(b -> b.param("value", Types.NUMBER)));
        registrar.addInputAlias(IntProvider.class, Types.object(b -> b.param("clamped", Types.clazz(IntProvider.class))));
        registrar.addInputAlias(IntProvider.class, Types.object(b -> b.param("clamped_normal", Types.clazz(IntProvider.class))));
        registrar.addInputAlias(NumberProvider.class, Types.NUMBER);
        registrar.addInputAlias(NumberProvider.class, Types.fixedArray(b -> b
                .param("min", Types.NUMBER)
                .param("max", Types.NUMBER)));
        registrar.addInputAlias(NumberProvider.class, Types.object(b -> b
                .param("min", Types.NUMBER)
                .param("max", Types.NUMBER)));
        registrar.addInputAlias(NumberProvider.class, Types.object(b -> b
                .param("n", Types.NUMBER)
                .param("p", Types.NUMBER)));
        registrar.addInputAlias(NumberProvider.class, Types.object(b -> b.param("value", Types.NUMBER)));
        registrar.addInputAlias(TickDuration.class, Types.NUMBER);
    }

    @Override
    public Set<Class<?>> provideClassForDiscovery() {
        return Set.of(
                List.class,
                Map.class,
                Iterable.class,
                Collection.class,
                Set.class,
                UUID.class,
                JsonObject.class,
                JsonArray.class,
                JsonPrimitive.class,
                JsonElement.class,
                Path.class,
                File.class,
                TemporalAmount.class,
                Duration.class,
                ResourceLocation.class,
                CompoundTag.class,
                CollectionTag.class,
                ListTag.class,
                Tag.class,
                BlockPos.class,
                Vec3.class,
                MobCategory.class,
                AABB.class,
                IntProvider.class,
                NumberProvider.class
        );
    }
}
