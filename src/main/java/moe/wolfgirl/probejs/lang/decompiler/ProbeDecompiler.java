package moe.wolfgirl.probejs.lang.decompiler;

import moe.wolfgirl.probejs.lang.decompiler.remapper.ProbeRemapper;
import net.minecraftforge.fml.ModList;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProbeDecompiler {
    public static List<File> findModFiles() {
        ModList modList = ModList.get();
        return modList.getModFiles().stream()
                .map(fileInfo -> fileInfo.getFile().getFilePath())
                .map(Path::toFile)
                .collect(Collectors.toList());
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
        resultSaver.classCount = 0;
        try {
            engine.decompileContext();
        } finally {
            engine.clearContext();
        }
    }
}
