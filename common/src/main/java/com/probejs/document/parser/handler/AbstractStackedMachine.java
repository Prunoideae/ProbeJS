package com.probejs.document.parser.handler;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractStackedMachine<T> {
    protected final List<IStateHandler<T>> stack = new ArrayList<>();

    public void step(T element) {
        if (!this.isEmpty())
            stack.get(stack.size() - 1).trial(element, stack);
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    public List<? super IStateHandler<T>> getStack() {
        return stack;
    }
}
