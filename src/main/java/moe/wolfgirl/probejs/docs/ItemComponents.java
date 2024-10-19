package moe.wolfgirl.probejs.docs;

import dev.latvian.mods.kubejs.component.DataComponentWrapper;
import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.transpiler.TypeConverter;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;
import moe.wolfgirl.probejs.lang.typescript.code.member.ClassDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.MethodDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.TypeDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.clazz.MethodBuilder;
import moe.wolfgirl.probejs.lang.typescript.code.ts.Statements;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import moe.wolfgirl.probejs.lang.typescript.code.type.js.JSObjectType;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ItemComponents extends ProbeJSPlugin {
    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        TypeConverter typeConverter = scriptDump.transpiler.typeConverter;

        JSObjectType.Builder builder = Types.object();
        for (Map.Entry<DataComponentType<?>, TypeInfo> entry : DataComponentWrapper.TYPE_INFOS.get().entrySet()) {
            ResourceLocation componentType = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(entry.getKey());
            BaseType baseType = typeConverter.convertType(entry.getValue());

            assert componentType != null; // An unregistered component type but found in type infos?
            builder.member(componentType.toString(), baseType);
        }

        TypeScriptFile scriptFile = globalClasses.get(new ClassPath(ItemStack.class));
        assert scriptFile != null; // Must be very serious to have this null

        patchClass(builder, ItemStack.class, globalClasses);
        patchClass(builder, FluidStack.class, globalClasses);
    }

    private void patchClass(JSObjectType.Builder builder, Class<?> thisType, Map<ClassPath, TypeScriptFile> globalClasses) {
        TypeScriptFile scriptFile = globalClasses.get(new ClassPath(thisType));
        assert scriptFile != null;

        scriptFile.addCode(new TypeDecl("ComponentTypeMap", builder.buildIndexed()));
        scriptFile.addCode(new TypeDecl("ComponentTypes", Types.primitive("keyof ComponentTypeMap")));

        ClassDecl classDecl = scriptFile.findCode(ClassDecl.class).orElseThrow();
        classDecl.methods.removeIf(methodDecl -> methodDecl.name.equals("set"));
        classDecl.methods.add(new MethodBuilder("set")
                .typeVariables(Types.generic("T", Types.primitive("ComponentTypes")))
                .param("componentType", Types.generic("T"))
                .param("value", Types.primitive("ComponentTypeMap[T]"))
                .returnType(Types.type(thisType))
                .buildAsMethod()
        );
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        Set<Class<?>> toAdd = new HashSet<>();
        toAdd.add(DataComponentType.class);
        toAdd.add(FluidStack.class);
        toAdd.add(ItemStack.class);
        for (TypeInfo value : DataComponentWrapper.TYPE_INFOS.get().values()) {
            if (!value.isPrimitive()) {
                toAdd.addAll(value.getContainedComponentClasses());
            }
        }
        return toAdd;
    }
}
