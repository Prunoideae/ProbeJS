package moe.wolfgirl.probejs.lang.typescript.code.type.js;

import moe.wolfgirl.probejs.lang.typescript.Declaration;

import java.util.*;

public class JSObjectType extends JSMemberType {

    public JSObjectType(Collection<JSParam> members) {
        super(members);
    }

    @Override
    public List<String> format(Declaration declaration, FormatType input) {
        return List.of("{%s}".formatted(String.join(", ", formatMembers(declaration, input))));
    }

    public static class Builder extends JSMemberType.Builder<Builder, JSObjectType> {
        public JSObjectType build() {
            return new JSObjectType(members);
        }
    }
}
