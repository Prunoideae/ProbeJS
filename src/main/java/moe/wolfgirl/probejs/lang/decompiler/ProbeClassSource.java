package moe.wolfgirl.probejs.lang.decompiler;

import org.jetbrains.java.decompiler.main.extern.IContextSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ProbeClassSource implements IContextSource {

    private final Class<?> clazz;

    public ProbeClassSource(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public String getName() {
        return clazz.getName();
    }

    @Override
    public Entries getEntries() {
        return new Entries(List.of(Entry.parse(clazz.getName())), List.of(), List.of(), List.of());
    }

    @Override
    public InputStream getInputStream(String resource) throws IOException {
        String name = clazz.getName();
        String path = name.replace(".", "/") + ".class";
        return clazz.getClassLoader().getResourceAsStream(path);
    }
}
