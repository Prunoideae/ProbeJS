package moe.wolfgirl.probejs.next.utils;


import dev.latvian.mods.kubejs.bindings.JavaWrapper;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.rhino.*;
import moe.wolfgirl.probejs.next.java.clazz.ClassPath;

import java.util.Arrays;

public class Require extends BaseFunction {
    private final JavaWrapper innerWrapper;

    public Require(ScriptManager manager) {
        this.innerWrapper = new JavaWrapper(manager);
    }

    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        String result = (String) Context.jsToJava(cx, args[0], String.class);
        String[] parts = result.split("/", 2);
        ClassPath path = new ClassPath(Arrays.stream(parts[1].split("/")).toList());

        var loaded = innerWrapper.tryLoadClass(path.getClassPath());
        return new RequireWrapper(path, loaded == null ? Undefined.instance : loaded);
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
            return path.getClassPath();
        }

        @Override
        public Object get(Context cx, String name, Scriptable start) {
            if (name.equals(path.getName())) return clazz;
            return super.get(cx, name, start);
        }
    }
}
