package moe.wolfgirl.probejs.util.special_docs;

import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.jdoc.document.DocumentClass;
import moe.wolfgirl.probejs.jdoc.document.DocumentMethod;
import moe.wolfgirl.probejs.jdoc.java.ClassInfo;
import moe.wolfgirl.probejs.jdoc.property.PropertyParam;
import moe.wolfgirl.probejs.jdoc.property.PropertyType;
import moe.wolfgirl.probejs.util.PlatformSpecial;
import dev.latvian.mods.kubejs.bindings.JavaWrapper;
import dev.latvian.mods.kubejs.server.ServerScriptManager;

import java.util.List;

public class JavaWrapperDocument {
    public static DocumentClass loadJavaWrapperDocument(List<DocumentClass> globalClasses) throws NoSuchMethodException {
        //Document for Java.loadClass
        DocumentClass javaWrapper = DocumentClass.fromJava(ClassInfo.getOrCache(JavaWrapper.class));
        DocumentMethod loadClass = PlatformSpecial.getMethodDocument(JavaWrapper.class, "loadClass", String.class);
        for (DocumentClass globalClass : globalClasses) {
            if (ServerScriptManager.getScriptManager().isClassAllowed(globalClass.getName())) {
                DocumentMethod method = loadClass.copy();
                method.params.set(0, new PropertyParam("className", new PropertyType.Native(ProbeJS.GSON.toJson(globalClass.getName())), false));
                //Return interface directly since typeof Interface = any in Typescript
                method.returns = globalClass.isInterface() ?
                        new PropertyType.Clazz(globalClass.getName()) :
                        new PropertyType.TypeOf(new PropertyType.Clazz(globalClass.getName()));
                javaWrapper.methods.add(method);
            }
        }
        return javaWrapper;
    }
}
