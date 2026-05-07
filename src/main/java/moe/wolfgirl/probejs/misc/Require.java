package moe.wolfgirl.probejs.misc;

import dev.latvian.mods.kubejs.plugin.builtin.wrapper.JavaWrapper;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.rhino.BaseFunction;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.util.DefaultValueTypeHint;
import moe.wolfgirl.probejs.typescript.ClassPath;

import java.util.List;

public class Require implements Scriptable {
    public static boolean usedRequire = false;
    public static boolean usageReported = false;
    private final ClassPath basePath;

    public Require(String tsPath) {
        var segments = List.of(tsPath.split("/"));
        if (segments.size() < 2) {
            throw new IllegalArgumentException("Invalid TS path: " + tsPath);
        }
        var baseName = segments.getFirst();
        if (!baseName.equals("@package")) throw new IllegalArgumentException("Can not load non-java class: " + tsPath);
        var classSegments = segments.subList(1, segments.size());
        this.basePath = new ClassPath(classSegments);
    }

    public static class Wrapper extends BaseFunction {
        @Override
        public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
            return new Require(cx.toString(args[0]));
        }
    }


    @Override
    public String getClassName() {
        return getClass().getName();
    }

    @Override
    public Object get(Context cx, String name, Scriptable start) {
        usedRequire = true;
        return JavaWrapper.loadClass((KubeJSContext) cx, basePath.append(name).asJavaPath());
    }

    @Override
    public Object get(Context cx, int index, Scriptable start) {
        return Scriptable.NOT_FOUND;
    }

    @Override
    public boolean has(Context cx, String name, Scriptable start) {
        try {
            basePath.append(name).loadClass();
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean has(Context cx, int index, Scriptable start) {
        return false;
    }

    @Override
    public void put(Context cx, String name, Scriptable start, Object value) {

    }

    @Override
    public void put(Context cx, int index, Scriptable start, Object value) {

    }

    @Override
    public void delete(Context cx, String name) {

    }

    @Override
    public void delete(Context cx, int index) {

    }

    @Override
    public Scriptable getPrototype(Context cx) {
        return null;
    }

    @Override
    public void setPrototype(Scriptable prototype) {

    }

    @Override
    public Scriptable getParentScope() {
        return null;
    }

    @Override
    public void setParentScope(Scriptable parent) {

    }

    @Override
    public Object[] getIds(Context cx) {
        return new Object[0];
    }

    @Override
    public Object getDefaultValue(Context cx, DefaultValueTypeHint hint) {
        return null;
    }

    @Override
    public boolean hasInstance(Context cx, Scriptable instance) {
        return false;
    }
}
