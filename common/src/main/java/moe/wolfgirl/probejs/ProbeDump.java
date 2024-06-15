package moe.wolfgirl.probejs;

import moe.wolfgirl.probejs.decompiler.ProbeDecompiler;
import moe.wolfgirl.probejs.java.ClassRegistry;
import moe.wolfgirl.probejs.snippet.SnippetDump;
import moe.wolfgirl.probejs.typescript.ScriptDump;
import moe.wolfgirl.probejs.utils.GameUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ProbeDump {
    public static final Path SNIPPET_PATH = ProbePaths.WORKSPACE_SETTINGS.resolve("probe.code-snippets");
    public static final Path CLASS_CACHE = ProbePaths.PROBE.resolve("classes.txt");

    final SnippetDump snippetDump = new SnippetDump();
    final Collection<ScriptDump> scriptDumps = new ArrayList<>();
    final ProbeDecompiler decompiler = new ProbeDecompiler();

    public void addScript(ScriptDump dump) {
        scriptDumps.add(dump);
    }

    public void defaultScripts() {
        addScript(ScriptDump.CLIENT_DUMP.get());
        addScript(ScriptDump.SERVER_DUMP.get());
        addScript(ScriptDump.STARTUP_DUMP.get());
    }

    private void onModChange() throws IOException {
        // Decompile stuffs
        if (ProbeConfig.INSTANCE.enableDecompiler.get()) {
            decompiler.fromMods();
            decompiler.decompileContext();
            decompiler.resultSaver.writeTo(ProbePaths.DECOMPILED);
            ClassRegistry.REGISTRY.fromClasses(decompiler.resultSaver.getClasses());
        }
    }

    private void onRegistryChange() throws IOException {

    }

    public void trigger() throws IOException, NoSuchAlgorithmException {

        // Create the snippets
        snippetDump.fromDocs();
        snippetDump.writeTo(SNIPPET_PATH);

        if (GameUtils.modHash() != ProbeConfig.INSTANCE.modHash.get()) {
            onModChange();
            ProbeConfig.INSTANCE.modHash.set(GameUtils.modHash());
        }

        if (GameUtils.registryHash() != ProbeConfig.INSTANCE.registryHash.get()) {
            onRegistryChange();
            ProbeConfig.INSTANCE.registryHash.set(GameUtils.registryHash());
        }

        // Fetch classes that will be used in the dump
        ClassRegistry.REGISTRY.loadFrom(CLASS_CACHE);
        for (ScriptDump scriptDump : scriptDumps) {
            ClassRegistry.REGISTRY.fromClasses(scriptDump.retrieveClasses());
        }

        ClassRegistry.REGISTRY.discoverClasses();
        ClassRegistry.REGISTRY.writeTo(CLASS_CACHE);

        // Spawn a thread for each dump
        List<Thread> dumpThreads = new ArrayList<>();
        for (ScriptDump scriptDump : scriptDumps) {
            Thread t = new Thread(() -> {
                scriptDump.acceptClasses(ClassRegistry.REGISTRY.getFoundClasses());
                try {
                    scriptDump.dump();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
            t.start();
            dumpThreads.add(t);
        }

        for (Thread dumpThread : dumpThreads) {
            try {
                dumpThread.join();
            } catch (InterruptedException ignore) {
            }
        }
    }
}
