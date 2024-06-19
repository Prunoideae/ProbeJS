package moe.wolfgirl.probejs.lang.typescript.code.type.js;

import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;

import java.util.*;

public class JSObjectType extends BaseType {
    public final Map<String, BaseType> members;

    public JSObjectType(Map<String, BaseType> members) {
        this.members = members;
    }

    @Override
    public Collection<ClassPath> getUsedClassPaths() {
        Set<ClassPath> paths = new HashSet<>();
        for (BaseType value : members.values()) {
            paths.addAll(value.getUsedClassPaths());
        }
        return paths;
    }

    @Override
    public List<String> format(Declaration declaration, FormatType input) {
        List<String> memberStrings = new ArrayList<>();

        for (Map.Entry<String, BaseType> entry : members.entrySet()) {
            String memberName = entry.getKey();
            BaseType type = entry.getValue();

            if (type instanceof JSLambdaType lambdaType) {
                memberStrings.add(lambdaType.formatWithName(memberName, declaration, input));
            } else {
                boolean isOptional = memberName.endsWith("?");
                if (isOptional) memberName = memberName.substring(0, memberName.length() - 1);
                String member = ProbeJS.GSON.toJson(memberName);
                if (isOptional) member = member + "?";
                memberStrings.add("%s: %s".formatted(member, type.line(declaration, input)));
            }
        }
        return List.of("{%s}".formatted(String.join(", ", memberStrings)));
    }

    public static class Builder {
        public final Map<String, BaseType> members = new HashMap<>();

        public Builder member(String name, BaseType type) {
            members.put(name, type);
            return this;
        }

        public JSObjectType build() {
            return new JSObjectType(members);
        }
    }
}
