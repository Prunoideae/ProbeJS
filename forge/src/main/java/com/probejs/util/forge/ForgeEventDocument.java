package com.probejs.util.forge;

import com.probejs.ProbeJS;
import com.probejs.info.ClassInfo;
import com.probejs.info.MethodInfo;
import com.probejs.info.type.TypeInfoClass;
import com.probejs.jdoc.document.DocumentClass;
import com.probejs.jdoc.document.DocumentMethod;
import com.probejs.jdoc.property.PropertyComment;
import com.probejs.jdoc.property.PropertyParam;
import com.probejs.jdoc.property.PropertyType;
import com.probejs.plugin.CapturedClasses;
import dev.latvian.mods.kubejs.forge.ForgeEventConsumer;
import dev.latvian.mods.kubejs.forge.ForgeEventWrapper;
import dev.latvian.mods.kubejs.forge.GenericForgeEventConsumer;
import dev.latvian.mods.rhino.JavaMembers;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

public class ForgeEventDocument {
    public static DocumentClass loadForgeEventDocument() throws NoSuchMethodException {
        DocumentClass document = DocumentClass.fromJava(ClassInfo.getOrCache(ForgeEventWrapper.class));
        DocumentMethod onEvent = DocumentMethod.fromJava(new MethodInfo(MethodInfo.getMethodInfo(ForgeEventWrapper.class.getMethod("onEvent", Object.class, ForgeEventConsumer.class), ForgeEventWrapper.class).get(), ForgeEventWrapper.class));
        DocumentMethod onGenericEvent = DocumentMethod.fromJava(new MethodInfo(MethodInfo.getMethodInfo(ForgeEventWrapper.class.getMethod("onGenericEvent", Object.class, Object.class, GenericForgeEventConsumer.class), ForgeEventWrapper.class).get(), ForgeEventWrapper.class));
        var consumer = new PropertyType.Clazz();
        consumer.fromJava(new TypeInfoClass(Consumer.class));
        for (Class<?> clazz : CapturedClasses.capturedRawEvents.values()) {
            ClassInfo info = ClassInfo.getOrCache(clazz);
            DocumentMethod method = (info.getParameters().isEmpty() ? onEvent : onGenericEvent).copy();
            PropertyComment comment = new PropertyComment(
                    "This event is%s cancellable".formatted(clazz.isAnnotationPresent(Cancelable.class) ? "" : " **not**"),
                    "",
                    "This event does%s have a result".formatted(clazz.isAnnotationPresent(Event.HasResult.class) ? "" : " **not**")
            );
            method.addProperty(comment);
            method.params.set(0, new PropertyParam("eventClass", new PropertyType.Native(ProbeJS.GSON.toJson(info.getName())), false));
            var eventType = new PropertyType.Clazz();
            eventType.fromJava(new TypeInfoClass(clazz));
            method.params.set(1, new PropertyParam("handler", new PropertyType.Parameterized(consumer, List.of(eventType)), false));
            document.methods.add(method);
        }
        return document;
    }
}
