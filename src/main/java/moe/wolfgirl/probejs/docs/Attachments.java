package moe.wolfgirl.probejs.docs;

import dev.latvian.mods.kubejs.block.entity.BlockEntityAttachmentType;
import dev.latvian.mods.kubejs.block.entity.BlockEntityInfo;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.transpiler.TypeConverter;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;
import moe.wolfgirl.probejs.lang.typescript.code.member.ClassDecl;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Attachments extends ProbeJSPlugin {

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        TypeScriptFile typeScriptFile = globalClasses.get(new ClassPath(BlockEntityInfo.class));
        ClassDecl classDecl = typeScriptFile.findCode(ClassDecl.class).orElse(null);
        if (classDecl == null) return;

        classDecl.methods.removeIf(methodDecl -> methodDecl.name.equals("attach"));
        TypeConverter converter = scriptDump.transpiler.typeConverter;
        for (Map.Entry<String, BlockEntityAttachmentType> entry : BlockEntityAttachmentType.ALL.get().entrySet()) {
            String id = entry.getKey();
            BlockEntityAttachmentType attachment = entry.getValue();

            var baseType = converter.convertType(attachment.typeInfo());
            baseType.getUsedImports().forEach(typeScriptFile.declaration::addClass);

            classDecl.methods.add(Types.lambda()
                    .method()
                    .param("type", Types.literal(id))
                    .param("input", baseType)
                    .build()
                    .asMethod("attach")
            );
        }
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        HashSet<Class<?>> classes = new HashSet<>();

        for (BlockEntityAttachmentType value : BlockEntityAttachmentType.ALL.get().values()) {
            classes.addAll(value.typeInfo().getContainedComponentClasses());
        }

        return classes;
    }
}
