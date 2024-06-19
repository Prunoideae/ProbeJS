package moe.wolfgirl.probejs.events;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import moe.wolfgirl.probejs.lang.transpiler.TypeConverter;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;

public class ScriptEventJS implements KubeEvent {
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
