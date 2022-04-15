package com.probejs.plugin;

import com.probejs.ProbeJS;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.rhino.NativeJavaObject;

import java.util.Arrays;

public class ProbeJSPlugin extends KubeJSPlugin {
    @Override
    public void addBindings(BindingsEvent event) {
        event.addFunction("inspect", os -> {
            for (Object o : os) {
                if (o instanceof NativeJavaObject njo) {
                    ProbeJS.LOGGER.info(o);
                    ProbeJS.LOGGER.info(njo.getClassName());
                    Arrays.stream(njo.getIds()).map(Object::toString).forEach(ProbeJS.LOGGER::info);
                } else {
                    ProbeJS.LOGGER.info(o);
                }
            }
            return null;
        });
    }
}
