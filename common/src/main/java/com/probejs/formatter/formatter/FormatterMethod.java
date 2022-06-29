package com.probejs.formatter.formatter;

import com.probejs.document.DocumentComment;
import com.probejs.document.DocumentMethod;
import com.probejs.document.Manager;
import com.probejs.document.comment.CommentUtil;
import com.probejs.document.comment.special.CommentReturns;
import com.probejs.document.type.IType;
import com.probejs.document.type.TypeArray;
import com.probejs.document.type.TypeNamed;
import com.probejs.formatter.NameResolver;
import com.probejs.info.MethodInfo;
import com.probejs.info.type.ITypeInfo;
import com.probejs.info.type.TypeInfoArray;
import com.probejs.info.type.TypeInfoClass;
import com.probejs.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

public class FormatterMethod extends DocumentReceiver<DocumentMethod> implements IFormatter {
    private final MethodInfo methodInfo;

    private static String getCamelCase(String text) {
        return Character.toLowerCase(text.charAt(0)) + text.substring(1);
    }

    public FormatterMethod(MethodInfo methodInfo) {
        this.methodInfo = methodInfo;
    }

    public MethodInfo getMethodInfo() {
        return methodInfo;
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

    private static String formatTypeParameterized(ITypeInfo info, boolean useSpecial) {
        StringBuilder sb = new StringBuilder(new FormatterType(info, useSpecial).format(0, 0));
        if (info instanceof TypeInfoClass clazz && !NameResolver.isTypeSpecial(clazz.getResolvedClass())) {
            if (clazz.getTypeVariables().size() != 0)
                sb.append("<%s>".formatted(String.join(", ", Collections.nCopies(clazz.getTypeVariables().size(), "any"))));
        }
        return sb.toString();
    }

    private String formatReturn() {
        return formatTypeParameterized(methodInfo.getReturnType(), NameResolver.specialTypeGuards.getOrDefault(methodInfo.getReturnType().getResolvedClass(), false));
    }

    public static String formatParamUnderscore(ITypeInfo info) {
        StringBuilder sb = new StringBuilder(new FormatterType(info, false, (typeInfo, rawString) -> {
            if (typeInfo instanceof TypeInfoClass classInfo) {
                if (!NameResolver.resolvedPrimitives.contains(classInfo.getTypeName()))
                    return rawString + "_";
            }
            return rawString;
        }).format(0, 0));
        if (info instanceof TypeInfoClass clazz) {
            if (clazz.getTypeVariables().size() != 0)
                sb.append("<%s>".formatted(String.join(", ", Collections.nCopies(clazz.getTypeVariables().size(), "any"))));
        }
        return sb.toString();
    }

    private static String formatParamDoc(Map<String, IType> modifiers, MethodInfo.ParamInfo paramInfo) {
        IType type = modifiers.get(paramInfo.getName());
        return type.getTransformedName((t, s) -> {
            if (t instanceof TypeNamed n && NameResolver.resolvedNames.containsKey(n.getRawTypeName()) && !NameResolver.resolvedPrimitives.contains((n.getRawTypeName()))) {
                return s + "_";
            }
            return s;
        });
    }

    private String formatParams(Map<String, IType> modifiers, Map<String, String> renames) {
        return "(%s)".formatted(
                methodInfo
                        .getParams()
                        .stream()
                        .map(paramInfo -> "%s: %s".formatted(
                                (paramInfo.isVararg() ? "... " : "") + NameResolver.getNameSafe(renames.getOrDefault(paramInfo.getName(), paramInfo.getName())),
                                modifiers.containsKey(paramInfo.getName())
                                        ? formatParamDoc(modifiers, paramInfo)
                                        : formatParamUnderscore(paramInfo.getType())))
                        .collect(Collectors.joining(", ")));
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

        Pair<Map<String, IType>, IType> modifierPair = getModifiers();
        Map<String, IType> modifiers = modifierPair.getFirst();
        IType returnModifier = modifierPair.getSecond();
        Map<String, String> renames = new HashMap<>();
        if (document != null)
            renames.putAll(CommentUtil.getRenames(document.getComment()));

        StringBuilder sb = new StringBuilder();
        sb.append(" ".repeat(indent));
        if (methodInfo.isStatic())
            sb.append("static ");
        sb.append(methodInfo.getName());
        if (methodInfo.getTypeVariables().size() != 0)
            sb.append("<%s>".formatted(methodInfo.getTypeVariables().stream().map(ITypeInfo::getTypeName).collect(Collectors.joining(", "))));
        sb.append(formatParams(modifiers, renames));
        sb.append(": %s;".formatted(returnModifier != null ? returnModifier.getTypeName() : formatReturn()));

        formatted.add(sb.toString());
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
            formatted.add(" ".repeat(indent) + "set %s%s;".formatted(getBean(), formatParams(paramModifiers, new HashMap<>())));
        }
        return formatted;
    }
}
