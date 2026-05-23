package moe.wolfgirl.probejs.plugin.builtins.extras;

import dev.latvian.mods.kubejs.plugin.builtin.wrapper.NativeEventWrapper;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.typescript.Documents;
import moe.wolfgirl.probejs.typescript.document.ClassDecl;
import moe.wolfgirl.probejs.typescript.document.Types;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.members.MethodDecl;
import moe.wolfgirl.probejs.typescript.document.members.ParamDecl;
import net.neoforged.bus.api.Event;

public class ForgeEvents extends ProbeJSPlugin {

    @Override
    public void modifyClasses(Documents.ClassAccessor classDocuments) {
        if (classDocuments.getDocument(NativeEventWrapper.class) instanceof ClassDecl classDecl) {
            for (Code member : classDecl.members) {
                if (member instanceof MethodDecl methodDecl && methodDecl.name.equals("onEvent")) {
                    methodDecl.typeParams.add(Types.variable("E", Types.clazz(Event.class)));
                    int params = methodDecl.params.size();
                    methodDecl.params.set(params - 1, new ParamDecl("callback", Types.raw("(event: E) => void")));
                    methodDecl.params.set(params - 2, new ParamDecl("eventClass", Types.raw("new (...args: any[]) => E")));

                }
            }
        }
    }

    @Override
    public boolean allowClassInDiscovery(Class<?> clazz) {
        return Event.class.isAssignableFrom(clazz);
    }
}
