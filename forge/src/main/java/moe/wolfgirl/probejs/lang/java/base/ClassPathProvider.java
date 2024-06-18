package moe.wolfgirl.probejs.lang.java.base;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;

import java.util.Collection;

public interface ClassPathProvider {
    Collection<ClassPath> getClassPaths();
}
