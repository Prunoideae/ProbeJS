package moe.wolfgirl.next.typescript.parts.impl.members;

import moe.wolfgirl.next.typescript.parts.Code;

import java.util.List;

/**
 * Represents a method definition in TypeScript.
 * <br>
 * function something(a: number): string {} or
 * <br>
 * something(a: number): string;
 * <br>
 * Code can be added as comments in the method body. So decompiled Java code can be present here.
 * If left empty, the method will not have {} because we are generating .d.ts files.
 */
public class MethodDef extends Code {
    @Override
    public List<String> getContent() {
        return null;
    }
}
