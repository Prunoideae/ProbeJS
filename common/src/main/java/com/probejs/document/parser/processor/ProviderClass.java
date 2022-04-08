package com.probejs.document.parser.processor;

import com.probejs.document.DocumentClass;
import com.probejs.document.IConcrete;
import com.probejs.document.IDecorative;
import com.probejs.document.IDocument;
import com.probejs.document.parser.handler.IStateHandler;
import com.probejs.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class ProviderClass implements IStateHandler<String>, IDocumentProvider<DocumentClass> {
    public static List<Pair<Predicate<String>, BiFunction<String, ProviderClass, IStateHandler<String>>>> handlers = new ArrayList<>();
    private final List<IDocumentProvider<?>> elements = new ArrayList<>();
    private String name;

    public static void addMultiHandler(Predicate<String> condition, BiFunction<String, ProviderClass, IStateHandler<String>> handler) {
        handlers.add(new Pair<>(condition, handler));
    }

    public static void addSingleHandler(Predicate<String> condition, BiConsumer<String, ProviderClass> handler) {
        handlers.add(new Pair<>(condition, (s, documentHandler) -> {
            handler.accept(s, documentHandler);
            return null;
        }));
    }

    public void addElement(IDocumentProvider<?> element) {
        this.elements.add(element);
    }

    @Override
    public void trial(String element, List<IStateHandler<String>> stack) {
        element = element.strip();
        if (element.startsWith("class") && element.endsWith("{")) {
            String[] elements = element.split(" ");
            name = elements[1];
        } else if (element.equals("}")) {
            stack.remove(this);
        }

        for (Pair<Predicate<String>, BiFunction<String, ProviderClass, IStateHandler<String>>> multiHandler : handlers) {
            if (multiHandler.getFirst().test(element)) {
                IStateHandler<String> layer = multiHandler.getSecond().apply(element, this);
                if (layer != null) {
                    layer.trial(element, stack);
                    stack.add(layer);
                }
                return;
            }
        }
    }

    @Override
    public DocumentClass provide() {
        DocumentClass document = new DocumentClass();
        document.setName(name);
        List<IDecorative> decos = new ArrayList<>();
        for (IDocumentProvider<?> provider : elements) {
            IDocument doc = provider.provide();
            if (doc instanceof IDecorative) {
                decos.add((IDecorative) doc);
            } else {
                if (doc instanceof IConcrete) {
                    ((IConcrete) doc).acceptDeco(decos.stream().toList());
                }
                decos.clear();
                document.acceptProperty(doc);
            }
        }
        return document;
    }

    public String getName() {
        return name;
    }
}
