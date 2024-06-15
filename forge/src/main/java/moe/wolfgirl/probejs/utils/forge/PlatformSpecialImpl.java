package moe.wolfgirl.probejs.utils.forge;

import moe.wolfgirl.probejs.utils.PlatformSpecial;
import net.minecraftforge.fml.ModList;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class PlatformSpecialImpl extends PlatformSpecial {

    @Override
    public List<File> getModFiles() {
        ModList modList = ModList.get();
        return modList.getModFiles().stream()
                .map(fileInfo -> fileInfo.getFile().getFilePath())
                .map(Path::toFile)
                .collect(Collectors.toList());
    }
}
