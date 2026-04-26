package moe.wolfgirl.probejs.docs;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.transpiler.transformation.InjectBeans;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;
import moe.wolfgirl.probejs.lang.typescript.code.Code;
import moe.wolfgirl.probejs.lang.typescript.code.member.ClassDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.MethodDecl;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.Map;

public class ObjectIDs extends ProbeJSPlugin {
    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        replaceIdGetter(globalClasses, Block.class, "getId", Types.primitive("Special.Block"));
        replaceIdGetter(globalClasses, Item.class, "getId", Types.primitive("Special.Item"));
        replaceIdGetter(globalClasses, ItemStack.class, "getId", Types.primitive("Special.Item"));
    }

    private void replaceIdGetter(Map<ClassPath, TypeScriptFile> globalClasses, Class<?> clazz, String methodName, BaseType replaced) {
        ClassDecl classDecl = findClassFile(globalClasses, clazz).findCode(ClassDecl.class).orElse(null);
        if (classDecl == null) return;

        for (MethodDecl method : classDecl.methods) {
            if (method.name.equals(methodName)) {
                method.returnType = replaced;
            }
        }

        String beanName = InjectBeans.getBeanName(methodName);
        for (Code code : classDecl.bodyCode) {
            if (code instanceof InjectBeans.BeanDecl beanDecl && beanDecl.name.equals(beanName)) {
                beanDecl.baseType = replaced;
            }
        }
    }
}
