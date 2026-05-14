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
            // ["foo": number] is not valid, must be [number, number, ...] or [foo: number, bar: string, ...]
            if (!param.isNameValid()) {
                throw new IllegalStateException("FixedArrayType param name must be valid, got: " + param.name);
            }
            sb.append(param.first()).append(", ");
        }
        sb.append("]");
        return List.of(sb.toString());
    }
}
