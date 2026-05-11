package moe.wolfgirl.probejs.plugin.builtins;

import moe.wolfgirl.probejs.ProbeConfig;
import moe.wolfgirl.probejs.misc.llm.CommentBlock;
import moe.wolfgirl.probejs.misc.llm.NotesToLLM;
import moe.wolfgirl.probejs.plugin.Priority;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.typescript.Documents;

public class InjectDocsForAgents extends ProbeJSPlugin {

    @Override
    @Priority(-100) // So that the notes are almost added to the top but below class declaration
    public void transformClass(Documents.ClassDocument document) {
        if (!ProbeConfig.INSTANCE.hintsForLLM.get()) return;
        ClassPath classPath = document.classInfo().classPath();
        var lines = NotesToLLM.NOTES.get(classPath);
        if (lines == null) return;
        document.document().members.addFirst(new CommentBlock(lines));
    }
}
