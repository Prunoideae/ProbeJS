package com.probejs.info;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConstructorInfo {
    private List<MethodInfo.ParamInfo> params;

    public ConstructorInfo(Constructor<?> constructor) {
        this.params = Arrays.stream(constructor.getParameters()).map(MethodInfo.ParamInfo::new).collect(Collectors.toList());
    }

    public List<MethodInfo.ParamInfo> getParams() {
        return params;
    }

    public void setParams(List<MethodInfo.ParamInfo> params) {
        this.params = params;
    }
}
