package moe.wolfgirl.probejs.docs;

import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.block.entity.BlockEntityAttachmentType;
import dev.latvian.mods.kubejs.block.entity.BlockEntityInfo;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.transpiler.TypeConverter;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;
import moe.wolfgirl.probejs.lang.typescript.code.member.ClassDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.ParamDecl;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.utils.DocUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Attachments extends ProbeJSPlugin {

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        TypeScriptFile typeScriptFile = globalClasses.get(new ClassPath(BlockEntityInfo.class));
        ClassDecl classDecl = typeScriptFile.findCode(ClassDecl.class).orElse(null);
        if (classDecl == null) return;

        TypeConverter converter = scriptDump.transpiler.typeConverter;

        DocUtils.generateMappedType("AttachmentMap", "Attachments",
                BlockEntityAttachmentType.ALL.get()
                        .entrySet()
                        .stream()
                        .map(entry -> Pair.of(entry.getKey(), converter.convertType(entry.getValue().typeInfo())))
                        .collect(Collectors.toSet()),
                typeScriptFile);

        classDecl.methods.stream()
                .filter(methodDecl -> methodDecl.name.equals("attach"))
                .findFirst()
                .ifPresent(attach -> {
                    attach.variableTypes.add(Types.generic("T", Types.primitive("Attachments")));
                    attach.params.set(1, new ParamDecl("type", Types.generic("T"), false, false));
                    attach.params.set(3, new ParamDecl("args", Types.primitive("%s[T]".formatted("AttachmentMap")), false, false));
                });
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
