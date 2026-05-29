package com.bariskokulu.crudgen.processor.generator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import com.bariskokulu.crudgen.util.Util;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

public class SecurityServiceInterfaceGenerator {
	
    public static void generate(ProcessingEnvironment processingEnv) {
        TypeSpec interfaceSpec = TypeSpec.interfaceBuilder("CrudGenSecurityService")
            .addModifiers(Modifier.PUBLIC)
            .addMethod(MethodSpec.methodBuilder("checkEntityAccess")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addParameter(String.class, "entityClassName")
                .addParameter(String.class, "method")
                .addParameter(ArrayTypeName.of(ClassName.get(Object.class)), "params")
                .varargs()
                .build())
            .addMethod(MethodSpec.methodBuilder("checkUseAccess")
                    .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                    .addParameter(String.class, "method")
                    .addParameter(ArrayTypeName.of(ClassName.get(Object.class)), "params")
                    .varargs()
                    .build())
            .build();
        Util.saveFile("com.bariskokulu.crudgen.security", interfaceSpec, processingEnv);
	}
	
}
