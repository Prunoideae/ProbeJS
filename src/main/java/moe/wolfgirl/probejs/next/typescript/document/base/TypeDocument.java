package moe.wolfgirl.probejs.next.typescript.document.base;

import moe.wolfgirl.probejs.next.typescript.document.types.ArrayType;
import moe.wolfgirl.probejs.next.typescript.document.types.ParamType;
import moe.wolfgirl.probejs.next.typescript.document.types.VariableType;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public interface TypeDocument {
    default ArrayType asArray() {
        if (this instanceof Type thisCode) {
            return new ArrayType(thisCode);
        } else throw new RuntimeException("Cannot convert to ArrayType: " + this.getClass().getName());
    }

    default ParamType withParams(Type... paramTypes) {
        if (this instanceof Type thisCode) {
            if (this instanceof VariableType) {
                throw new RuntimeException("Cannot convert to ParamType: VariableType cannot be used as a function type");
            }

            return new ParamType(thisCode, List.of(paramTypes));
        } else throw new RuntimeException("Cannot convert to ParamType: " + this.getClass().getName());
    }

    Collection<Code> getContainedTypes();

    default Stream<Code> getAllTypes() {
        return getContainedTypes().stream()
                .flatMap(contained -> contained instanceof TypeDocument typeDoc
                        ? Stream.concat(Stream.of(contained), typeDoc.getAllTypes())
                        : Stream.of(contained)
                );
    }
}
