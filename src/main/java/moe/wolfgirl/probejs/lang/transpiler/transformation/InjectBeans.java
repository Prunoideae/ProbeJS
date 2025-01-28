package moe.wolfgirl.probejs.lang.transpiler.transformation;

import moe.wolfgirl.probejs.ProbeJS;
import moe.wolfgirl.probejs.lang.java.clazz.Clazz;
import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.Code;
import moe.wolfgirl.probejs.lang.typescript.code.ImportInfo;
import moe.wolfgirl.probejs.lang.typescript.code.member.ClassDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.MethodDecl;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;

import java.util.*;

public class InjectBeans implements ClassTransformer {
    @Override
    public void transform(Clazz clazz, ClassDecl classDecl) {
        Set<String> names = new HashSet<>();
        for (MethodDecl method : classDecl.methods) {
            names.add(method.name);
        }
        for (MethodDecl method : classDecl.methods) {
            if (method.isStatic) continue;
            if (!isBean(method.name)) continue;

            if (method.name.startsWith("set") && method.params.size() == 1) {
                String beanName = getBeanName(method.name);
                if (names.contains(beanName)) continue;
                classDecl.bodyCode.add(new BeanDecl(
                        "set %s(value: %s)",
                        beanName,
                        Types.ignoreContext(method.params.getFirst().type, BaseType.FormatType.INPUT)
                ));
            } else if (method.params.isEmpty()) {
                String beanName = getBeanName(method.name);
                if (names.contains(beanName)) continue;
                classDecl.bodyCode.add(new BeanDecl("get %s(): %s", beanName, beanName.startsWith("is") ? Types.BOOLEAN : method.returnType));
            }
        }
    }

    /**
     * @author DeepSeek-R1
     */
    public static String getBeanName(String name) {
        boolean memberIsIsMethod = name.startsWith("is");
        int prefixLength = memberIsIsMethod ? 2 : 3;

        String nameComponent = name.substring(prefixLength);
        if (nameComponent.isEmpty()) {
            return "";
        }

        char ch0 = nameComponent.charAt(0);
        String beanPropertyName = nameComponent;

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

    /**
     * @author DeepSeek-R1
     */
    public static boolean isBean(String name) {
        if (name == null) {
            return false;
        }

        boolean isGetOrSet = name.startsWith("get") || name.startsWith("set");
        boolean isIs = name.startsWith("is");
        if (!isGetOrSet && !isIs) {
            return false;
        }

        int prefixLength = isIs ? 2 : 3;
        return name.length() > prefixLength;
    }

    public static class BeanDecl extends Code {
        public String formattingString;
        public String name;
        public BaseType baseType;

        BeanDecl(String formattingString, String name, BaseType baseType) {
            this.formattingString = formattingString;
            this.name = name;
            this.baseType = baseType;
        }

        @Override
        public Collection<ImportInfo> getUsedImports() {
            return baseType.getUsedImports();
        }

        @Override
        public List<String> format(Declaration declaration) {
            return List.of(formattingString.formatted(ProbeJS.GSON.toJson(name), baseType.line(declaration)));
        }
    }
}
