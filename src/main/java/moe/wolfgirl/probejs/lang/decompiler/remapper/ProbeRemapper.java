package moe.wolfgirl.probejs.lang.decompiler.remapper;

import dev.latvian.mods.rhino.mod.util.RhinoProperties;
import moe.wolfgirl.probejs.ProbeJS;
import org.jetbrains.java.decompiler.main.extern.IIdentifierRenamer;

import java.io.BufferedInputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

/**
 * Recreation of MinecraftRemapper to remap identifiers in decompiled
 * content.
 * <br>
 * Note that it directly reads from the expected space, since constructing
 * a remapper requires no arguments.
 */
public class ProbeRemapper implements IIdentifierRenamer {
    public final Map<String, String> srgClasses = new HashMap<>();
    public final Map<String, String> srgMethods = new HashMap<>();
    public final Map<String, String> srgFields = new HashMap<>();

    public ProbeRemapper() {
        var configPath = RhinoProperties.getGameDir().resolve("config/mm.jsmappings");

        if (Files.exists(configPath)) {
            try (var in = new BufferedInputStream(new GZIPInputStream(Objects.requireNonNull(Files.newInputStream(configPath))))) {
                this.loadFromRemapped(RemappedClass.loadFrom(in));
            } catch (Exception ex) {
                ProbeJS.LOGGER.error("Failed to load Rhino Minecraft remapper.");
            }
        } else {
            try (var in = new BufferedInputStream(new GZIPInputStream(Objects.requireNonNull(RhinoProperties.openResource("mm.jsmappings"))))) {
                this.loadFromRemapped(RemappedClass.loadFrom(in));
            } catch (Exception e) {
                ProbeJS.LOGGER.error("Failed to load Rhino Minecraft remapper.");
            }
        }
    }

    public void loadFromRemapped(Map<String, RemappedClass> remapped) {
        for (Map.Entry<String, RemappedClass> entry : remapped.entrySet()) {
            String name = entry.getKey();
            RemappedClass remappedClass = entry.getValue();

            if (!name.equals(remappedClass.remappedName)) srgClasses.put(name, remappedClass.remappedName);
            if (remappedClass.fields != null) {
                srgFields.putAll(remappedClass.fields);
            }
            if (remappedClass.emptyMethods != null) {
                for (Map.Entry<String, String> e : remappedClass.emptyMethods.entrySet()) {
                    String method = e.getKey();
                    String remappedMethod = e.getValue();

                    srgMethods.put(method + "(", remappedMethod);
                }
            }

            if (remappedClass.methods != null) {
                for (Map.Entry<String, String> e : remappedClass.methods.entrySet()) {
                    String method = e.getKey();
                    String remappedMethod = e.getValue();

                    srgMethods.put(method, remappedMethod);
                }
            }
        }
    }

    @Override
    public boolean toBeRenamed(Type elementType, String className, String element, String descriptor) {
        className = className.replace("/", ".");
        return switch (elementType) {
            case ELEMENT_CLASS -> srgClasses.containsKey(className);
            case ELEMENT_FIELD -> srgFields.containsKey(element);
            case ELEMENT_METHOD -> srgMethods.containsKey(element + descriptor.split("\\)", 2)[0]);
        };
    }

    @Override
    public String getNextClassName(String fullName, String shortName) {
        fullName = fullName.replace("/", ".");
        String[] parts = srgClasses.get(fullName).split("\\.");
        return parts[parts.length - 1];
    }

    @Override
    public String getNextFieldName(String className, String field, String descriptor) {
        return srgFields.get(field);
    }

    @Override
    public String getNextMethodName(String className, String method, String descriptor) {
        return srgMethods.get(method + descriptor.split("\\)", 2)[0]);
    }

}
