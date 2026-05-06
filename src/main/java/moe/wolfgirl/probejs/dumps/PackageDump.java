package moe.wolfgirl.probejs.dumps;

import moe.wolfgirl.probejs.GameStates;
import moe.wolfgirl.probejs.java.PackageTree;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.typescript.Documents;
import moe.wolfgirl.probejs.typescript.IndexFile;
import moe.wolfgirl.probejs.typescript.base.AliasRegistrar;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;

public class PackageDump {
    private final Path baseDir;

    public PackageDump(Path baseDir) {
        this.baseDir = baseDir;
    }

    private void reportFilesWritten(int count) {
        GameStates.DUMP_STATE.setStatus(Component.literal("Written %d files".formatted(count)));
    }

    public void dump() {
        GameStates.DUMP_STATE.setStatus(Component.literal("Preparing to transpile..."));
        Documents.INSTANCE.clear();
        ProbeJSPlugin.forEachWithPriority("addTypeAlias", plugin -> plugin.addTypeAlias(new AliasRegistrar.Proxy(Documents.INSTANCE)));
        Documents.INSTANCE.transpile();
        GameStates.DUMP_STATE.incrementProgress(1);
        PackageTree packageTree = Documents.INSTANCE.resolveTree();
        int fileCount = 0;
        for (var packageNode : packageTree.traverse()) {
            IndexFile indexFile = new IndexFile(packageNode, Documents.INSTANCE);
            indexFile.dumpTo(baseDir);
            fileCount++;
            reportFilesWritten(fileCount);
        }
        IndexFile rootIndex = new IndexFile(packageTree.getRoot(), Documents.INSTANCE);
        rootIndex.dumpTo(baseDir.resolve("@package"));
        GameStates.DUMP_STATE.incrementProgress(1);
    }
}
