package moe.wolfgirl.probejs.decompiler;

import moe.wolfgirl.probejs.decompiler.remapper.ProbeRemapper;
import moe.wolfgirl.probejs.utils.PlatformSpecial;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ProbeDecompiler {
    public static List<File> findModFiles() {
        return PlatformSpecial.INSTANCE.get().getModFiles();
    }

    public final Fernflower engine;
    public final ProbeFileSaver resultSaver;

    public ProbeDecompiler() {
        this.resultSaver = new ProbeFileSaver();

        this.engine = new Fernflower(
                resultSaver,
                Map.of(
                        IFernflowerPreferences.RENAME_ENTITIES, "1",
                        IFernflowerPreferences.USER_RENAMER_CLASS, ProbeRemapper.class.getName()
                ),
                new ProbeDecompilerLogger()
        );
    }

    public void addSource(File source) {
        engine.addSource(source);
    }

    public void fromMods() {
        for (File modFile : findModFiles()) {
            addSource(modFile);
        }
    }

    public void decompileContext() {
        try {
            engine.decompileContext();
        } finally {
            engine.clearContext();
        }
    }
}
