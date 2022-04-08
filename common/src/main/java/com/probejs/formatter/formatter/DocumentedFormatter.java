package com.probejs.formatter.formatter;

public abstract class DocumentedFormatter<T> implements IDocumented<T> {
    protected T document;

    @Override
    public void setDocument(T document) {
        this.document = document;
    }
}
