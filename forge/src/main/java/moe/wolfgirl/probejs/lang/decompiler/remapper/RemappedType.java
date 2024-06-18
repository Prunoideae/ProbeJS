package moe.wolfgirl.probejs.lang.decompiler.remapper;

import dev.latvian.mods.rhino.mod.util.RemappingHelper;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.Objects;
import java.util.Optional;

public class RemappedType {
    public final RemappedClass parent;
    public final int array;
    public Optional<Class<?>> realClass;
    public String descriptorString;

    public RemappedType(RemappedClass parent, int array) {
        this.parent = parent;
        this.array = array;
        // ???
        this.realClass = null;
    }

    @Override
    public String toString() {
        if (array == 0) {
            return parent.toString();
        }

        return parent.toString() + "[]".repeat(array);
    }

    public boolean isRemapped() {
        return array == 0 && parent.remapped;
    }

    @Nullable
    private Class<?> getRealClass(boolean debug) {
        if (realClass == null) {
            var r = RemappingHelper.getClass(parent.realName);

            if (r.isPresent()) {
                if (array > 0) {
                    realClass = Optional.of(Array.newInstance(r.get(), array).getClass());
                } else {
                    realClass = r;
                }
            } else {
                realClass = Optional.empty();

                if (debug) {
                    RemappingHelper.LOGGER.error("Class " + parent.realName + " / " + parent.remappedName + " not found!");
                }
            }
        }

        return realClass.orElse(null);
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || obj instanceof RemappedType type && type.parent == parent && type.array == array;
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, array);
    }

    public String descriptorString() {
        if (descriptorString == null) {
            if (array > 0) {
                descriptorString = "[".repeat(array) + parent.descriptorString();
            } else {
                descriptorString = parent.descriptorString();
            }
        }

        return descriptorString;
    }
}
