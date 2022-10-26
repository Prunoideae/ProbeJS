package com.probejs.util.forge;

import com.probejs.ProbeJS;
import com.probejs.info.ClassInfo;
import com.probejs.info.MethodInfo;
import com.probejs.jdoc.document.DocumentClass;
import com.probejs.jdoc.document.DocumentMethod;
import com.probejs.jdoc.property.PropertyParam;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.plugin.CapturedClasses;
import dev.latvian.mods.kubejs.forge.ForgeEventConsumer;
import dev.latvian.mods.kubejs.forge.ForgeEventWrapper;
import dev.latvian.mods.kubejs.forge.GenericForgeEventConsumer;

import java.lang.reflect.Method;

public class ForgeEventDocument {
    public static DocumentClass loadForgeEventDocument() throws NoSuchMethodException {
        DocumentClass document = DocumentClass.fromJava(ClassInfo.getOrCache(ForgeEventWrapper.class));
        DocumentMethod onEvent = DocumentMethod.fromJava(new MethodInfo(ForgeEventWrapper.class.getMethod("onEvent", Object.class, ForgeEventConsumer.class), ForgeEventWrapper.class));
        DocumentMethod onGenericEvent = DocumentMethod.fromJava(new MethodInfo(ForgeEventWrapper.class.getMethod("onGenericEvent", Object.class, Object.class, GenericForgeEventConsumer.class), ForgeEventWrapper.class));

        for (Class<?> clazz : CapturedClasses.capturedRawEvents.values()) {
            ClassInfo info = ClassInfo.getOrCache(clazz);
            DocumentMethod method = (info.getParameters().isEmpty() ? onEvent : onGenericEvent).copy();
            method.params.set(0, new PropertyParam("eventClass", new PropertyType.Native(ProbeJS.GSON.toJson(info.getName())), false));
            method.returns = new PropertyType.Clazz(info.getName());
            document.methods.add(method);
        }
        return document;
    }
}
