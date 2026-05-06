package moe.wolfgirl.probejs.dumps;

import moe.wolfgirl.probejs.GameStates;
import moe.wolfgirl.probejs.java.PackageTree;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.typescript.IndexFile;
import moe.wolfgirl.probejs.typescript.SidedDocuments;
import moe.wolfgirl.probejs.typescript.SpecialDocuments;
import moe.wolfgirl.probejs.typescript.base.DocumentRegistrar;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;

public class OtherDump {
    private final Path baseDir;

    public OtherDump(Path baseDir) {
        this.baseDir = baseDir;
    }

    public void dump() {
        GameStates.DUMP_STATE.setStatus(Component.literal("Finding @special..."));
        SpecialDocuments.INSTANCE.clear();
        ProbeJSPlugin.forEachWithPriority("addSpecialDocuments", plugin -> plugin.addSpecialDocuments(new DocumentRegistrar.Proxy(SpecialDocuments.INSTANCE)));
        GameStates.DUMP_STATE.incrementProgress(1);
        PackageTree packageTree = SpecialDocuments.INSTANCE.resolveTree();
        int fileCount = 0;
        for (var packageNode : packageTree.traverse()) {
            IndexFile indexFile = new IndexFile(packageNode, SpecialDocuments.INSTANCE);
            indexFile.dumpTo(baseDir);
            fileCount++;
            GameStates.DUMP_STATE.setStatus(Component.literal("Written %d files".formatted(fileCount)));
        }
        IndexFile rootIndex = new IndexFile(packageTree.getRoot(), SpecialDocuments.INSTANCE);
        rootIndex.dumpTo(baseDir.resolve("@special"));
        GameStates.DUMP_STATE.incrementProgress(1);

        GameStates.DUMP_STATE.setStatus(Component.literal("Finding @sided..."));
        SidedDocuments.INSTANCE.clear();
        ProbeJSPlugin.forEachWithPriority("addSidedDocuments", plugin -> plugin.addSidedDocuments(new DocumentRegistrar.Proxy(SidedDocuments.INSTANCE)));
        GameStates.DUMP_STATE.incrementProgress(1);
        packageTree = SidedDocuments.INSTANCE.resolveTree();
        fileCount = 0;
        for (var packageNode : packageTree.traverse()) {
            IndexFile indexFile = new IndexFile(packageNode, SidedDocuments.INSTANCE);
            indexFile.dumpTo(baseDir);
            fileCount++;
            GameStates.DUMP_STATE.setStatus(Component.literal("Written %d files".formatted(fileCount)));
        }
        GameStates.DUMP_STATE.incrementProgress(1);
        // No need to generate root for sided documents because roots are always one of the subpackages
    }
}
