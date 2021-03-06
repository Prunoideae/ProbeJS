package com.probejs.formatter.formatter.clazz;

import com.probejs.ProbeConfig;
import com.probejs.ProbeJS;
import com.probejs.document.*;
import com.probejs.document.comment.special.CommentHidden;
import com.probejs.document.type.IType;
import com.probejs.formatter.NameResolver;
import com.probejs.formatter.formatter.DocumentReceiver;
import com.probejs.formatter.formatter.IFormatter;
import com.probejs.info.ClassInfo;
import com.probejs.info.type.ITypeInfo;
import com.probejs.info.type.InfoTypeResolver;
import com.probejs.info.type.TypeInfoClass;
import com.probejs.info.type.TypeInfoParameterized;
import dev.latvian.mods.kubejs.client.ClientEventJS;
import dev.latvian.mods.kubejs.event.EventJS;
import dev.latvian.mods.kubejs.event.StartupEventJS;
import dev.latvian.mods.kubejs.server.ServerEventJS;
import dev.latvian.mods.rhino.util.EnumTypeWrapper;

import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.stream.Collectors;

public class FormatterClass extends DocumentReceiver<DocumentClass> implements IFormatter {
    private final ClassInfo classInfo;
    private final Map<String, FormatterField> fieldFormatters = new HashMap<>();
    private final Map<String, List<FormatterMethod>> methodFormatters = new HashMap<>();
    private final List<DocumentField> fieldAdditions = new ArrayList<>();
    private final List<DocumentMethod> methodAdditions = new ArrayList<>();
    private boolean internal = false;


    public FormatterClass(ClassInfo classInfo) {
        this.classInfo = classInfo;
        classInfo.getMethodInfo().forEach(methodInfo -> methodFormatters.computeIfAbsent(methodInfo.getName(), s -> new ArrayList<>()).add(new FormatterMethod(methodInfo)));
        classInfo.getFieldInfo().forEach(fieldInfo -> fieldFormatters.put(fieldInfo.getName(), new FormatterField(fieldInfo)));
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    private List<String> fetchAdditionalComments(Integer indent) {
        List<String> comments = new ArrayList<>();
        comments.add("%s* @javaClass %s".formatted(" ".repeat(indent), classInfo.getClazzRaw().getName()));
        return comments;
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        List<String> formatted = new ArrayList<>();
        DocumentComment comment = document == null ? null : document.getComment();
        if (comment != null) {
            if (comment.getSpecialComment(CommentHidden.class) != null)
                return formatted;
            List<String> formattedText = comment.format(indent, stepIndent);
            formattedText.addAll(formattedText.size() - 1, fetchAdditionalComments(indent));
            formatted.addAll(formattedText);
        } else {
            List<String> formattedText = new ArrayList<>();
            formattedText.add("%s/**".formatted(" ".repeat(indent)));
            formattedText.addAll(fetchAdditionalComments(indent));
            formattedText.add("%s*/".formatted(" ".repeat(indent)));
            formatted.addAll(formattedText);
        }

        List<String> assignableTypes = Manager.typesAssignable
                .getOrDefault(classInfo.getName(), new ArrayList<>())
                .stream()
                .map(IType::getTypeName)
                .collect(Collectors.toList());

        assignableTypes.addAll(NameResolver.getClassAssignments(classInfo.getClazzRaw()));

        if (classInfo.isEnum()) {
            Class<?> clazz = classInfo.getClazzRaw();
            EnumTypeWrapper<?> enumWrapper = EnumTypeWrapper.get(clazz);
            enumWrapper.nameValues.keySet().stream().map(ProbeJS.GSON::toJson).forEach(assignableTypes::add);
        }

        // First line
        List<String> firstLine = new ArrayList<>();

        if (!internal)
            firstLine.add("declare");

        if (classInfo.isAbstract() && !classInfo.isInterface())
            firstLine.add("abstract");

        if (classInfo.isInterface())
            firstLine.add("interface");
        else
            firstLine.add("class");

        firstLine.add(NameResolver.getResolvedName(classInfo.getName()).getLastName());
        if (classInfo.getClazzRaw().getTypeParameters().length != 0) {
            firstLine.add("<%s>".formatted(Arrays.stream(classInfo.getClazzRaw().getTypeParameters()).map(TypeVariable::getName).collect(Collectors.joining(", "))));
        }
        if (classInfo.getSuperClass() != null) {
            firstLine.add("extends");
            firstLine.add(formatTypeParameterized(InfoTypeResolver.resolveType(classInfo.getClazzRaw().getGenericSuperclass())));

            if (NameResolver.specialExtension.containsKey(classInfo.getClazzRaw())) {
                String extensions = NameResolver.specialExtension.get(classInfo.getClazzRaw()).stream().map(IType::getTypeName).collect(Collectors.joining(", "));
                if (!extensions.isEmpty())
                    firstLine.add("," + extensions);
            }
        }
        if (!classInfo.getInterfaces().isEmpty()) {
            firstLine.add(classInfo.isInterface() ? "extends" : "implements");
            firstLine.add("%s".formatted(Arrays.stream(classInfo.getClazzRaw().getGenericInterfaces()).map(InfoTypeResolver::resolveType).map(FormatterClass::formatTypeParameterized).collect(Collectors.joining(", "))));
            if (NameResolver.specialExtension.containsKey(classInfo.getClazzRaw())) {
                String extensions = NameResolver.specialExtension.get(classInfo.getClazzRaw()).stream().map(IType::getTypeName).collect(Collectors.joining(", "));
                if (!extensions.isEmpty() && classInfo.isInterface())
                    firstLine.add("," + extensions);
            }
        }
        firstLine.add("{");
        formatted.add(" ".repeat(indent) + String.join(" ", firstLine));

        // Fields, methods
        Set<List<String>> formattedMethods = new HashSet<>();
        methodFormatters.values().forEach(m -> m.stream().filter(mf -> ProbeConfig.INSTANCE.dumpMethod || (mf.getBean() == null || fieldFormatters.containsKey(mf.getBean()) || methodFormatters.containsKey(mf.getBean()))).forEach(mf -> {
            if (classInfo.isInterface() && mf.getMethodInfo().isStatic() && internal)
                return;
            List<String> formattedMethod = mf.format(indent + stepIndent, stepIndent);
            if (!formattedMethods.contains(formattedMethod)) {
                formatted.addAll(formattedMethod);
                formattedMethods.add(formattedMethod);
            }
        }));

        fieldFormatters.entrySet().stream().filter(e -> !methodFormatters.containsKey(e.getKey())).forEach(f -> {
            if (classInfo.isInterface() && f.getValue().getFieldInfo().isStatic() && internal)
                return;
            f.getValue().setInterface(classInfo.isInterface());
            formatted.addAll(f.getValue().format(indent + stepIndent, stepIndent));
        });

        // beans
        if (!classInfo.isInterface()) {
            Map<String, FormatterMethod> getterMap = new HashMap<>();
            Map<String, List<FormatterMethod>> setterMap = new HashMap<>();

            methodFormatters.values().forEach(ml -> ml.forEach(m -> {
                String beanName = m.getBean();
                if (beanName != null && Character.isAlphabetic(beanName.charAt(0)))
                    if (!fieldFormatters.containsKey(beanName) && !methodFormatters.containsKey(beanName)) {
                        if (m.isGetter())
                            getterMap.put(beanName, m);
                        else
                            setterMap.computeIfAbsent(beanName, s -> new ArrayList<>()).add(m);
                    }
            }));

            getterMap.forEach((k, v) -> formatted.addAll(v.formatBean(indent + stepIndent, stepIndent)));
            setterMap.forEach((k, v) -> {
                Optional<FormatterMethod> result = v.stream()
                        .filter(m -> !getterMap.containsKey(m.getBean()) || getterMap.get(m.getBean()).getBeanTypeString().equals(m.getBeanTypeString()))
                        .findFirst();
                result.ifPresent(formatterMethod -> formatted.addAll(formatterMethod.formatBean(indent + stepIndent, stepIndent)));
            });
        }

        // constructors
        if (!classInfo.isInterface())
            if (internal) {
                formatted.add(" ".repeat(indent + stepIndent) + "/**");
                formatted.add(" ".repeat(indent + stepIndent) + "* Internal constructor, this means that it's not valid and you will get an error if you use it.");
                formatted.add(" ".repeat(indent + stepIndent) + "*/");
                formatted.add(" ".repeat(indent + stepIndent) + "protected constructor();");
            } else {
                classInfo.getConstructorInfo().stream().map(FormatterConstructor::new).forEach(f -> formatted.addAll(f.format(indent + stepIndent, stepIndent)));
            }

        // additions
        fieldAdditions.forEach(fieldDoc -> formatted.addAll(fieldDoc.format(indent + stepIndent, stepIndent)));
        methodAdditions.forEach(methodDoc -> formatted.addAll(methodDoc.format(indent + stepIndent, stepIndent)));

        formatted.add(" ".repeat(indent) + "}");
        //type conversion
        String underName = NameResolver.getResolvedName(classInfo.getName()).getLastName() + "_";
        String origName = NameResolver.getResolvedName(classInfo.getName()).getLastName();
        if (NameResolver.specialTypeFormatters.containsKey(classInfo.getClazzRaw()))
            assignableTypes.add(new FormatterType(new TypeInfoParameterized(new TypeInfoClass(classInfo.getClazzRaw()), classInfo.getParameters())).format(0, 0));
        List<ITypeInfo> params = classInfo.getParameters();
        if (params.size() > 0) {
            String paramString = "<%s>".formatted(params.stream().map(ITypeInfo::getTypeName).collect(Collectors.joining(", ")));
            underName += paramString;
            origName += paramString;
        }

        assignableTypes.add(origName);
        if (comment != null) {
            formatted.addAll(comment.format(indent, stepIndent));
        }
        formatted.add(" ".repeat(indent) + "type %s = %s;".formatted(underName, String.join(" | ", assignableTypes)));

        return formatted;
    }

    @Override
    public void addDocument(DocumentClass document) {
        super.addDocument(document);
        document.getFields().forEach(documentField -> {
            if (fieldFormatters.containsKey(documentField.getName()))
                fieldFormatters.get(documentField.getName()).addDocument(documentField);
            else
                fieldAdditions.add(documentField);
        });

        document.getMethods().forEach(documentMethod -> {
            if (methodFormatters.containsKey(documentMethod.getName()))
                methodFormatters.get(documentMethod.getName()).forEach(formatterMethod -> {
                    if (documentMethod.testMethod(formatterMethod.getMethodInfo()))
                        formatterMethod.addDocument(documentMethod);
                });
            else
                methodAdditions.add(documentMethod);
        });
    }

    public static String formatTypeParameterized(ITypeInfo info) {
        StringBuilder sb = new StringBuilder(new FormatterType(info, false).format(0, 0));
        if (info instanceof TypeInfoClass clazz) {
            if (clazz.getTypeVariables().size() != 0)
                sb.append("<%s>".formatted(String.join(", ", Collections.nCopies(clazz.getTypeVariables().size(), "any"))));
        }
        return sb.toString();
    }
}
