package com.probejs.document.parser.handler;

import java.util.List;

public interface IStateHandler<T> {
    void trial(T element, List<IStateHandler<T>> stack);
}
