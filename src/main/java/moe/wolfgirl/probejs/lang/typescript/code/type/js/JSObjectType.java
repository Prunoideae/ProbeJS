package moe.wolfgirl.probejs.lang.typescript.code.type.js;

import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.lang.typescript.Declaration;

import java.util.*;

public class JSObjectType extends JSMemberType {
    private String delimiter;

    public JSObjectType(Collection<JSParam> members) {
        super(members);
        delimiter = ", ";
    }

    @Override
    protected String getMemberName(String name) {
        return ProbeJS.GSON.toJson(name);
    }

    @Override
    public List<String> format(Declaration declaration, FormatType input) {
        return List.of("{%s}".formatted(formatMembers(declaration, input, delimiter)));
    }

    private JSObjectType asIndexed() {
        delimiter = ";\n";
        return this;
    }

    public static class Builder extends JSMemberType.Builder<Builder, JSObjectType> {
        public JSObjectType build() {
            return new JSObjectType(members);
        }

        public JSObjectType buildIndexed() {
            return build().asIndexed();
        }
    }
}
