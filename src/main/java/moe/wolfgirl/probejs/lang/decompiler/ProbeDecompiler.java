package moe.wolfgirl.probejs.lang.decompiler;

import moe.wolfgirl.probejs.ProbeJS;
import net.neoforged.fml.ModList;
import org.jetbrains.java.decompiler.main.Fernflower;

import java.io.File;
import java.io.IOException;
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

    public final ProbeFileSaver resultSaver;
    public final ProbeClassScanner scanner;

    public ProbeDecompiler() {
        this.resultSaver = new ProbeFileSaver();
        this.scanner = new ProbeClassScanner();
    }

    public void addRuntimeSource(File source) {
        try {
            scanner.acceptFile(source);
        } catch (IOException e) {
            ProbeJS.LOGGER.error("Unable to load file: %s".formatted(source));
        }
    }

    public void fromMods() {
        for (File modFile : findModFiles()) {
            addRuntimeSource(modFile);
        }
    }

    public void decompileContext() {
        Fernflower engine = new Fernflower(
                resultSaver, Map.of(),
                new ProbeDecompilerLogger()
        );
        ProbeClassSource source = new ProbeClassSource(scanner.getScannedClasses());
        engine.addSource(source);

        resultSaver.classCount = 0;
        try {
            engine.decompileContext();
        } finally {
            engine.clearContext();
        }
    }
}
