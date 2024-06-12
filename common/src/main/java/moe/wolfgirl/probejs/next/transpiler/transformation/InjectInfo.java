package moe.wolfgirl.probejs.next.transpiler.transformation;

import dev.latvian.mods.kubejs.typings.Info;
import moe.wolfgirl.probejs.next.java.clazz.Clazz;
import moe.wolfgirl.probejs.next.java.clazz.members.ConstructorInfo;
import moe.wolfgirl.probejs.next.java.clazz.members.FieldInfo;
import moe.wolfgirl.probejs.next.java.clazz.members.MethodInfo;
import moe.wolfgirl.probejs.next.typescript.code.member.ClassDecl;
import moe.wolfgirl.probejs.next.typescript.code.member.ConstructorDecl;
import moe.wolfgirl.probejs.next.typescript.code.member.FieldDecl;
import moe.wolfgirl.probejs.next.typescript.code.member.MethodDecl;

import java.util.HashMap;
import java.util.Map;

public class InjectInfo implements ClassTransformer {
    @Override
    public void transform(Clazz clazz, ClassDecl classDecl) {
        for (Info annotation : clazz.getAnnotations(Info.class)) {
            classDecl.addComment(annotation.value());
        }

        Map<String, MethodDecl> methodDecls = new HashMap<>();
        for (MethodDecl method : classDecl.methods) {
            methodDecls.put(method.name + method.params.size(), method);
        }

        for (MethodInfo method : clazz.methods) {
            for (Info annotation : method.getAnnotations(Info.class)) {
                var decl = methodDecls.get(method.name + method.params.size());
                decl.addComment(annotation.value());
            }
        }

        Map<String, FieldDecl> fieldDecls = new HashMap<>();
        for (FieldDecl field : classDecl.fields) {
            fieldDecls.put(field.name, field);
        }

        for (FieldInfo field : clazz.fields) {
            for (Info annotation : field.getAnnotations(Info.class)) {
                var decl = fieldDecls.get(field.name);
                decl.addComment(annotation.value());
            }
        }

        Map<Integer, ConstructorDecl> constructorDecls = new HashMap<>();
        for (ConstructorDecl constructor : classDecl.constructors) {
            constructorDecls.put(constructor.params.size(), constructor);
        }

        for (ConstructorInfo constructor : clazz.constructors) {
            for (Info annotation : constructor.getAnnotations(Info.class)) {
                var decl = constructorDecls.get(constructor.params.size());
                decl.addComment(annotation.value());
            }
        }
    }
}
