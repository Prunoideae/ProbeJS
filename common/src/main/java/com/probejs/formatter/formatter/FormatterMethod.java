package com.probejs.formatter.formatter;

import com.google.common.collect.Sets;
import com.probejs.document.DocumentComment;
import com.probejs.document.DocumentMethod;
import com.probejs.document.Manager;
import com.probejs.document.comment.CommentUtil;
import com.probejs.document.comment.special.CommentReturns;
import com.probejs.document.type.IType;
import com.probejs.formatter.NameResolver;
import com.probejs.info.MethodInfo;
import com.probejs.info.type.ITypeInfo;
import com.probejs.info.type.TypeInfoClass;
import com.probejs.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class FormatterMethod extends DocumentedFormatter<DocumentMethod> implements IFormatter {
    private final MethodInfo methodInfo;
    private boolean isInterface = false;

    private static String getCamelCase(String text) {
        return Character.toLowerCase(text.charAt(0)) + text.substring(1);
    }

    public FormatterMethod(MethodInfo methodInfo) {
        this.methodInfo = methodInfo;
    }

    public MethodInfo getMethodInfo() {
        return methodInfo;
    }

    public void setInterface(boolean anInterface) {
        isInterface = anInterface;
    }

    public String getBean() {
        String methodName = methodInfo.getName();
        if (methodName.equals("is") || methodName.equals("get") || methodName.equals("set"))
            return null;
        if (methodName.startsWith("is") && methodInfo.getParams().size() == 0 && (methodInfo.getReturnType().assignableFrom(new TypeInfoClass(Boolean.class)) || methodInfo.getReturnType().assignableFrom(new TypeInfoClass(Boolean.TYPE))))
            return getCamelCase(methodName.substring(2));
        if (methodName.startsWith("get") && methodInfo.getParams().size() == 0)
            return getCamelCase(methodName.substring(3));
        if (methodName.startsWith("set") && methodInfo.getParams().size() == 1)
            return getCamelCase(methodName.substring(3));
        return null;
    }

    public boolean isGetter() {
        return !methodInfo.getName().startsWith("set");
    }

    public String getBeanTypeString() {
        return isGetter() ? methodInfo.getReturnType().getTypeName() : methodInfo.getParams().get(0).getType().getTypeName();
    }

    private Pair<Map<String, IType>, IType> getModifiers() {
        Map<String, IType> modifiers = new HashMap<>();
        IType returns = null;
        if (document != null) {
            DocumentComment comment = document.getComment();
            if (comment != null) {
                modifiers.putAll(CommentUtil.getTypeModifiers(comment));
                CommentReturns r = comment.getSpecialComment(CommentReturns.class);
                if (r != null)
                    returns = r.getReturnType();
            }
        }
        return new Pair<>(modifiers, returns);
    }

    private static String formatTypeParameterized(ITypeInfo info) {
        StringBuilder sb = new StringBuilder(new FormatterType(info).format(0, 0));
        if (info instanceof TypeInfoClass clazz && !NameResolver.isTypeSpecial(clazz.getResolvedClass())) {
            if (clazz.getTypeVariables().size() != 0)
                sb.append("<%s>".formatted(String.join(", ", Collections.nCopies(clazz.getTypeVariables().size(), "any"))));
        }
        return sb.toString();
    }

    private String formatReturn() {
        return formatTypeParameterized(methodInfo.getReturnType());
    }

    private String formatMethodBody(IType returnModifier) {
        StringBuilder sb = new StringBuilder();
        if (methodInfo.isStatic() && !isInterface)
            sb.append("static ");
        sb.append(methodInfo.getName());
        if (methodInfo.getTypeVariables().size() != 0)
            sb.append("<%s>".formatted(methodInfo.getTypeVariables().stream().map(ITypeInfo::getTypeName).collect(Collectors.joining(", "))));
        sb.append("(%s)");
        sb.append(": %s".formatted(returnModifier == null ? formatReturn() : returnModifier.getTypeName()));
        return sb.toString();
    }

    private Set<String> formatParam(String name, Object type) {
        Set<String> results = new HashSet<>();
        String paramString = name + ": %s";
        if (type instanceof IType docType) {
            docType.getAssignableNames().stream().map(paramString::formatted).forEach(results::add);
        } else if (type instanceof ITypeInfo origType) {
            results.add(paramString.formatted(formatTypeParameterized(origType)));
            if (origType instanceof TypeInfoClass clazz) {
                List<IType> types = Manager.typesAssignable.getOrDefault(clazz.getTypeName(), new ArrayList<>());
                for (IType t : types) {
                    results.add(paramString.formatted(t.getTypeName()));
                }
            }
        }
        return results;
    }

    private List<String> formatMethods(Integer indent) {
        List<String> methods = new ArrayList<>();

        Pair<Map<String, IType>, IType> modifierPair = getModifiers();
        Map<String, IType> modifiers = modifierPair.getFirst();
        IType returnModifier = modifierPair.getSecond();

        Map<String, String> renames = new HashMap<>();
        if (document != null)
            renames.putAll(CommentUtil.getRenames(document.getComment()));

        List<MethodInfo.ParamInfo> params = methodInfo.getParams();
        List<Set<String>> paramTypes = new ArrayList<>();
        for (MethodInfo.ParamInfo param : params) {
            String paramOriginal = param.getName();
            String realName = renames.getOrDefault(paramOriginal, paramOriginal);
            paramTypes.add(formatParam(NameResolver.getNameSafe(realName), modifiers.containsKey(paramOriginal) ? modifiers.get(paramOriginal) : param.getType()));
        }

        String methodBody = formatMethodBody(returnModifier);
        Sets.cartesianProduct(paramTypes).forEach(p -> methods.add(" ".repeat(indent) + methodBody.formatted(String.join(", ", p))));
        return methods;
    }


    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        List<String> formatted = new ArrayList<>();

        if (document != null) {
            DocumentComment comment = document.getComment();
            if (CommentUtil.isHidden(comment))
                return formatted;
            if (comment != null)
                formatted.addAll(comment.format(indent, stepIndent));
        }

        formatted.addAll(formatMethods(indent));
        return formatted;
    }

    public List<String> formatBean(Integer indent, Integer stepIndent) {
        List<String> formatted = new ArrayList<>();

        String methodName = methodInfo.getName();
        Pair<Map<String, IType>, IType> modifierPair = getModifiers();
        Map<String, IType> paramModifiers = modifierPair.getFirst();
        IType returnModifier = modifierPair.getSecond();

        if (document != null) {
            DocumentComment comment = document.getComment();
            if (CommentUtil.isHidden(comment))
                return formatted;
            if (comment != null)
                formatted.addAll(comment.format(indent, stepIndent));
        }

        if (methodName.startsWith("is"))
            formatted.add(" ".repeat(indent) + "get %s(): boolean;".formatted(getBean()));
        if (methodName.startsWith("get"))
            formatted.add(" ".repeat(indent) + "get %s(): %s;".formatted(getBean(), returnModifier == null ? formatReturn() : returnModifier.getTypeName()));
        if (methodName.startsWith("set")) {
            MethodInfo.ParamInfo info = methodInfo.getParams().get(0);
            String name = info.getName();
            for (String paramString : formatParam(NameResolver.getNameSafe(name), paramModifiers.containsKey(name) ? paramModifiers.get(name) : info.getType()))
                formatted.add(" ".repeat(indent) + "set %s(%s);".formatted(getBean(), paramString));
        }
        return formatted;
    }
}
