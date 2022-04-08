package com.probejs.document;

import com.google.common.collect.Sets;
import com.probejs.document.parser.processor.IDocumentProvider;
import com.probejs.document.type.IType;
import com.probejs.document.type.Resolver;
import com.probejs.formatter.formatter.IFormatter;
import com.probejs.info.MethodInfo;
import com.probejs.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

public class DocumentMethod extends DocumentProperty implements IDocumentProvider<DocumentMethod>, IFormatter {
    @Override
    public DocumentMethod provide() {
        return this;
    }

    public String getMethodBody() {
        StringBuilder sb = new StringBuilder();
        if (isStatic)
            sb.append("static ");
        sb.append(name);
        sb.append("(%s)");
        sb.append(": %s;".formatted(returnType.getTypeName()));
        return sb.toString();
    }

    private static Set<String> getParamSet(DocumentParam param) {
        String paramBody = param.name + ": %s";
        return param.type.getAssignableNames().stream().map(paramBody::formatted).collect(Collectors.toSet());
    }

    @Override
    public List<String> format(Integer indent, Integer stepIndent) {
        List<String> formatted = new ArrayList<>();
        if (comment != null)
            formatted.addAll(comment.format(indent, stepIndent));
        String methodBody = getMethodBody();
        List<Set<String>> paramSet = getParams().stream().map(DocumentMethod::getParamSet).collect(Collectors.toList());
        for (List<String> paramsFormatted : Sets.cartesianProduct(paramSet)) {
            formatted.add(" ".repeat(indent) + methodBody.formatted(String.join(", ", paramsFormatted)));
        }
        return formatted;
    }

    private static class DocumentParam {
        private final String name;
        private final IType type;

        private DocumentParam(String name, IType type) {
            this.name = name;
            this.type = type;
        }

        public IType getType() {
            return type;
        }

        public String getName() {
            return name;
        }
    }

    private final boolean isStatic;
    private final String name;
    private final IType returnType;
    private final List<DocumentParam> params;

    public DocumentMethod(String line) {
        line = line.strip();
        if (line.startsWith("static")) {
            line = line.substring(6).strip();
            isStatic = true;
        } else {
            isStatic = false;
        }
        int nameIndex = line.indexOf("(");
        int methodIndex = line.indexOf(")");

        name = line.substring(0, nameIndex).strip();
        String paramsString = line.substring(nameIndex + 1, methodIndex);
        String remainedString = line.substring(methodIndex + 1).replace(":", "").strip();
        params = StringUtil.splitLayer(paramsString, "<", ">", ",")
                .stream()
                .map(String::strip)
                .filter(s -> !s.isEmpty()).map(s -> {
                    String[] nameType = s.split(":");
                    return new DocumentParam(nameType[0].strip(), Resolver.resolveType(nameType[1]));
                })
                .collect(Collectors.toList());
        returnType = Resolver.resolveType(remainedString);
    }

    public boolean isStatic() {
        return isStatic;
    }

    public String getName() {
        return name;
    }

    public IType getReturnType() {
        return returnType;
    }

    public List<DocumentParam> getParams() {
        return params;
    }

    public boolean testMethod(MethodInfo methodInfo) {
        if (methodInfo.isStatic() != isStatic)
            return false;

        if (!Objects.equals(methodInfo.getName(), name))
            return false;

        Map<String, MethodInfo.ParamInfo> params = new HashMap<>();
        Map<String, DocumentParam> docParams = new HashMap<>();
        methodInfo.getParams().forEach(p -> params.put(p.getName(), p));
        this.params.forEach(p -> docParams.put(p.name, p));

        if (!params.keySet().equals(docParams.keySet()))
            return false;

        if (!Resolver.typeEquals(returnType, methodInfo.getReturnType()))
            return false;

        for (Map.Entry<String, MethodInfo.ParamInfo> e : params.entrySet()) {
            DocumentParam doc = docParams.get(e.getKey());
            if (!Resolver.typeEquals(doc.getType(), e.getValue().getType()))
                return false;
        }

        return true;
    }
}
