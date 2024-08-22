package moe.wolfgirl.probejs.utils;

import dev.latvian.mods.rhino.BaseFunction;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Scriptable;

public class Require extends BaseFunction {
    @Override
    public Object call(Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
        throw new UnsupportedOperationException("require() is not supported! You should install the VSCode extension to get auto-imports for Java.loadClass().");
    }
}
