package moe.wolfgirl.probejs.next.plugin.builtins;

import dev.architectury.fluid.FluidStack;
import moe.wolfgirl.probejs.next.ClassPath;
import moe.wolfgirl.probejs.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.next.typescript.base.AliasRegistrar;
import moe.wolfgirl.probejs.next.typescript.base.DocumentRegistrar;
import moe.wolfgirl.probejs.next.typescript.document.Types;
import moe.wolfgirl.probejs.next.typescript.document.base.Code;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Set;

public class TestDocument extends ProbeJSPlugin {

    @Override
    public void addSpecialDocuments(DocumentRegistrar registrar) {
        registrar.addDocument(ClassPath.special("foo.Foo"), new Foo());
        registrar.addGlobal(ClassPath.special("bar.Bar"), new Bar());
    }

    @Override
    public void addTypeAlias(AliasRegistrar registrar) {
        registrar.addInputAlias(ItemStack.class, Types.lambda(builder -> {
            builder.param("item", Types.clazz(Item.class));
        }));
    }

    @Override
    public Set<Class<?>> provideClassForDiscovery() {
        return Set.of(Item.class, FluidStack.class, Block.class);
    }

    static class Foo extends Code {

        @Override
        public Set<ClassPath> getImports() {
            return Set.of();
        }

        @Override
        public List<String> format(int indent) {
            return List.of("export class Foo {}");
        }
    }

    static class Bar extends Code {

        @Override
        public Set<ClassPath> getImports() {
            return Set.of();
        }

        @Override
        public List<String> format(int indent) {
            return List.of("function bar(): void;");
        }
    }
}
