package moe.wolfgirl.probejs.docs.events;

import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;
import dev.latvian.mods.kubejs.recipe.schema.JsonRecipeSchemaType;
import dev.latvian.mods.kubejs.recipe.schema.RecipeNamespace;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaType;
import dev.latvian.mods.kubejs.recipe.schema.minecraft.SpecialRecipeSchema;
import dev.latvian.mods.kubejs.script.ScriptType;
import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.lang.transpiler.TypeConverter;
import moe.wolfgirl.probejs.lang.transpiler.transformation.InjectBeans;
import moe.wolfgirl.probejs.lang.typescript.TypeScriptFile;
import moe.wolfgirl.probejs.lang.typescript.code.Code;
import moe.wolfgirl.probejs.lang.typescript.code.member.ClassDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.FieldDecl;
import moe.wolfgirl.probejs.lang.typescript.code.ts.Statements;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import moe.wolfgirl.probejs.lang.typescript.code.type.js.JSLambdaType;
import moe.wolfgirl.probejs.lang.typescript.code.type.js.JSObjectType;
import moe.wolfgirl.probejs.utils.NameUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RecipeEvents extends ProbeJSPlugin {
    public static final Map<String, String> SHORTCUTS = new HashMap<>();
    public static final ClassPath DOCUMENTED_RECIPES = new ClassPath("moe.wolfgirl.probejs.generated.DocumentedRecipes");

    {
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
        if (scriptDump.scriptType != ScriptType.SERVER) return;
        TypeConverter converter = scriptDump.transpiler.typeConverter;

        // Generate recipe schema classes
        // Also generate the documented recipe class containing all stuffs from everywhere
        ClassDecl.Builder documentedRecipes = Statements.clazz(DOCUMENTED_RECIPES.getName());

        for (Map.Entry<String, RecipeNamespace> entry : RecipeNamespace.getAll().entrySet()) {
            String namespaceId = entry.getKey();
            RecipeNamespace namespace = entry.getValue();

            Map<String, BaseType> namespaceMembers = new HashMap<>();

            for (Map.Entry<String, RecipeSchemaType> e : namespace.entrySet()) {
                String schemaId = e.getKey();
                RecipeSchemaType schemaType = e.getValue();
                if (schemaType instanceof JsonRecipeSchemaType) continue;
                RecipeSchema schema = schemaType.schema;
                if (schema == SpecialRecipeSchema.SCHEMA) continue;

                ClassPath schemaPath = getSchemaClassPath(namespaceId, schemaId);
                ClassDecl schemaDecl = generateSchemaClass(schemaId, schema, converter);
                TypeScriptFile schemaFile = new TypeScriptFile(schemaPath);
                schemaFile.addCode(schemaDecl);
                globalClasses.put(schemaPath, schemaFile);

                JSLambdaType recipeFunction = generateSchemaFunction(schemaPath, schema, converter);
                namespaceMembers.put(schemaId, recipeFunction);
            }

            documentedRecipes.field(namespaceId, new JSObjectType(namespaceMembers));
        }
        TypeScriptFile documentFile = new TypeScriptFile(DOCUMENTED_RECIPES);
        documentFile.addCode(documentedRecipes.build());
        globalClasses.put(DOCUMENTED_RECIPES, documentFile);

        // Inject types into the RecipeEventJS
        TypeScriptFile recipeEventFile = globalClasses.get(new ClassPath(RecipesEventJS.class));
        ClassDecl recipeEvent = recipeEventFile.findCode(ClassDecl.class).orElse(null);
        if (recipeEvent == null) return; // What???
        recipeEvent.methods.stream()
                .filter(m -> m.params.isEmpty() && m.name.equals("getRecipes"))
                .findFirst()
                .ifPresent(methodDecl -> methodDecl.returnType = Types.type(DOCUMENTED_RECIPES));
        for (Code code : recipeEvent.bodyCode) {
            if (code instanceof InjectBeans.BeanDecl beanDecl && beanDecl.name.equals("recipes")) {
                beanDecl.baseType = Types.type(DOCUMENTED_RECIPES);
            }
        }
        recipeEventFile.declaration.addClass(DOCUMENTED_RECIPES);

        // Make shortcuts valid recipe functions
        for (FieldDecl field : recipeEvent.fields) {
            if (!SHORTCUTS.containsKey(field.name)) continue;
            String[] parts = SHORTCUTS.get(field.name).split(":", 2);
            RecipeSchema shortcutSchema = RecipeNamespace.getAll().get(parts[0]).get(parts[1]).schema;
            ClassPath returnType = getSchemaClassPath(parts[0], parts[1]);
            field.type = generateSchemaFunction(returnType, shortcutSchema, converter);

            for (ClassPath usedClassPath : field.type.getUsedClassPaths()) {
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
        ClassDecl.Builder builder = Statements.clazz(NameUtils.rlToTitle(id))
                .superClass(Types.type(schema.recipeType));
        for (RecipeKey<?> key : schema.keys) {
            if (key.noBuilders) continue;
            builder.method(key.preferred, method -> {
                        method.returnType(Types.THIS);
                        var baseType = converter.convertType(key.component.constructorDescription(TypeConverter.PROBEJS));
                        if (!baseType.equals(Types.BOOLEAN)) method.param(key.preferred, baseType);
                    }
            );
        }
        return builder.build();
    }

    private static JSLambdaType generateSchemaFunction(ClassPath returnType, RecipeSchema schema, TypeConverter converter) {
        JSLambdaType.Builder builder = Types.lambda()
                .method()
                .returnType(Types.type(returnType));

        for (RecipeKey<?> key : schema.keys) {
            if (key.excluded) continue;
            builder.param(key.preferred,
                    converter.convertType(key.component.constructorDescription(TypeConverter.PROBEJS)),
                    key.optional(), false);
        }

        return builder.build();
    }

    @Override
    public Set<Class<?>> provideJavaClass(ScriptDump scriptDump) {
        Set<Class<?>> classes = new HashSet<>();
        for (RecipeNamespace namespace : RecipeNamespace.getAll().values()) {
            for (RecipeSchemaType schemaType : namespace.values()) {
                classes.add(schemaType.schema.recipeType);
            }
        }
        return classes;
    }
}
