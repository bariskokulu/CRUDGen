package com.bariskokulu.crudgen.processor.generator;

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import com.bariskokulu.crudgen.util.Util;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

public class EntityLifecycleCallbacksInterfaceGenerator {

	public static void generate(ProcessingEnvironment processingEnv) {
		TypeVariableName t = TypeVariableName.get("T");
		ParameterizedTypeName listOfT = ParameterizedTypeName.get(ClassName.get(List.class), t);
		TypeSpec interfaceSpec = TypeSpec.interfaceBuilder("EntityLifecycleCallbacks")
				.addTypeVariable(t)
				.addModifiers(Modifier.PUBLIC)
				.addMethod(MethodSpec.methodBuilder("beforeCreate")
						.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
						.addParameter(t, "entity")
						.build())
				.addMethod(MethodSpec.methodBuilder("afterCreate")
						.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
						.addParameter(t, "entity")
						.build())
				.addMethod(MethodSpec.methodBuilder("beforeCreateBatch")
						.addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
						.addParameter(listOfT, "entities")
						.build())
				.addMethod(MethodSpec.methodBuilder("afterCreateBatch")
						.addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
						.addParameter(listOfT, "entities")
						.build())
				.addMethod(MethodSpec.methodBuilder("beforeUpdate")
						.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
						.addParameter(t, "existing")
						.build())
				.addMethod(MethodSpec.methodBuilder("afterUpdate")
						.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
						.addParameter(t, "saved")
						.build())
				.addMethod(MethodSpec.methodBuilder("beforeUpdateBatch")
						.addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
						.build())
				.addMethod(MethodSpec.methodBuilder("afterUpdateBatch")
						.addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
						.addParameter(listOfT, "saved")
						.build())
				.addMethod(MethodSpec.methodBuilder("beforeDelete")
						.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
						.addParameter(t, "entity")
						.build())
				.addMethod(MethodSpec.methodBuilder("afterDelete")
						.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
						.addParameter(t, "entity")
						.build())
				.addMethod(MethodSpec.methodBuilder("beforeDeleteBatch")
						.addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
						.addParameter(listOfT, "entities")
						.build())
				.addMethod(MethodSpec.methodBuilder("afterDeleteBatch")
						.addModifiers(Modifier.PUBLIC, Modifier.DEFAULT)
						.addParameter(listOfT, "entities")
						.build())
				.build();
		Util.saveFile("com.bariskokulu.crudgen.lifecycle", interfaceSpec, processingEnv);
	}

}
