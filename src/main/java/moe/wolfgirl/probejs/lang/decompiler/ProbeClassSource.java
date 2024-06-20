package moe.wolfgirl.probejs.lang.decompiler;

import org.jetbrains.java.decompiler.main.extern.IContextSource;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ProbeClassSource implements IContextSource {

    private final Map<String, Class<?>> classes;

    public ProbeClassSource(Collection<Class<?>> classes) {
        this.classes = new HashMap<>();
        for (Class<?> clazz : classes) {
            this.classes.put(clazz.getName().replace(".", "/"), clazz);
        }
    }

    @Override
    public String getName() {
        return ProbeClassSource.class.getName();
    }

    @Override
    public Entries getEntries() {
        List<Entry> entries = classes.keySet().stream().map(Entry::atBase).toList();
        return new Entries(entries, List.of(), List.of(), List.of());
    }

    @Override
    public InputStream getInputStream(String resource) throws IOException {
        Class<?> clazz = classes.get(resource.substring(0, resource.length() - IContextSource.CLASS_SUFFIX.length()));
        return clazz.getClassLoader().getResourceAsStream(resource);
    }

    @Override
    public IOutputSink createOutputSink(IResultSaver saver) {
        return new IOutputSink() {
            @Override
            public void begin() {

            }

            @Override
            public void acceptClass(String qualifiedName, String fileName, String content, int[] mapping) {
                saver.saveClassEntry("probejs", "probejs", qualifiedName, fileName, content, mapping);
            }

            @Override
            public void acceptDirectory(String directory) {

            }

            @Override
            public void acceptOther(String path) {

            }

            @Override
            public void close() throws IOException {

            }
        };
    }
}
