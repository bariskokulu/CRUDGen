package com.bariskokulu.crudgen.processor.generator;

import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import com.bariskokulu.crudgen.processor.component.EntityElement;
import com.bariskokulu.crudgen.processor.component.FieldElement;
import com.bariskokulu.crudgen.util.TypeNames;
import com.bariskokulu.crudgen.util.Util;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

public class EntityRepositoryGenerator {

	public static void generate(EntityElement element, ProcessingEnvironment processingEnv) {
		TypeSpec.Builder clazz = TypeSpec.interfaceBuilder(element.getRepositoryName())
				.addAnnotation(AnnotationSpec.builder(TypeNames.REPOSITORY).build())
				.addModifiers(Modifier.PUBLIC);
		if(element.getCustomRepoTypeName() != null) {
			clazz.addSuperinterface(element.getCustomRepoTypeName());
		} else {
			switch(element.getRepoType()) {
			case JPA:
				clazz.addSuperinterface(ParameterizedTypeName.get(
						TypeNames.JPA_REPOSITORY,
						element.getTypeName(),
						element.getIdTypeName()
						))
				.addSuperinterface(ParameterizedTypeName.get(
						TypeNames.JPA_SPEC_EXECUTOR,
						element.getTypeName()
						));
				break;
			case MONGO:
				clazz.addSuperinterface(ParameterizedTypeName.get(
						TypeNames.MONGO_REPOSITORY,
						element.getTypeName()
						));
				break;
			}
		}
		for(FieldElement field : element.getFields()) {
			if(field.isFindBy()) {
				clazz.addMethod(MethodSpec.methodBuilder("findBy"+field.getNameCapitalized())
						.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
						.addParameter(ClassName.get(field.getType()), field.getName())
						.returns(element.getTypeName())
						.build());
			}
			if(field.isFindAllBy()) {
				clazz.addMethod(MethodSpec.methodBuilder("findAllBy"+field.getNameCapitalized())
						.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
						.addParameter(ClassName.get(field.getType()), field.getName())
						.returns(ParameterizedTypeName.get(ClassName.get(List.class), element.getTypeName()))
						.build());
				clazz.addMethod(MethodSpec.methodBuilder("findAllBy"+field.getNameCapitalized())
						.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
						.addParameter(ClassName.get(field.getType()), field.getName())
						.addParameter(TypeNames.PAGEABLE, "pageable")
						.returns(ParameterizedTypeName.get(TypeNames.PAGE, element.getTypeName()))
						.build());
			}
		}
		Util.saveFile(element.getPackageName(), clazz.build(), processingEnv);
	}

}
