package moe.wolfgirl.next.typescript.code.type;

import moe.wolfgirl.ProbeJS;
import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.typescript.Declaration;

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
    public List<String> format(Declaration declaration) {
        List<String> memberStrings = new ArrayList<>();

        for (Map.Entry<String, BaseType> entry : members.entrySet()) {
            String member = ProbeJS.GSON.toJson(entry.getKey());
            BaseType type = entry.getValue();

            memberStrings.add("%s: %s".formatted(member, type.line(declaration)));
        }
        return List.of("{%s}".formatted(String.join(", ", memberStrings)));
    }
}
