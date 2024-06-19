package moe.wolfgirl.probejs.lang.transpiler.transformation;

import moe.wolfgirl.probejs.lang.java.clazz.Clazz;
import moe.wolfgirl.probejs.lang.java.clazz.members.ConstructorInfo;
import moe.wolfgirl.probejs.lang.java.clazz.members.FieldInfo;
import moe.wolfgirl.probejs.lang.java.clazz.members.MethodInfo;
import moe.wolfgirl.probejs.lang.typescript.code.member.ClassDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.ConstructorDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.FieldDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.MethodDecl;

/**
 * Accepts a Clazz and a transpiled TS file, modify the
 * file to respect some stuffs.
 */
public interface ClassTransformer {
    ClassTransformer[] CLASS_TRANSFORMERS = new ClassTransformer[]{
            new InjectAnnotation(),
            new InjectArray(),
            new InjectBeans(),
    };

    static void transformClass(Clazz clazz, ClassDecl classDecl) {
        for (ClassTransformer classTransformer : CLASS_TRANSFORMERS) {
            classTransformer.transform(clazz, classDecl);
        }
    }

    static void transformMethods(MethodInfo methodInfo, MethodDecl methodDecl) {
        for (ClassTransformer classTransformer : CLASS_TRANSFORMERS) {
            classTransformer.transformMethod(methodInfo, methodDecl);
        }
    }

    static void transformConstructors(ConstructorInfo constructorInfo, ConstructorDecl constructorDecl) {
        for (ClassTransformer classTransformer : CLASS_TRANSFORMERS) {
            classTransformer.transformConstructor(constructorInfo, constructorDecl);
        }
    }

    static void transformFields(FieldInfo fieldInfo, FieldDecl fieldDecl) {
        for (ClassTransformer classTransformer : CLASS_TRANSFORMERS) {
            classTransformer.transformField(fieldInfo, fieldDecl);
        }
    }

    void transform(Clazz clazz, ClassDecl classDecl);

    default void transformMethod(MethodInfo methodInfo, MethodDecl methodDecl) {

    }

    default void transformConstructor(ConstructorInfo constructorInfo, ConstructorDecl constructorDecl) {

    }

    default void transformField(FieldInfo fieldInfo, FieldDecl fieldDecl) {

    }
}
