package moe.wolfgirl.probejs.docs;

import moe.wolfgirl.probejs.lang.typescript.ScriptDump;
import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin;
import moe.wolfgirl.probejs.lang.transpiler.TypeConverter;
import moe.wolfgirl.probejs.lang.typescript.Declaration;
import moe.wolfgirl.probejs.lang.typescript.code.Code;
import moe.wolfgirl.probejs.lang.typescript.code.type.js.JSPrimitiveType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;

import java.util.Collection;
import java.util.List;

public class Primitives extends ProbeJSPlugin {
    public static final JSPrimitiveType LONG = Types.primitive("long");
    public static final JSPrimitiveType INTEGER = Types.primitive("integer");
    public static final JSPrimitiveType SHORT = Types.primitive("short");
    public static final JSPrimitiveType BYTE = Types.primitive("byte");
    public static final JSPrimitiveType DOUBLE = Types.primitive("double");
    public static final JSPrimitiveType FLOAT = Types.primitive("float");
    public static final JSPrimitiveType CHARACTER = Types.primitive("character");
    public static final JSPrimitiveType CHAR_SEQUENCE = Types.primitive("charseq");


    static class JavaPrimitive extends Code {
        private final String javaPrimitive;
        private final String jsInterface;

        JavaPrimitive(String javaPrimitive, String jsInterface) {
            this.javaPrimitive = javaPrimitive;
            this.jsInterface = jsInterface;
        }

        @Override
        public Collection<ClassPath> getUsedClassPaths() {
            return List.of();
        }

        @Override
        public List<String> format(Declaration declaration) {
            return List.of("interface %s extends %s {}".formatted(javaPrimitive, jsInterface));
        }

        static JavaPrimitive of(String javaPrimitive, String jsInterface) {
            return new JavaPrimitive(javaPrimitive, jsInterface);
        }
    }

    @Override
    public void addPredefinedTypes(TypeConverter converter) {
        converter.addType(Object.class, Types.ANY);

        converter.addType(String.class, Types.STRING);
        converter.addType(CharSequence.class, CHAR_SEQUENCE);
        converter.addType(Character.class, CHARACTER);
        converter.addType(Character.TYPE, CHARACTER);

        converter.addType(Void.class, Types.VOID);
        converter.addType(Void.TYPE, Types.VOID);

        converter.addType(Long.class, LONG);
        converter.addType(Long.TYPE, LONG);
        converter.addType(Integer.class, INTEGER);
        converter.addType(Integer.TYPE, INTEGER);
        converter.addType(Short.class, SHORT);
        converter.addType(Short.TYPE, SHORT);
        converter.addType(Byte.class, BYTE);
        converter.addType(Byte.TYPE, BYTE);
        converter.addType(Number.class, Types.NUMBER);
        converter.addType(Double.class, DOUBLE);
        converter.addType(Double.TYPE, DOUBLE);
        converter.addType(Float.class, FLOAT);
        converter.addType(Float.TYPE, FLOAT);

        converter.addType(Boolean.class, Types.BOOLEAN);
        converter.addType(Boolean.TYPE, Types.BOOLEAN);
    }

    @Override
    public void addGlobals(ScriptDump scriptDump) {
        scriptDump.addGlobal("primitives",
                JavaPrimitive.of("long", "Number"),
                JavaPrimitive.of("integer", "Number"),
                JavaPrimitive.of("short", "Number"),
                JavaPrimitive.of("byte", "Number"),
                JavaPrimitive.of("double", "Number"),
                JavaPrimitive.of("float", "Number"),
                JavaPrimitive.of("character", "String"),
                JavaPrimitive.of("charseq", "String")
        );
    }
}
