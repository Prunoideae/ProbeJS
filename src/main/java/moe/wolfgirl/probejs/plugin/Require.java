package moe.wolfgirl.probejs.plugin;


import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.rhino.*;
import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;

import java.util.Arrays;

public class Require extends BaseFunction {
    private final KubeJSContext context;

    public Require(KubeJSContext context) {

        this.context = context;
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        String result = (String) context.jsToJava(args[0], TypeInfo.STRING);
        if (!result.startsWith("packages")) {
            return new RequireWrapper(null, Undefined.INSTANCE);
        }
        String[] parts = result.split("/", 2);
        ClassPath path = new ClassPath(Arrays.stream(parts[1].split("/")).toList());

        var loaded = context.loadJavaClass(path.getClassPathJava(), false);
        if (loaded == null) {
            ((KubeJSContext) cx).getConsole().warn("Class '%s' not loaded".formatted(path.getClassPathJava()));
        }
        return new RequireWrapper(path, loaded == null ? Undefined.INSTANCE : loaded);
    }

    public static class RequireWrapper extends ScriptableObject {
        private final ClassPath path;
        private final Object clazz;

        public RequireWrapper(ClassPath path, Object clazz) {
            this.path = path;
            this.clazz = clazz;
        }

        @Override
        public String getClassName() {
            return path.getClassPathJava();
        }

        @Override
        public Object get(Context cx, String name, Scriptable start) {
            if (path == null || name.equals(path.getName())) return clazz;
            return super.get(cx, name, start);
        }
    }
}
