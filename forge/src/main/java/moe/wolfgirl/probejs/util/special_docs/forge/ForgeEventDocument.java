package moe.wolfgirl.probejs.util.special_docs.forge;

import com.mojang.datafixers.util.Pair;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.docs.DocCompiler;
import moe.wolfgirl.probejs.jdoc.java.ClassInfo;
import moe.wolfgirl.probejs.jdoc.java.MethodInfo;
import moe.wolfgirl.probejs.jdoc.java.type.TypeInfoClass;
import moe.wolfgirl.probejs.jdoc.document.DocumentClass;
import moe.wolfgirl.probejs.jdoc.document.DocumentMethod;
import moe.wolfgirl.probejs.jdoc.property.PropertyComment;
import moe.wolfgirl.probejs.jdoc.property.PropertyParam;
import moe.wolfgirl.probejs.jdoc.property.PropertyType;
import dev.latvian.mods.kubejs.forge.ForgeEventConsumer;
import dev.latvian.mods.kubejs.forge.ForgeEventWrapper;
import dev.latvian.mods.kubejs.forge.GenericForgeEventConsumer;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;
import java.util.function.Consumer;

public class ForgeEventDocument {
    public static DocumentClass loadForgeEventDocument() throws NoSuchMethodException {
        DocumentClass document = DocumentClass.fromJava(ClassInfo.getOrCache(ForgeEventWrapper.class));
        DocumentMethod onEvent = DocumentMethod.fromJava(new MethodInfo(MethodInfo.getMethodInfo(ForgeEventWrapper.class.getMethod("onEvent", Object.class, ForgeEventConsumer.class), ForgeEventWrapper.class).get(), ForgeEventWrapper.class));
        DocumentMethod onGenericEvent = DocumentMethod.fromJava(new MethodInfo(MethodInfo.getMethodInfo(ForgeEventWrapper.class.getMethod("onGenericEvent", Object.class, Object.class, GenericForgeEventConsumer.class), ForgeEventWrapper.class).get(), ForgeEventWrapper.class));
        var consumer = new PropertyType.Clazz();
        consumer.fromJava(new TypeInfoClass(Consumer.class));
        for (Class<?> clazz : DocCompiler.CapturedClasses.capturedRawEvents.values()) {
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
            if (!info.getParameters().isEmpty()) {
                method.params.set(1, new PropertyParam("generic", new PropertyType.Native("Special.JavaClass"), false));
            }
            method.params.set(info.getParameters().isEmpty() ? 1 : 2, new PropertyParam("handler", new PropertyType.JSLambda(List.of(new Pair<>("event", eventType)), new PropertyType.Native("void")), false));
            document.methods.add(method);
        }
        return document;
    }
}
