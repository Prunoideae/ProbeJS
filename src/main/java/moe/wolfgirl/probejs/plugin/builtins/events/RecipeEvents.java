package moe.wolfgirl.probejs.plugin.builtins.events;

import dev.latvian.mods.kubejs.recipe.RecipeFunction;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeNamespace;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaType;
import dev.latvian.mods.kubejs.recipe.schema.UnknownRecipeSchemaType;
import dev.latvian.mods.kubejs.recipe.schema.function.RecipeFunctionInstance;
import dev.latvian.mods.kubejs.server.ServerScriptManager;
import moe.wolfgirl.probejs.utils.GameUtils;
import moe.wolfgirl.probejs.utils.NameUtils;
import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.typescript.Documents;
import moe.wolfgirl.probejs.typescript.base.DocumentRegistrar;
import moe.wolfgirl.probejs.typescript.document.ClassDecl;
import moe.wolfgirl.probejs.typescript.document.Members;
import moe.wolfgirl.probejs.typescript.document.Types;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.KindAware;
import moe.wolfgirl.probejs.typescript.document.builders.ClassBuilder;
import moe.wolfgirl.probejs.typescript.document.members.FieldDecl;
import moe.wolfgirl.probejs.typescript.document.members.MethodDecl;
import moe.wolfgirl.probejs.typescript.transpiler.TypeConverter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class RecipeEvents extends ProbeJSPlugin {
    public static final Map<String, String> SHORTCUTS = new HashMap<>();
    public static final ClassPath RECIPES_BASE = Events.SERVER_EVENTS.append("recipes");
    public static final ClassPath RECIPES = RECIPES_BASE.append("DocumentedRecipes");

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

    public static ClassPath recipeClass(String namespace, String id) {
        return RECIPES_BASE.append("%s$%s".formatted(NameUtils.rlToTitle(namespace), NameUtils.rlToTitle(id)));
    }


    @Override
    public void addSidedDocuments(DocumentRegistrar registrar) {
        TypeConverter converter = new TypeConverter();
        ServerScriptManager manager = GameUtils.getServerScriptManager();
        if (manager == null) return;
        var documentedRecipes = Members.clazz(RECIPES).kind(KindAware.Kind.CLASS);

        for (Map.Entry<String, RecipeNamespace> entry : manager.recipeSchemaStorage.namespaces.entrySet()) {
            var namespaceId = entry.getKey();
            var recipeNamespace = entry.getValue();

            List<MethodDecl> methods = new ArrayList<>();
            for (Map.Entry<String, RecipeSchemaType> e : recipeNamespace.entrySet()) {
                String schemaId = e.getKey();
                RecipeSchemaType schemaType = e.getValue();
                if (schemaType instanceof UnknownRecipeSchemaType) continue;
                RecipeSchema schema = schemaType.schema;
                if (schema.isHidden()) continue;
                if (!BuiltInRegistries.RECIPE_SERIALIZER.containsKey(ResourceLocation.fromNamespaceAndPath(namespaceId, schemaId))) {
                    continue;
                }

                var recipeClass = generateRecipeClass(namespaceId, schemaId, schema, converter);
                registrar.addDocument(recipeClass(namespaceId, schemaId), recipeClass);
                methods.add(generateRecipeMethod(namespaceId, schemaId, schema, converter));
            }

            if (!methods.isEmpty()) documentedRecipes.member(new MappedDecl(namespaceId, methods));
        }

        registrar.addDocument(RECIPES, documentedRecipes.build());
    }

    @Override
    public void transformClass(Documents.ClassDocument document) {
        var classInfo = document.classInfo();
        var classDocument = document.document();
        if (!classInfo.clazz().equals(RecipesKubeEvent.class)) return;
        ServerScriptManager manager = GameUtils.getServerScriptManager();
        if (manager == null) return;

        // Make getRecipes return the documented recipes class
        classDocument.members.stream().filter(m -> m instanceof MethodDecl)
                .map(m -> (MethodDecl) m)
                .filter(m -> m.name.equals("getRecipes"))
                .findFirst()
                .ifPresent(m -> m.returnType = Types.clazz(RECIPES));

        // Add shortcuts
        List<Code> membersToRemove = new ArrayList<>();
        List<Code> newMembers = new ArrayList<>();
        for (Code member : classDocument.members) {
            if (member instanceof FieldDecl fieldDecl && SHORTCUTS.containsKey(fieldDecl.name)) {
                membersToRemove.add(member);
                String recipeId = SHORTCUTS.get(fieldDecl.name);
                String[] parts = recipeId.split(":");
                if (parts.length != 2) continue;
                String namespace = parts[0];
                String id = parts[1];
                RecipeSchema schema = manager.recipeSchemaStorage.namespaces.get(namespace).get(id).schema;
                newMembers.add(generateRecipeMethod(namespace, id, schema, document.converter()));
            }
        }

        classDocument.members.removeAll(membersToRemove);
        classDocument.members.addAll(newMembers);
    }

    private MethodDecl generateRecipeMethod(String namespace, String recipeName, RecipeSchema schema, TypeConverter converter) {
        var builder = Members.method(recipeName)
                .returnType(Types.clazz(recipeClass(namespace, recipeName)));
        for (RecipeKey<?> key : schema.keys) {
            if (key.excluded) continue;
            if (key.functionNames == null || !key.functionNames.isEmpty()) {
                builder.param(
                        key.getPrimaryFunctionName(),
                        converter.convertType(key.component.typeInfo()).markAsInput(),
                        false,
                        key.optional()
                );
            }
        }
        return builder.build();
    }

    private ClassDecl generateRecipeClass(String namespace, String id, RecipeSchema schema, TypeConverter converter) {
        ClassPath classPath = recipeClass(namespace, id);
        ClassBuilder builder = Members.clazz(classPath).kind(KindAware.Kind.CLASS).extendsType(converter.convertType(schema.recipeFactory.recipeType()));

        // Setters that autogenerated from recipe keys
        for (RecipeKey<?> key : schema.keys) {
            var name = key.getPrimaryFunctionName();
            if (!RecipeFunction.isValidIdentifier(name.toCharArray())) continue;
            builder.method(name, mb -> {
                mb.returnType(Types.THIS);
                mb.param(name, converter.convertType(key.component.typeInfo()).markAsInput());
            });
        }

        // Recipe functions
        for (RecipeFunctionInstance value : schema.functions.values()) {
            builder.method(value.name(), mb -> {
                var args = value.function().arguments();
                for (int i = 0; i < args.size(); i++) {
                    var argument = args.get(i);
                    mb.param("arg" + i, converter.convertType(argument.typeInfo()).markAsInput());
                }
                mb.returnType(Types.THIS);
            });
        }

        return builder.build();
    }

    // Represents an "object type" that has several methods inside
    // class DocumentedRecipes {
    //     minecraft: {
    //         shaped(...): ShapedRecipe;
    //         smelting(...): SmeltingRecipe;
    //     }
    //     modded: {
    //         ...
    //     }
    // }
    private static class MappedDecl extends Code {
        private final List<MethodDecl> methods;
        private final String identifier;

        private MappedDecl(String identifier, List<MethodDecl> methods) {
            this.identifier = identifier;
            this.methods = methods;
        }

        @Override
        public Set<ClassPath> getImports() {
            Set<ClassPath> imports = new HashSet<>();
            for (MethodDecl method : methods) {
                imports.addAll(method.getImports());
            }
            return imports;
        }

        @Override
        public List<String> format(int indent) {
            List<String> lines = new ArrayList<>();
            lines.add(" ".repeat(indent) + "%s: {".formatted(identifier));
            for (MethodDecl method : methods) {
                lines.addAll(method.format(indent + 4));
            }
            lines.add(" ".repeat(indent) + "}");
            return lines;
        }
    }
}
