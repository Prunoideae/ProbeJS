package moe.wolfgirl.probejs.docs.events;

import dev.latvian.mods.kubejs.recipe.RecipeFunction;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import dev.latvian.mods.kubejs.recipe.component.RecipeComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeNamespace;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaType;
import dev.latvian.mods.kubejs.recipe.schema.UnknownRecipeSchemaType;
import dev.latvian.mods.kubejs.recipe.schema.function.RecipeFunctionInstance;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import dev.latvian.mods.rhino.type.TypeInfo;
import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.lang.schema.ObjectElement;
import moe.wolfgirl.probejs.lang.schema.SchemaDump;
import moe.wolfgirl.probejs.lang.schema.SchemaElement;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.code.ImportInfo;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.lang.transpiler.TypeConverter;
import moe.wolfgirl.probejs.lang.transpiler.transformation.InjectBeans;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;
import moe.wolfgirl.probejs.lang.typescript.code.Code;
import moe.wolfgirl.probejs.lang.typescript.code.member.ClassDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.FieldDecl;
import moe.wolfgirl.probejs.lang.typescript.code.ts.Statements;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import moe.wolfgirl.probejs.lang.typescript.code.type.js.JSLambdaType;
import moe.wolfgirl.probejs.utils.GameUtils;
import moe.wolfgirl.probejs.utils.NameUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class RecipeEvents extends ProbeJSPlugin {
    public static final Map<String, String> SHORTCUTS = new HashMap<>();
    public static final ClassPath DOCUMENTED_RECIPES = new ClassPath("moe.wolfgirl.probejs.generated.DocumentedRecipes");

    static {
        SHORTCUTS.put("shaped", "kubejs:shaped");
        SHORTCUTS.put("shapeless", "kubejs:shapeless");
        SHORTCUTS.put("smelting", "minecraft:smelting");
        SHORTCUTS.put("blasting", "minecraft:blasting");
        SHORTCUTS.put("smoking", "minecraft:smoking");
        SHORTCUTS.put("campfireCooking", "minecraft:campfire_cooking");
        SHORTCUTS.put("stonecutting", "minecraft:stonecutting");
        SHORTCUTS.put("smithing", "minecraft:smithing_transform");
        SHORTCUTS.put("smithingTrim", "minecraft:smithing_trim");

    }

    @Override
    public void modifyClasses(ScriptDump scriptDump, Map<ClassPath, TypeScriptFile> globalClasses) {
        ProbeJS.LOGGER.info("Patching recipe events...");
        TypeConverter converter = scriptDump.transpiler.typeConverter;
        ServerScriptManager manager = GameUtils.getServerScriptManager();
        if (manager == null) return;

        // Generate recipe schema classes
        // Also generate the documented recipe class containing all stuffs from everywhere
        ClassDecl.Builder documentedRecipes = Statements.clazz(DOCUMENTED_RECIPES.getName());

        for (Map.Entry<String, RecipeNamespace> entry : manager.recipeSchemaStorage.namespaces.entrySet()) {
            String namespaceId = entry.getKey();
            RecipeNamespace namespace = entry.getValue();

            var builder = Types.object();
            for (Map.Entry<String, RecipeSchemaType> e : namespace.entrySet()) {
                String schemaId = e.getKey();
                RecipeSchemaType schemaType = e.getValue();
                if (schemaType instanceof UnknownRecipeSchemaType) continue;
                RecipeSchema schema = schemaType.schema;
                if (schema.isHidden()) continue;
                if (!BuiltInRegistries.RECIPE_SERIALIZER.containsKey(
                        ResourceLocation.fromNamespaceAndPath(namespaceId, schemaId))
                ) continue;

                ClassPath schemaPath = getSchemaClassPath(namespaceId, schemaId);
                ClassDecl schemaDecl = generateSchemaClass(schemaId, schema, converter);
                TypeScriptFile schemaFile = new TypeScriptFile(schemaPath);
                schemaFile.addCode(schemaDecl);
                globalClasses.put(schemaPath, schemaFile);

                JSLambdaType recipeFunction = generateSchemaFunction(schemaPath, schema, converter);
                builder.member(schemaId, recipeFunction);
            }

            documentedRecipes.field(namespaceId, builder.build());
        }
        TypeScriptFile documentFile = new TypeScriptFile(DOCUMENTED_RECIPES);
        documentFile.addCode(documentedRecipes.build());
        globalClasses.put(DOCUMENTED_RECIPES, documentFile);

        // Inject types into the RecipeEventJS
        TypeScriptFile recipeEventFile = globalClasses.get(new ClassPath(RecipesKubeEvent.class));
        ClassDecl recipeEvent = recipeEventFile.findCode(ClassDecl.class).orElse(null);
        if (recipeEvent == null) return; // What???
        recipeEvent.methods.stream()
                .filter(m -> m.name.equals("getRecipes"))
                .findFirst()
                .ifPresent(methodDecl -> methodDecl.returnType = Types.type(DOCUMENTED_RECIPES));
        for (Code code : recipeEvent.bodyCode) {
            if (code instanceof InjectBeans.BeanDecl beanDecl && beanDecl.name.equals("recipes")) {
                beanDecl.baseType = Types.type(DOCUMENTED_RECIPES);
            }
        }
        recipeEventFile.declaration.addClass(ImportInfo.original(DOCUMENTED_RECIPES));

        // Make shortcuts valid recipe functions
        for (FieldDecl field : recipeEvent.fields) {
            if (!SHORTCUTS.containsKey(field.name)) continue;
            String[] parts = SHORTCUTS.get(field.name).split(":", 2);
            RecipeSchema shortcutSchema = manager.recipeSchemaStorage.namespaces.get(parts[0]).get(parts[1]).schema;
            ClassPath returnType = getSchemaClassPath(parts[0], parts[1]);
            field.type = generateSchemaFunction(returnType, shortcutSchema, converter);

            for (ImportInfo usedClassPath : field.type.getUsedImports()) {
                recipeEventFile.declaration.addClass(usedClassPath);
            }
        }

    }


    private static ClassPath getSchemaClassPath(String namespace, String id) {
        return new ClassPath("moe.wolfgirl.probejs.generated.schema.%s.%s".formatted(
                namespace, NameUtils.rlToTitle(id)
        ));
    }

    /**
     * export class RecipeId {
     * foo(foo: FooType): this
     * bar(bar: BarType): this
     * }
     */
    private static ClassDecl generateSchemaClass(String id, RecipeSchema schema, TypeConverter converter) {
        ClassDecl.Builder builder = Statements.clazz("$" + NameUtils.rlToTitle(id))
                .superClass(converter.convertType(schema.recipeFactory.recipeType()));
        for (RecipeKey<?> key : schema.keys) {
            var name = key.getPrimaryFunctionName();
            if (!RecipeFunction.isValidIdentifier(name.toCharArray())) continue;
            builder.method(name, method -> {
                        method.returnType(Types.THIS);
                        method.param(name, converter.convertType(key.component.typeInfo()));
                    }
            );
        }

        for (RecipeFunctionInstance value : schema.functions.values()) {
            builder.method(value.name(), method -> {
                var args = value.function().arguments();
                for (int i = 0; i < args.size(); i++) {
                    var argument = args.get(i);
                    method.param("arg" + i, converter.convertType(argument.typeInfo()));
                }
                method.returnType(Types.THIS);
            });
        }
        return builder.build();
    }

    private static JSLambdaType generateSchemaFunction(ClassPath returnType, RecipeSchema schema, TypeConverter converter) {
        JSLambdaType.Builder builder = Types.lambda()
                .method()
                .returnType(Types.type(returnType));

        for (RecipeKey<?> key : schema.keys) {
            if (key.excluded) continue;
            if (key.functionNames == null || !key.functionNames.isEmpty()) {
                builder.param(key.getPrimaryFunctionName(),
                        converter.convertType(key.component.typeInfo()),
                        key.optional(), false);
            }
        }

        return builder.build();
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        Set<Class<?>> classes = new HashSet<>();
        ServerScriptManager manager = GameUtils.getServerScriptManager();
        if (manager == null) return Set.of();

        for (RecipeNamespace namespace : manager.recipeSchemaStorage.namespaces.values()) {
            for (RecipeSchemaType schemaType : namespace.values()) {
                for (RecipeKey<?> key : schemaType.schema.keys) {
                    classes.addAll(key.component.typeInfo().getContainedComponentClasses());
                }
                classes.addAll(schemaType.schema.recipeFactory.recipeType().getContainedComponentClasses());
            }
        }
        return classes;
    }

    @Override
    public void addJsonSchema(SchemaDump dump) {
        ServerScriptManager scriptManager = GameUtils.getServerScriptManager();
        if (scriptManager == null) return;
        // TODO: Implement new logic (if lat really did something
    }
}
