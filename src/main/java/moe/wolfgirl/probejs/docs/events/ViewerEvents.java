package moe.wolfgirl.probejs.docs.events;

import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.recipe.viewer.*;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.transpiler.TypeConverter;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;
import moe.wolfgirl.probejs.lang.typescript.code.member.ClassDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.MethodDecl;
import moe.wolfgirl.probejs.lang.typescript.code.ts.MethodDeclaration;
import moe.wolfgirl.probejs.lang.typescript.code.ts.Statements;
import moe.wolfgirl.probejs.lang.typescript.code.ts.Wrapped;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;

import java.util.Map;
import java.util.function.Consumer;

public class ViewerEvents extends ProbeJSPlugin {
    @Override
    public void addGlobals(ScriptDump scriptDump) {
        Wrapped.Namespace events = new Wrapped.Namespace("RecipeViewerEvents");
        TypeConverter typeConverter = scriptDump.transpiler.typeConverter;
        for (Map.Entry<String, EventHandler> entry : RecipeViewerEvents.GROUP.getHandlers().entrySet()) {
            String key = entry.getKey();
            EventHandler handler = entry.getValue();
            if (handler.target == RecipeViewerEvents.TARGET && handler.scriptTypePredicate.test(scriptDump.scriptType)) {
                for (RecipeViewerEntryType recipeViewerEntryType : RecipeViewerEntryType.ALL_TYPES.get()) {
                    events.addCode(makeEvent(
                            key, recipeViewerEntryType.id,
                            handler.eventType.get(),
                            Types.ignoreContext(typeConverter.convertType(recipeViewerEntryType.entryType.type()), BaseType.FormatType.INPUT),
                            Types.ignoreContext(typeConverter.convertType(recipeViewerEntryType.predicateType.type()), BaseType.FormatType.INPUT)
                    ));
                }
            }
        }
        scriptDump.addGlobal("recipe_viewer_events", events);
    }

    private static MethodDeclaration makeEvent(String event, String identifier, Class<?> eventClass, BaseType entry, BaseType filter) {
        return Statements.method(event)
                .param("extra", Types.literal(identifier))
                .param("handler", Types.lambda()
                        .param("event", Types.parameterized(
                                Types.type(eventClass),
                                entry, filter
                        ))
                        .build())
                .build();
    }

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        applyEdits(globalClasses, AddEntriesKubeEvent.class, decl -> {
            addVariables(decl);
            replaceType(decl, "add", 0, "E");
        });
        applyEdits(globalClasses, RemoveEntriesKubeEvent.class, decl -> {
            addVariables(decl);
            replaceType(decl, "remove", 0, "F");
        });
        applyEdits(globalClasses, GroupEntriesKubeEvent.class, decl -> {
            addVariables(decl);
            replaceType(decl, "group", 0, "F");
        });
        applyEdits(globalClasses, AddInformationKubeEvent.class, decl -> {
            addVariables(decl);
            replaceType(decl, "add", 0, "F");
        });
        applyEdits(globalClasses, RegisterSubtypesKubeEvent.class, decl -> {
            addVariables(decl);
            replaceType(decl, "useComponents", 0, "F");
            replaceType(decl, "register", 0, "F");
        });
        applyEdits(globalClasses, RemoveRecipesKubeEvent.class, decl ->
                replaceType(decl, "remove", 0, Types.primitive("Special.RecipeId").asArray())
        );
    }

    private static void addVariables(ClassDecl decl) {
        decl.variableTypes.add(Types.generic("E")); // Entry type
        decl.variableTypes.add(Types.generic("F")); // Filter type
    }

    private static void replaceType(ClassDecl decl, String name, int index, String symbol) {
        for (MethodDecl method : decl.methods) {
            if (method.name.equals(name)) {
                method.params.get(index).type = Types.generic(symbol);
            }
        }
    }

    private static void replaceType(ClassDecl decl, String name, int index, BaseType type) {
        for (MethodDecl method : decl.methods) {
            if (method.name.equals(name)) {
                method.params.get(index).type = type;
            }
        }
    }

    private static void applyEdits(Map<ClassPath, TypeScriptFile> globalClasses, Class<?> clazz, Consumer<ClassDecl> edits) {
        TypeScriptFile typeScriptFile = globalClasses.get(new ClassPath(clazz));
        if (typeScriptFile == null) return;
        typeScriptFile.findCode(ClassDecl.class).ifPresent(edits);
    }
}
