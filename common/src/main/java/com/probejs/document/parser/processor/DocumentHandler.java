package com.probejs.document.parser.processor;


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

public class DocumentHandler implements IStateHandler<String> {
    public static List<Pair<Predicate<String>, BiFunction<String, DocumentHandler, IStateHandler<String>>>> handlers = new ArrayList<>();
    private final List<IDocumentProvider<?>> elements = new ArrayList<>();

    public static void addMultiHandler(Predicate<String> condition, BiFunction<String, DocumentHandler, IStateHandler<String>> handler) {
        handlers.add(new Pair<>(condition, handler));
    }

    public static void addSingleHandler(Predicate<String> condition, BiConsumer<String, DocumentHandler> handler) {
        handlers.add(new Pair<>(condition, (s, documentHandler) -> {
            handler.accept(s, documentHandler);
            return null;
        }));
    }

    public void addElement(IDocumentProvider<?> element) {
        this.elements.add(element);
    }


    public List<IDocument> getDocuments() {
        List<IDecorative> decos = new ArrayList<>();
        List<IDocument> elements = new ArrayList<>();
        for (IDocumentProvider<?> document : this.elements) {
            IDocument doc = document.provide();
            if (doc instanceof IDecorative) {
                decos.add((IDecorative) doc);
            } else {
                if (doc instanceof IConcrete) {
                    ((IConcrete) doc).acceptDeco(decos);
                }
                decos.clear();
                elements.add(doc);
            }
        }
        return elements;
    }

    @Override
    public void trial(String element, List<IStateHandler<String>> stack) {
        element = element.strip();
        for (Pair<Predicate<String>, BiFunction<String, DocumentHandler, IStateHandler<String>>> multiHandler : handlers) {
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

}
