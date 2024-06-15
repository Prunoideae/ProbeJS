package moe.wolfgirl.probejs.utils;


import com.google.common.base.Suppliers;

import java.io.File;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Supplier;

public abstract class PlatformSpecial {
    public static Supplier<PlatformSpecial> INSTANCE = Suppliers.memoize(() -> {
        var serviceLoader = ServiceLoader.load(PlatformSpecial.class);
        return serviceLoader.findFirst().orElseThrow(() -> new RuntimeException("Could not find platform implementation for PlatformSpecial!"));
    });


    public List<File> getModFiles(){
        return null;
    }
}
