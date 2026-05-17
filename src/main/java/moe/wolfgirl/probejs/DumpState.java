package moe.wolfgirl.probejs;

import dev.latvian.mods.kubejs.KubeJSPaths;
import moe.wolfgirl.probejs.misc.llm.NotesToLLM;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.typescript.Documents;
import moe.wolfgirl.probejs.typescript.SidedDocuments;
import moe.wolfgirl.probejs.typescript.SpecialDocuments;
import moe.wolfgirl.probejs.utils.GameUtils;
import moe.wolfgirl.probejs.dumps.OtherDump;
import moe.wolfgirl.probejs.dumps.PackageDump;
import moe.wolfgirl.probejs.dumps.ProjectDump;
import moe.wolfgirl.probejs.java.ClassRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class DumpState {
    // Fetching initial classes
    // Discovering more classes
    // Transpiling to TypeScript
    // Generating @package dumps
    // Fetching @special dumps
    // Generating @special dumps
    // Fetching @sided dumps
    // Generating @sided dumps
    // Writing down snippets
    // Finalizing the dump
    public static final int TOTAL_STEPS = 10;
    public int maxProgress = TOTAL_STEPS;
    public int progress = 0;
    public Component status = Component.literal("Ready");

    public static void startDump() {
        try {
            if (GameStates.DUMP_STATE != null) throw new IllegalStateException("Dump already in progress");
            ProbeJSPlugin.forEachWithPriority("addUsageHintsForAgents", registry -> registry.addUsageHintsForAgents(new NotesToLLM.Registry()));
            GameStates.DUMP_STATE = new DumpState();
            GameStates.DUMP_STATE.setProgress(0);
            if (GameStates.DUMP_SCREEN != null) GameStates.DUMP_SCREEN.initFromState();
            ClassRegistry.INSTANCE.fetchInitialClasses();
            GameStates.DUMP_STATE.incrementProgress(1);
            ClassRegistry.INSTANCE.discover();
            GameStates.DUMP_STATE.incrementProgress(1);
            PackageDump packageDump = new PackageDump(ProbePaths.PROBE);
            packageDump.dump();
            OtherDump otherDump = new OtherDump(ProbePaths.PROBE);
            otherDump.dump();
            ProjectDump projectDump = new ProjectDump(KubeJSPaths.GAMEDIR);
            projectDump.dump();

            GameStates.DUMP_STATE.setStatus(Component.literal("Dump complete!"));
            GameStates.DUMP_STATE = null;
            GameStates.DUMP_SCREEN.onDumpFinished();

            // clear the registries to free up memory
            ClassRegistry.INSTANCE.clear();
            Documents.INSTANCE.clear();
            SpecialDocuments.INSTANCE.clear();
            SidedDocuments.INSTANCE.clear();
        } catch (Exception e) {
            GameStates.DUMP_STATE.setStatus(Component.literal("Dump failed!").withStyle(ChatFormatting.RED));
            GameUtils.logException(e);
            GameStates.DUMP_STATE = null;
            if (GameStates.DUMP_SCREEN != null) GameStates.DUMP_SCREEN.onDumpFinished();
        }

    }

    public void setMaxProgress(int maxProgress) {
        this.maxProgress = maxProgress;
        if (GameStates.DUMP_SCREEN != null) GameStates.DUMP_SCREEN.setMaxProgress(maxProgress);
    }

    public void setProgress(int progress) {
        this.progress = progress;
        if (GameStates.DUMP_SCREEN != null) GameStates.DUMP_SCREEN.setProgress(progress);
    }

    public void incrementProgress(int step) {
        setProgress(this.progress + step);
    }

    public void setStatus(Component status) {
        this.status = status;
        if (GameStates.DUMP_SCREEN != null) GameStates.DUMP_SCREEN.setStatus(status);
    }
}
