package moe.wolfgirl.util.special_docs;

import moe.wolfgirl.ProbeJS;
import moe.wolfgirl.jdoc.document.DocumentClass;
import moe.wolfgirl.jdoc.document.DocumentMethod;
import moe.wolfgirl.jdoc.java.ClassInfo;
import moe.wolfgirl.jdoc.property.PropertyParam;
import moe.wolfgirl.jdoc.property.PropertyType;
import moe.wolfgirl.util.PlatformSpecial;
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
