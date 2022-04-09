package com.probejs.formatter.formatter;

public abstract class DocumentReceiver<T> implements IDocumented<T> {
    public T document;

    @Override
    public void addDocument(T document) {
        this.document = document;
    }
}
