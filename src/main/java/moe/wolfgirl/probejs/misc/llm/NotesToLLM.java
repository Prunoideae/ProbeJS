package moe.wolfgirl.probejs.misc.llm;

import moe.wolfgirl.probejs.typescript.ClassPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NotesToLLM {
    public static final Map<ClassPath, List<String>> NOTES = new HashMap<>();

    public static void addNote(ClassPath classPath, List<String> noteLines) {
        NOTES.computeIfAbsent(classPath, k -> new ArrayList<>()).addAll(noteLines);
    }

    public static void addNote(Class<?> clazz, List<String> noteLines) {
        addNote(new ClassPath(clazz), noteLines);
    }

    public static class Registry {
        public void register(ClassPath classPath, List<String> noteLines) {
            addNote(classPath, noteLines);
        }

        public void register(Class<?> clazz, List<String> noteLines) {
            addNote(clazz, noteLines);
        }
    }
}
