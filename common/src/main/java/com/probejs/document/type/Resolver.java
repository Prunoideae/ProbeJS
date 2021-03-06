package com.probejs.document.type;

import com.google.gson.JsonSyntaxException;
import com.probejs.ProbeJS;
import com.probejs.info.type.*;
import com.probejs.util.Pair;
import com.probejs.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Resolver {
    public static IType resolveType(String type) {
        type = type.strip();

        Pair<String, String> splitUnion = StringUtil.splitFirst(type, "<{", "}>", "|");
        if (splitUnion != null) {
            return new TypeUnion(resolveType(splitUnion.getFirst()), resolveType(splitUnion.getSecond()));
        }

        Pair<String, String> splitIntersection = StringUtil.splitFirst(type, "<{", "}>", "&");
        if (splitIntersection != null) {
            return new TypeIntersection(resolveType(splitIntersection.getFirst()), resolveType(splitIntersection.getSecond()));
        }

        if (type.endsWith("[]")) {
            return new TypeArray(resolveType(type.substring(0, type.length() - 2)));
        }

        if (type.startsWith("{")) {
            Map<String, IType> membersMap = new HashMap<>();
            ProbeJS.LOGGER.info(type);
            List<String> members = StringUtil.splitLayer(type.substring(1, type.length() - 1), "<{", "}>", ",");
            for (String member : members) {
                Pair<String, String> pair = StringUtil.splitFirst(member, "[", "]", ":");
                if (pair == null) {
                    ProbeJS.LOGGER.warn("Failed to parse member: %s".formatted(member));
                    continue;
                }
                String memberName = pair.getFirst().strip();
                IType memberType = resolveType(pair.getSecond().strip());
                if (memberName.startsWith("\""))
                    try {
                        memberName = ProbeJS.GSON.fromJson(memberName, String.class);
                    } catch (JsonSyntaxException e) {
                        ProbeJS.LOGGER.error(e.getMessage());
                    }
                membersMap.put(memberName, memberType);
            }
            return new TypeObject(membersMap);
        }

        if (type.endsWith(">")) {
            int indexLeft = type.indexOf("<");
            String rawType = type.substring(0, indexLeft);
            String typeParams = type.substring(indexLeft + 1, type.length() - 1);
            List<String> params = StringUtil.splitLayer(typeParams, "<{", "}>", ",");
            return new TypeParameterized(resolveType(rawType), params.stream().map(Resolver::resolveType).collect(Collectors.toList()));
        }
        return new TypeNamed(type);
    }

    public static boolean typeEquals(IType docType, ITypeInfo param) {
        if (docType instanceof TypeUnion || docType instanceof TypeIntersection)
            return false;
        if (docType instanceof TypeArray && param instanceof TypeInfoArray array)
            return typeEquals(((TypeArray) docType).getComponent(), array.getBaseType());
        if (docType instanceof TypeParameterized && param instanceof TypeInfoParameterized parameterized) {
            List<ITypeInfo> paramInfo = parameterized.getParamTypes();
            List<IType> paramDoc = ((TypeParameterized) docType).getParamTypes();
            if (paramDoc.size() != paramInfo.size())
                return false;
            for (int i = 0; i < paramDoc.size(); i++) {
                if (!typeEquals(paramDoc.get(i), paramInfo.get(i)))
                    return false;
            }
            return typeEquals(((TypeParameterized) docType).getRawType(), parameterized.getBaseType());
        }
        if (docType instanceof TypeNamed && (param instanceof TypeInfoVariable || param instanceof TypeInfoClass))
            return ((TypeNamed) docType).getRawTypeName().equals(param.getTypeName());

        return false;
    }
}
