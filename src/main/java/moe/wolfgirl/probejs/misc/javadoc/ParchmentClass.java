package moe.wolfgirl.probejs.misc.javadoc;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import moe.wolfgirl.probejs.typescript.ClassPath;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record ParchmentClass(String name, List<String> javaDoc, List<Method> methods, List<Field> fields) {
    public ClassPath getClassPath() {
        String[] parts = name.split("/");
        parts[parts.length - 1] = "$" + parts[parts.length - 1];
        return new ClassPath(List.of(parts));
    }

    public Method getConstructor(String descriptor) {
        for (Method method : methods) {
            if (method.name().equals("<init>") && method.descriptor().equals(descriptor)) return method;
        }
        return null;
    }

    public Method getMethod(String descriptor) {
        for (Method method : methods) {
            if (method.descriptor().equals(descriptor)) return method;
        }
        return null;
    }

    public Field getField(String name) {
        for (Field field : fields) {
            if (field.descriptor().equals(name)) return field;
        }
        return null;
    }

    private static List<String> readJavaDoc(JsonObject jsonObject) {
        List<String> result = new ArrayList<>();
        if (jsonObject.has("javadoc")) {
            var javaDocElement = jsonObject.get("javadoc");
            if (javaDocElement.isJsonArray()) {
                for (JsonElement line : javaDocElement.getAsJsonArray()) {
                    result.add(line.getAsString());
                }
            } else if (javaDocElement.isJsonPrimitive()) {
                result.add(javaDocElement.getAsString());
            } else {
                throw new IllegalArgumentException("Invalid parchment format: javaDoc is not valid");
            }
        }
        return result;
    }

    private static List<JsonObject> readAsObjList(JsonElement element) {
        List<JsonObject> result = new ArrayList<>();
        if (element.isJsonArray()) {
            for (JsonElement entry : element.getAsJsonArray()) {
                if (!entry.isJsonObject()) {
                    throw new IllegalArgumentException("Invalid parchment format: entry is not an object");
                }
                result.add(entry.getAsJsonObject());
            }
        } else {
            throw new IllegalArgumentException("Invalid parchment format: expected an array");
        }
        return result;
    }

    public record Param(String name, int index, List<String> javaDoc) {
        public static Param fromJson(JsonObject jsonObject) {
            return new Param(
                    jsonObject.get("name").getAsString(),
                    jsonObject.get("index").getAsInt(),
                    readJavaDoc(jsonObject)
            );
        }
    }

    public record Method(String name, List<String> javaDoc, String descriptor, List<Param> params) {
        public static Method fromJson(JsonObject jsonObject) {
            var name = jsonObject.get("name").getAsString();
            var descriptor = jsonObject.get("descriptor").getAsString();
            var javaDoc = readJavaDoc(jsonObject);
            List<Param> params = new ArrayList<>();
            if (jsonObject.has("parameters")) {
                var paramsElement = jsonObject.get("parameters");
                for (JsonObject paramObj : readAsObjList(paramsElement)) {
                    params.add(Param.fromJson(paramObj));
                }
            }
            return new Method(name, javaDoc, descriptor, params);
        }
    }

    public record Field(String name, List<String> javaDoc, String descriptor) {
        public static Field fromJson(JsonObject jsonObject) {
            var name = jsonObject.get("name").getAsString();
            var descriptor = jsonObject.get("descriptor").getAsString();
            var javaDoc = readJavaDoc(jsonObject);
            return new Field(name, javaDoc, descriptor);
        }
    }

    private static Pair<Set<Method>, Set<Field>> getSuperMembers(ClassPath current, Map<ClassPath, ParchmentClass> classMap, Set<ClassPath> visited) {
        if (!visited.add(current)) {
            return Pair.of(new HashSet<>(), new HashSet<>());
        }

        Set<Method> methods = new HashSet<>();
        Set<Field> fields = new HashSet<>();

        // Collect parents: superclass + interfaces
        List<ClassPath> parents = new ArrayList<>();
        ClassPath superclass = current.superclass();
        if (superclass != null) parents.add(superclass);
        parents.addAll(current.interfaces());

        for (ClassPath parent : parents) {
            // Add members from the parent's parchment directly
            ParchmentClass parentParchment = classMap.get(parent);
            if (parentParchment != null) {
                methods.addAll(parentParchment.methods());
                fields.addAll(parentParchment.fields());
            }
            // Recursively collect from grandparents
            Pair<Set<Method>, Set<Field>> superMembers = getSuperMembers(parent, classMap, visited);
            methods.addAll(superMembers.getFirst());
            fields.addAll(superMembers.getSecond());
        }

        return Pair.of(methods, fields);
    }

    public static List<ParchmentClass> load(JsonObject jsonObject) {
        var classes = jsonObject.get("classes").getAsJsonArray();
        List<ParchmentClass> result = new ArrayList<>();
        for (JsonElement element : classes) {
            if (!element.isJsonObject()) {
                throw new IllegalArgumentException("Invalid parchment format: class entry is not an object");
            }
            var classObj = element.getAsJsonObject();
            var name = classObj.get("name").getAsString();
            var javaDoc = readJavaDoc(classObj);
            List<Method> methods = new ArrayList<>();
            List<Field> fields = new ArrayList<>();
            if (classObj.has("methods")) {
                var methodsElement = classObj.get("methods");
                for (JsonObject methodObj : readAsObjList(methodsElement)) {
                    methods.add(Method.fromJson(methodObj));
                }
            }
            if (classObj.has("fields")) {
                var fieldsElement = classObj.get("fields");
                for (JsonObject fieldObj : readAsObjList(fieldsElement)) {
                    fields.add(Field.fromJson(fieldObj));
                }
            }

            result.add(new ParchmentClass(name, javaDoc, methods, fields));
        }

        return result;
    }

    public static void mergeSuperMembers(Map<ClassPath, ParchmentClass> classMap) {
        // For each class, we also need to merge parchment from super classes and interfaces
        for (Map.Entry<ClassPath, ParchmentClass> entry : classMap.entrySet()) {
            var classPath = entry.getKey();
            var parchmentClass = entry.getValue();
            var visited = new HashSet<ClassPath>();
            var superMembers = getSuperMembers(classPath, classMap, visited);

            // Only add inherited methods/fields not already declared in this class
            Set<String> existingMethodDescriptors = parchmentClass.methods.stream()
                    .map(Method::descriptor).collect(Collectors.toSet());
            for (Method m : superMembers.getFirst()) {
                if (!existingMethodDescriptors.contains(m.descriptor())) {
                    parchmentClass.methods.add(m);
                    existingMethodDescriptors.add(m.descriptor());
                }
            }

            Set<String> existingFieldDescriptors = parchmentClass.fields.stream()
                    .map(Field::descriptor).collect(Collectors.toSet());
            for (Field f : superMembers.getSecond()) {
                if (!existingFieldDescriptors.contains(f.descriptor())) {
                    parchmentClass.fields.add(f);
                    existingFieldDescriptors.add(f.descriptor());
                }
            }
        }
    }
}
