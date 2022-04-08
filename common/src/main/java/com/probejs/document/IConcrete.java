package com.probejs.document;

import java.util.List;

public interface IConcrete extends IDocument {
    void acceptDeco(List<IDecorative> decorates);
}
