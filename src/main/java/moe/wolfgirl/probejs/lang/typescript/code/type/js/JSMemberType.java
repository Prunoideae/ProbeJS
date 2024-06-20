package moe.wolfgirl.probejs.lang.typescript.code.type.js;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;

import java.util.*;
import java.util.stream.Collectors;

public abstract class JSMemberType extends BaseType {
    public final Collection<JSParam> members;


    protected JSMemberType(Collection<JSParam> members) {
        this.members = members;
    }

    @Override
    public Collection<ClassPath> getUsedClassPaths() {
        Set<ClassPath> paths = new HashSet<>();
        for (JSParam member : members) {
            paths.addAll(member.type().getUsedClassPaths());
        }
        return paths;
    }

    protected String formatMembers(Declaration declaration, FormatType type) {
        return members.stream()
                .map(m -> m.format(declaration, type, this::getMemberName))
                .collect(Collectors.joining(", "));
    }

    protected abstract String getMemberName(String name);

    public static abstract class Builder<T extends Builder<T, O>, O extends BaseType> {
        public final Collection<JSParam> members = new ArrayList<>();

        public T member(String name, BaseType type) {
            return member(name, false, type);
        }

        @SuppressWarnings("unchecked")
        public T member(String name, boolean optional, BaseType type) {
            members.add(new JSParam(name, optional, type));
            return (T) this;
        }

        public abstract O build();
    }
}
