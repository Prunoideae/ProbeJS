package moe.wolfgirl.probejs.next.transpiler.transformation;

import moe.wolfgirl.probejs.next.java.clazz.Clazz;
import moe.wolfgirl.probejs.next.typescript.code.member.ClassDecl;

/**
 * Accepts a Clazz and a transpiled TS file, modify the
 * file to respect some stuffs.
 */
public interface ClassTransformer {
    ClassTransformer[] CLASS_TRANSFORMERS = new ClassTransformer[]{
            new InjectInfo(),
            new InjectArray(),
            new InjectBeans(),
    };

    static void transformClass(Clazz clazz, ClassDecl classDecl) {
        for (ClassTransformer classTransformer : CLASS_TRANSFORMERS) {
            classTransformer.transform(clazz, classDecl);
        }
    }

    void transform(Clazz clazz, ClassDecl classDecl);
}
