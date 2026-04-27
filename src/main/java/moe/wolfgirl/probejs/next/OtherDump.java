package moe.wolfgirl.probejs.next;

import moe.wolfgirl.probejs.next.java.PackageTree;
import moe.wolfgirl.probejs.next.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.next.typescript.IndexFile;
import moe.wolfgirl.probejs.next.typescript.SidedDocuments;
import moe.wolfgirl.probejs.next.typescript.SpecialDocuments;
import moe.wolfgirl.probejs.next.typescript.base.DocumentRegistrar;

import java.nio.file.Path;

public class OtherDump {
    private final Path baseDir;

    public OtherDump(Path baseDir) {
        this.baseDir = baseDir;
    }

    public void dump() {
        SpecialDocuments.INSTANCE.clear();
        ProbeJSPlugin.forEachPlugin(plugin -> plugin.addSpecialDocuments(new DocumentRegistrar.Proxy(SpecialDocuments.INSTANCE)));
        PackageTree packageTree = SpecialDocuments.INSTANCE.resolveTree();
        for (var packageNode : packageTree.traverse()) {
            IndexFile indexFile = new IndexFile(packageNode, SpecialDocuments.INSTANCE);
            indexFile.dumpTo(baseDir);
        }
        IndexFile rootIndex = new IndexFile(packageTree.getRoot(), SpecialDocuments.INSTANCE);
        rootIndex.dumpTo(baseDir.resolve("@special"));

        SidedDocuments.INSTANCE.clear();
        ProbeJSPlugin.forEachPlugin(plugin -> plugin.addSidedDocuments(new DocumentRegistrar.Proxy(SidedDocuments.INSTANCE)));
        packageTree = SidedDocuments.INSTANCE.resolveTree();
        for (var packageNode : packageTree.traverse()) {
            IndexFile indexFile = new IndexFile(packageNode, SidedDocuments.INSTANCE);
            indexFile.dumpTo(baseDir);
        }
        // No need to generate root for sided documents because roots are always one of the subpackages
    }
}
