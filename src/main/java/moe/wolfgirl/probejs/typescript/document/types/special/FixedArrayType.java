package moe.wolfgirl.probejs.typescript.document.types.special;

import java.util.List;

public class FixedArrayType extends ObjectType {
    // [paramName: type, type, ...]
    public FixedArrayType(List<ParamType> params) {
        super(params);
    }

    @Override
    public List<String> format(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (ParamType param : params) {
            sb.append(param.first()).append(", ");
        }
        sb.append("]");
        return List.of(sb.toString());
    }
}
