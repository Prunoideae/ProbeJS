package moe.wolfgirl.probejs.next.plugin;


import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.next.plugin.builtins.alias.EnumTypes;
import moe.wolfgirl.probejs.utils.GameUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ProbeBuiltinDocs extends ProbeJSPlugin {
    public static final ProbeBuiltinDocs INSTANCE = new ProbeBuiltinDocs();

    public static final List<Supplier<ProbeJSPlugin>> BUILTIN_DOCS = new ArrayList<>(List.of(
            EnumTypes::new
    ));

    //TODO: Make this static once we have plugins finalized
    public static void forEach(Consumer<ProbeJSPlugin> consumer) {
        for (Supplier<ProbeJSPlugin> builtinDoc : BUILTIN_DOCS) {
            try {
                consumer.accept(builtinDoc.get());
            } catch (Throwable t) {
                ProbeJS.LOGGER.error("Error when applying builtin doc: %s".formatted(builtinDoc.get().getClass()));
                GameUtils.logException(t);
                ProbeJS.LOGGER.error("If you found severe problem in generated docs (e.g. largely missing types), please report to ProbeJS's github!");
            }
        }
    }
}
