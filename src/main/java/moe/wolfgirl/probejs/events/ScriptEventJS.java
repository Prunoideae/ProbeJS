package moe.wolfgirl.probejs.events;

import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.script.ScriptType;
import moe.wolfgirl.probejs.lang.transpiler.TypeConverter;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;

public class ScriptEventJS extends EventJS {
    protected final ScriptDump dump;

    public ScriptEventJS(ScriptDump dump) {
        this.dump = dump;
    }

    public ScriptType getScriptType() {
        return dump.scriptType;
    }

    public TypeConverter getTypeConverter() {
        return dump.transpiler.typeConverter;
    }
}
