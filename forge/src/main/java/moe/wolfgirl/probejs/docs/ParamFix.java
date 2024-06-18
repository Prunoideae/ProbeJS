package moe.wolfgirl.probejs.docs;

import dev.latvian.mods.kubejs.bindings.TextWrapper;
import dev.latvian.mods.kubejs.item.InputItem;
import dev.latvian.mods.kubejs.item.OutputItem;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import moe.wolfgirl.probejs.utils.DocUtils;
import net.minecraft.network.chat.MutableComponent;

import java.util.Map;

public class ParamFix extends ProbeJSPlugin {
    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {

        var textWrapper = globalClasses.get(new ClassPath(TextWrapper.class));
        DocUtils.replaceParamType(
                textWrapper,
                m -> m.params.size() == 1 && m.name.equals("of"),
                0,
                Types.type(MutableComponent.class)
        );

        var outputItem = globalClasses.get(new ClassPath(OutputItem.class));
        DocUtils.replaceParamType(
                outputItem,
                m -> m.params.size() == 1 && m.name.equals("of"),
                0,
                Types.type(OutputItem.class)
        );

        var inputItem = globalClasses.get(new ClassPath(InputItem.class));
        DocUtils.replaceParamType(
                inputItem,
                m -> m.params.size() == 1 && m.name.equals("of"),
                0,
                Types.type(InputItem.class)
        );
    }
}
