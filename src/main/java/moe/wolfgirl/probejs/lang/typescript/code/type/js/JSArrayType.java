package moe.wolfgirl.probejs.lang.typescript.code.type.js;

import moe.wolfgirl.probejs.lang.typescript.Declaration;
import java.util.Collection;
import java.util.List;

public class JSArrayType extends JSMemberType {


    public JSArrayType(Collection<JSParam> members) {
        super(members);
    }

    @Override
    public List<String> format(Declaration declaration, FormatType input) {
        return List.of("[%s]".formatted(formatMembers(declaration, input)));
    }

    public static class Builder extends JSMemberType.Builder<Builder, JSArrayType> {

        @Override
        public JSArrayType build() {
            return new JSArrayType(members);
        }
    }
}
