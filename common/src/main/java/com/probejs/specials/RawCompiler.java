package com.probejs.specials;

import com.probejs.ProbePaths;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;

public class RawCompiler {
    //To be honest, this sucks
    public static void compileRaw() throws IOException {
        BufferedWriter writer = Files.newBufferedWriter(ProbePaths.GENERATED.resolve("raw.d.ts"));
        writer.close();
    }
}
