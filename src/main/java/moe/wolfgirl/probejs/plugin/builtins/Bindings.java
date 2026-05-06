package moe.wolfgirl.probejs.plugin.builtins;

import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.event.EventGroupWrapper;
import dev.latvian.mods.kubejs.script.KubeJSContext;
import dev.latvian.mods.kubejs.script.ScriptManager;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import dev.latvian.mods.rhino.BaseFunction;
import dev.latvian.mods.rhino.NativeJavaClass;
import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.utils.GameUtils;
import moe.wolfgirl.probejs.ClassPath;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.typescript.base.DocumentRegistrar;
import moe.wolfgirl.probejs.typescript.document.Types;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.KindAware;
import moe.wolfgirl.probejs.typescript.document.members.FieldDecl;
import moe.wolfgirl.probejs.typescript.document.members.MethodDecl;
import moe.wolfgirl.probejs.typescript.transpiler.TypeConverter;

import java.lang.reflect.Modifier;
import java.util.*;

public class Bindings extends ProbeJSPlugin {
    public static final ClassPath STARTUP_BINDINGS = ClassPath.sided(ScriptType.STARTUP, "bindings.GlobalBindings");
    public static final ClassPath SERVER_BINDINGS = ClassPath.sided(ScriptType.SERVER, "bindings.GlobalBindings");
    public static final ClassPath CLIENT_BINDINGS = ClassPath.sided(ScriptType.CLIENT, "bindings.GlobalBindings");

    @Override
    public void addSidedDocuments(DocumentRegistrar registrar) {
        TypeConverter converter = new TypeConverter();
        registrar.addGlobal(STARTUP_BINDINGS, dumpBindings(STARTUP_BINDINGS, KubeJS.getStartupScriptManager(), converter));
        registrar.addGlobal(CLIENT_BINDINGS, dumpBindings(CLIENT_BINDINGS, KubeJS.getClientScriptManager(), converter));
        ServerScriptManager manager = GameUtils.getServerScriptManager();
        if (manager != null) {
            registrar.addGlobal(SERVER_BINDINGS, dumpBindings(SERVER_BINDINGS, manager, converter));
        }
    }

    private static Combined dumpBindings(ClassPath classPath, ScriptManager scriptManager, TypeConverter converter) {
        KubeJSContext context = (KubeJSContext) scriptManager.contextFactory.enter();
        Scriptable scope = context.topLevelScope;
        ClassPath parent = classPath.getPackage();

        List<Code> exports = new ArrayList<>();
        for (Object o : scope.getIds(context)) {
            if (o instanceof String id) {
                Object value = scope.get(context, id, scope);
                if (value instanceof NativeJavaClass njc) value = njc.getClassObject();
                else value = context.jsToJava(value, TypeInfo.OBJECT);

                if (id.equals("global")) {
                    FieldDecl field = new FieldDecl(id, Types.raw("{}"), true);
                    field.setKind(KindAware.Kind.NAMESPACE);
                    exports.add(field);
                } else if (value.getClass() == Class.class) {
                    FieldDecl field = new FieldDecl(id, Types.typeOf(converter.convertType(TypeInfo.of(findPublicParent((Class<?>) value)))), true);
                    field.setKind(KindAware.Kind.NAMESPACE);
                    exports.add(field);
                } else if (!(value instanceof BaseFunction || value instanceof EventGroupWrapper)) {
                    FieldDecl field = new FieldDecl(id, converter.convertType(TypeInfo.of(findPublicParent(value.getClass()))), true);
                    field.setKind(KindAware.Kind.NAMESPACE);
                    exports.add(field);
                }
            }
        }

        return new Combined(exports, parent);
    }

    @Override
    public Set<Class<?>> provideClassForDiscovery() {
        Set<Class<?>> classes = new HashSet<>();
        classes.addAll(findClassesInScope(KubeJS.getStartupScriptManager()));
        classes.addAll(findClassesInScope(KubeJS.getClientScriptManager()));
        ServerScriptManager manager = GameUtils.getServerScriptManager();
        if (manager != null) {
            classes.addAll(findClassesInScope(manager));
        }
        return classes;
    }

    private static Set<Class<?>> findClassesInScope(ScriptManager scriptManager) {
        KubeJSContext context = (KubeJSContext) scriptManager.contextFactory.enter();
        Scriptable scope = context.topLevelScope;

        Set<Class<?>> classes = new HashSet<>();
        for (Object o : scope.getIds(context)) {
            if (o instanceof String id) {
                Object value = scope.get(context, id, scope);
                if (value instanceof NativeJavaClass javaClass) {
                    value = javaClass.getClassObject();
                } else if (value.getClass() != Class.class) {
                    value = context.jsToJava(value, TypeInfo.OBJECT);
                }

                if (value.getClass() == Class.class) {
                    classes.add((Class<?>) value);
                } else if (!(value instanceof BaseFunction || value instanceof EventGroupWrapper)) {
                    // No base function as don't know how to get type info
                    // No events because they will be dumped separately
                    classes.add(value.getClass());
                }
            }
        }

        return classes;
    }

    private static Class<?> findPublicParent(Class<?> clazz) {
        while (!Modifier.isPublic(clazz.getModifiers())) {
            clazz = clazz.getSuperclass();
        }
        return clazz;
    }

    public static class Combined extends Code {
        private final List<Code> codes;
        private final ClassPath basePath;

        public Combined(List<Code> codes, ClassPath basePath) {
            this.codes = codes;
            this.basePath = basePath;
        }

        @Override
        public Set<ClassPath> getImports() {
            Set<ClassPath> imports = new HashSet<>();
            for (Code code : codes) {
                imports.addAll(code.getImports());
                if (code instanceof FieldDecl field) {
                    imports.add(basePath.append(field.name));
                } else if (code instanceof MethodDecl method) {
                    imports.add(basePath.append(method.name));
                }
            }
            return imports;
        }

        @Override
        public List<String> format(int indent) {
            List<String> lines = new ArrayList<>();
            for (Code code : codes) {
                lines.addAll(code.format(indent));
            }
            return lines;
        }

        @Override
        public void setResolvedSymbols(Map<ClassPath, String> resolvedSymbols) {
            super.setResolvedSymbols(resolvedSymbols);
            for (Code code : codes) {
                code.setResolvedSymbols(resolvedSymbols);
            }
        }
    }
}
