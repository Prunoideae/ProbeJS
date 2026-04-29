package moe.wolfgirl.probejs.next;

import moe.wolfgirl.probejs.next.java.ClassRegistry;
import moe.wolfgirl.probejs.next.java.PackageTree;
import moe.wolfgirl.probejs.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.next.typescript.Documents;
import moe.wolfgirl.probejs.next.typescript.IndexFile;
import moe.wolfgirl.probejs.next.typescript.base.AliasRegistrar;

import java.nio.file.Path;

public class PackageDump {
    private final Path baseDir;

    public PackageDump(Path baseDir) {
        this.baseDir = baseDir;
    }

    public void dump() {
        Documents.INSTANCE.clear();
        ProbeJSPlugin.forEachPlugin(plugin -> plugin.addTypeAlias(new AliasRegistrar.Proxy(Documents.INSTANCE)));
        Documents.INSTANCE.transpile();
        PackageTree packageTree = ClassRegistry.INSTANCE.resolveTree();
        for (var packageNode : packageTree.traverse()) {
            IndexFile indexFile = new IndexFile(packageNode, Documents.INSTANCE);
            indexFile.dumpTo(baseDir);
        }
        IndexFile rootIndex = new IndexFile(packageTree.getRoot(), Documents.INSTANCE);
        rootIndex.dumpTo(baseDir.resolve("@package"));
    }
}
