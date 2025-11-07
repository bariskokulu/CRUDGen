package com.bariskokulu.crudgen.processor.generator;

import java.util.List;

import javax.lang.model.element.Modifier;

import org.springframework.javapoet.AnnotationSpec;
import org.springframework.javapoet.ClassName;
import org.springframework.javapoet.MethodSpec;
import org.springframework.javapoet.ParameterizedTypeName;
import org.springframework.javapoet.TypeSpec;

import com.bariskokulu.crudgen.processor.component.EntityElement;
import com.bariskokulu.crudgen.processor.component.FieldElement;
import com.bariskokulu.crudgen.util.Util;

public class EntityRepositoryGenerator {

	public static void generate(EntityElement entity, Util util) {
		TypeSpec.Builder repoClass = TypeSpec.interfaceBuilder(entity.getRepositoryName())
				.addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("org.springframework.stereotype.Repository")).build())
				.addModifiers(Modifier.PUBLIC);
		switch(entity.getRepoType()) {
		case JPA:
			repoClass.addSuperinterface(ParameterizedTypeName.get(
					ClassName.bestGuess("org.springframework.data.jpa.repository.JpaRepository"),
					entity.getTypeName(),
					entity.getIdTypeName()
					))
			.addSuperinterface(ParameterizedTypeName.get(
					ClassName.bestGuess("org.springframework.data.jpa.repository.JpaSpecificationExecutor"),
					entity.getTypeName()
					));
			break;
		case MONGO:
			repoClass.addSuperinterface(ParameterizedTypeName.get(
					ClassName.bestGuess("org.springframework.data.mongodb.repository.MongoRepository"),
					entity.getTypeName()
					));
			break;
		}
		for(FieldElement field : entity.getFields()) {
			if(field.isFindBy()) {
				repoClass.addMethod(MethodSpec.methodBuilder("findBy"+field.getNameCapitalized())
						.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
						.addParameter(ClassName.get(field.getType()), field.getName())
						.returns(entity.getTypeName())
						.build());
			}
			if(field.isFindAllBy()) {
				repoClass.addMethod(MethodSpec.methodBuilder("findAllBy"+field.getNameCapitalized())
						.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
						.addParameter(ClassName.get(field.getType()), field.getName())
						.returns(ParameterizedTypeName.get(ClassName.get(List.class), entity.getTypeName()))
						.build());
				repoClass.addMethod(MethodSpec.methodBuilder("findAllBy"+field.getNameCapitalized())
						.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
						.addParameter(ClassName.get(field.getType()), field.getName())
						.addParameter(ClassName.bestGuess("org.springframework.data.domain.Pageable"), "pageable")
						.returns(ParameterizedTypeName.get(util.pageType, entity.getTypeName()))
						.build());
			}
		}
		util.saveFile("com.bariskokulu.crudgen", repoClass.build());
	}

}
