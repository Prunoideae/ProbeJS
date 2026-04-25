package moe.wolfgirl.probejs.next;

import moe.wolfgirl.probejs.next.java.ClassRegistry;
import moe.wolfgirl.probejs.next.java.PackageTree;
import moe.wolfgirl.probejs.next.typescript.IndexFile;

import java.nio.file.Path;

public class PackageDump {
    private final Path baseDir;

    public PackageDump(Path baseDir) {
        this.baseDir = baseDir;
    }

    public void dump() {
        PackageTree packageTree = ClassRegistry.INSTANCE.resolveTree();
        for (var packageNode : packageTree.traverse()) {
            IndexFile indexFile = new IndexFile(packageNode);
            indexFile.dumpTo(baseDir);
        }
        IndexFile rootIndex = new IndexFile(packageTree.getRoot());
        rootIndex.dumpTo(baseDir);
    }
}
