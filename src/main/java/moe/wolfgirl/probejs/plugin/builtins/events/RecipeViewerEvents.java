package moe.wolfgirl.probejs.plugin.builtins.events;

import com.mojang.datafixers.util.Pair;
import dev.latvian.mods.kubejs.recipe.viewer.*;
import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.plugin.builtins.alias.SpecialTypes;
import moe.wolfgirl.probejs.typescript.Documents;
import moe.wolfgirl.probejs.typescript.base.DocumentRegistrar;
import moe.wolfgirl.probejs.typescript.document.ClassDecl;
import moe.wolfgirl.probejs.typescript.document.Members;
import moe.wolfgirl.probejs.typescript.document.Types;
import moe.wolfgirl.probejs.typescript.document.base.KindAware;
import moe.wolfgirl.probejs.typescript.document.base.Type;
import moe.wolfgirl.probejs.typescript.document.builders.ClassBuilder;
import moe.wolfgirl.probejs.typescript.document.members.MethodDecl;
import moe.wolfgirl.probejs.typescript.document.members.ParamDecl;
import moe.wolfgirl.probejs.typescript.transpiler.TypeConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RecipeViewerEvents extends ProbeJSPlugin {
    public static final ClassPath CLIENT_RECIPE_EVENT = Events.CLIENT_EVENTS.append("RecipeViewerEvents");
    public static final ClassPath SERVER_RECIPE_EVENT = Events.SERVER_EVENTS.append("RecipeViewerEvents");

    static {
        Events.SKIP_EVENTS.addAll(List.of(
                Pair.of("RecipeViewerEvents", "addEntries"),
                Pair.of("RecipeViewerEvents", "addInformation"),
                Pair.of("RecipeViewerEvents", "groupEntries"),
                Pair.of("RecipeViewerEvents", "registerSubtypes"),
                Pair.of("RecipeViewerEvents", "removeCategories"),
                Pair.of("RecipeViewerEvents", "removeEntries"),
                Pair.of("RecipeViewerEvents", "removeEntriesCompletely"),
                Pair.of("RecipeViewerEvents", "removeRecipes")
        ));
    }

    @Override
    public void addSidedDocuments(DocumentRegistrar registrar) {
        ClassBuilder serverEvents = Members.clazz(SERVER_RECIPE_EVENT).kind(KindAware.Kind.NAMESPACE);
        ClassBuilder clientEvents = Members.clazz(CLIENT_RECIPE_EVENT).kind(KindAware.Kind.NAMESPACE);
        TypeConverter converter = new TypeConverter();

        for (MethodDecl event : generateEvents(converter)) {
            serverEvents.member(event);
            clientEvents.member(event);
        }

        registrar.addGlobal(SERVER_RECIPE_EVENT, serverEvents.build());
        registrar.addGlobal(CLIENT_RECIPE_EVENT, clientEvents.build());
    }

    public List<MethodDecl> generateEvents(TypeConverter converter) {
        List<MethodDecl> events = new ArrayList<>();

        for (RecipeViewerEntryType entry : RecipeViewerEntryType.ALL_TYPES.get()) {
            var entryType = converter.convertType(entry.entryType.type()).markAsInput();
            var predicateType = converter.convertType(entry.predicateType.type()).markAsInput();
            events.add(generateEvent("addEntries", entry.id, AddEntriesKubeEvent.class, entryType));
            events.add(generateEvent("addInformation", entry.id, AddInformationKubeEvent.class, predicateType));
            events.add(generateEvent("groupEntries", entry.id, GroupEntriesKubeEvent.class, predicateType));
            events.add(generateEvent("registerSubtypes", entry.id, RegisterSubtypesKubeEvent.class, predicateType));
            events.add(generateEvent("removeEntries", entry.id, RemoveEntriesKubeEvent.class, predicateType));
            events.add(generateEvent("removeEntriesCompletely", entry.id, RemoveEntriesKubeEvent.class, predicateType));

        }
        events.add(generateEventNoEntry("removeCategories", RemoveCategoriesKubeEvent.class));
        events.add(generateEventNoEntry("removeRecipes", RemoveRecipesKubeEvent.class));

        return events;
    }

    public MethodDecl generateEvent(String eventName, String entryType, Class<?> eventType, Type... params) {
        var e = params.length == 0 ? Types.clazz(eventType) : Types.clazz(eventType).withParams(params);

        return Members.method(eventName)
                .param("extra", Types.literal(entryType))
                .param("handler", Types.lambda(builder -> builder.param("event", e)))
                .build(KindAware.Kind.NAMESPACE);
    }

    public MethodDecl generateEventNoEntry(String eventName, Class<?> eventType, Type... params) {
        var e = params.length == 0 ? Types.clazz(eventType) : Types.clazz(eventType).withParams(params);
        return Members.method(eventName)
                .param("handler", Types.lambda(builder -> builder.param("event", e)))
                .build(KindAware.Kind.NAMESPACE);
    }

    @Override
    public void modifyClasses(Documents.ClassAccessor classDocuments) {
        findClass(classDocuments, AddEntriesKubeEvent.class, classDecl -> {
            addVariable(classDecl, "E");
            modifyParam(classDecl, "add", 0, Types.variable("E").asArray());
        });
        findClass(classDocuments, RemoveEntriesKubeEvent.class, classDecl -> {
            addVariable(classDecl, "F");
            modifyParam(classDecl, "remove", 0, Types.variable("F"));
        });
        findClass(classDocuments, GroupEntriesKubeEvent.class, classDecl -> {
            addVariable(classDecl, "F");
            modifyParam(classDecl, "group", 0, Types.variable("F"));
        });
        findClass(classDocuments, AddInformationKubeEvent.class, classDecl -> {
            addVariable(classDecl, "F");
            modifyParam(classDecl, "add", 0, Types.variable("F"));
        });
        findClass(classDocuments, RegisterSubtypesKubeEvent.class, classDecl -> {
            addVariable(classDecl, "F");
            modifyParam(classDecl, "register", 0, Types.variable("F"));
            modifyParam(classDecl, "useComponents", 0, Types.variable("F"));
        });
        findClass(classDocuments, RemoveRecipesKubeEvent.class, classDecl -> {
            modifyParam(classDecl, "remove", 0, SpecialTypes.RECIPE_ID.asArray());
        });
    }

    private static void addVariable(ClassDecl classDecl, String name) {
        classDecl.typeParams.add(Types.variable(name));
    }

    private static void modifyParam(ClassDecl classDecl, String methodName, int paramIndex, Type newType) {
        for (var member : classDecl.members) {
            if (member instanceof MethodDecl methodDecl && methodDecl.name.equals(methodName)) {
                var oldParam = methodDecl.params.get(paramIndex);
                methodDecl.params.set(paramIndex, new ParamDecl(oldParam.name, newType));
            }
        }
    }

    private static void findClass(Documents.ClassAccessor classDocuments, Class<?> clazz, Consumer<ClassDecl> callback) {
        if (classDocuments.getDocument(clazz) instanceof ClassDecl classDecl) {
            callback.accept(classDecl);
        }
    }
}
