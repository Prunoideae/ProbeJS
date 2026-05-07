package moe.wolfgirl.probejs.plugin.builtins;

import moe.wolfgirl.probejs.ProbeConfig;
import moe.wolfgirl.probejs.typescript.ClassPath;
import moe.wolfgirl.probejs.plugin.Priority;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.typescript.Documents;
import moe.wolfgirl.probejs.typescript.document.Types;
import moe.wolfgirl.probejs.typescript.document.base.Code;
import moe.wolfgirl.probejs.typescript.document.base.KindAware;
import moe.wolfgirl.probejs.typescript.document.members.FieldDecl;
import moe.wolfgirl.probejs.typescript.document.members.MethodDecl;
import moe.wolfgirl.probejs.typescript.document.types.special.WrappedType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class InjectBeans extends ProbeJSPlugin {

    @Override
    // After all plugins have modified the classes, then beans are injected to reflect changes in beans
    @Priority(-1000)
    public void transformClass(Documents.ClassDocument document) {
        if (!ProbeConfig.INSTANCE.beans.get()) return; // Bean allergy check

        var classDocument = document.document();
        var classInfo = document.classInfo();
        Set<String> memberNames = new HashSet<>();
        for (Code member : classDocument.members) {
            if (member instanceof MethodDecl methodDecl) memberNames.add(methodDecl.name);
            if (member instanceof FieldDecl fieldDecl) memberNames.add(fieldDecl.name);
        }

        List<Bean> beans = new ArrayList<>();
        Set<String> allBeanNames = new HashSet<>();
        Set<String> duplicatedNames = new HashSet<>(classInfo.getMethodNames());
        for (Code member : classDocument.members) {
            if (member instanceof MethodDecl methodDecl) {
                var beanName = getBeanName(methodDecl.name);
                if (beanName == null) continue;
                if (!allBeanNames.add(beanName)) duplicatedNames.add(beanName);
            }
        }

        for (Code member : classDocument.members) {
            if (member instanceof MethodDecl methodDecl) {
                var beanType = getBeanType(methodDecl.name);
                var beanName = getBeanName(methodDecl.name);
                if (beanName == null || beanType == null || duplicatedNames.contains(beanName)) continue;
                if (memberNames.contains(beanName)) continue;
                var bean = switch (beanType) {
                    case IS, GETTER -> {
                        if (!methodDecl.params.isEmpty()) yield null;
                        yield new Bean(beanName, methodDecl.returnType, beanType);
                    }
                    case SETTER -> {
                        if (methodDecl.params.size() != 1) yield null;
                        yield new Bean(beanName, methodDecl.params.getFirst().typeInfo, beanType);
                    }
                    case null -> throw new RuntimeException("Unexpected null bean!");
                };
                if (bean == null) continue;
                bean.isStatic = methodDecl.isStatic;
                bean.setKind(classDocument.kind);
                beans.add(bean);
            }
        }

        classDocument.members.addAll(beans);
    }

    private BeanType getBeanType(String name) {
        if (name.startsWith("get")) return BeanType.GETTER;
        if (name.startsWith("set")) return BeanType.SETTER;
        if (name.startsWith("is")) return BeanType.IS;
        return null;
    }

    @Nullable
    private String getBeanName(String name) {
        boolean isGet = name.startsWith("get");
        boolean isSet = name.startsWith("set");
        boolean isIs = name.startsWith("is");
        if (!isGet && !isSet && !isIs) return null;

        String nameComponent = name.substring(isIs ? 2 : 3);
        if (nameComponent.isEmpty()) return null;

        String beanPropertyName = nameComponent;
        char ch0 = nameComponent.charAt(0);
        if (Character.isUpperCase(ch0)) {
            if (nameComponent.length() == 1) {
                beanPropertyName = nameComponent.toLowerCase(Locale.ROOT);
            } else {
                char ch1 = nameComponent.charAt(1);
                if (!Character.isUpperCase(ch1)) {
                    beanPropertyName = Character.toLowerCase(ch0) + nameComponent.substring(1);
                }
            }
        }
        return beanPropertyName;
    }

    private static class Bean extends Code implements KindAware {
        private final String name;
        private final Code type;
        private final BeanType beanType;
        private boolean isStatic = false;
        private KindAware.Kind kind = Kind.CLASS;

        private Bean(String name, Code type, BeanType beanType) {
            this.name = name;
            this.type = type;
            this.beanType = beanType;
        }

        @Override
        public Set<ClassPath> getImports() {
            return type.getImports();
        }

        @Override
        public List<String> format(int indent) {
            return List.of(" ".repeat(indent) + (isStatic ? "static " : "") +
                    switch (beanType) {
                        case GETTER, IS -> "get %s(): %s;".formatted(
                                name, type instanceof WrappedType wrappedType &&
                                        wrappedType.formatter.startsWith("this is ") ?
                                        Types.BOOLEAN.first() :
                                        type.first()
                        );
                        case SETTER -> "set %s(value: %s);".formatted(name, type.first());
                    }
            );
        }

        @Override
        public void setResolvedSymbols(Map<ClassPath, String> resolvedSymbols) {
            super.setResolvedSymbols(resolvedSymbols);
            type.setResolvedSymbols(resolvedSymbols);
        }

        @Override
        public void setKind(Kind kind) {
            this.kind = kind;
        }

        @Override
        public boolean shouldAppear(Kind kind) {
            if (this.kind == Kind.INTERFACE && kind == Kind.CLASS) return isStatic;
            else if (this.kind == kind && kind == Kind.INTERFACE) return !isStatic;
            return true;
        }
    }

    private enum BeanType {
        GETTER, SETTER, IS
    }
}
