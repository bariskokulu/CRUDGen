package com.bariskokulu.crudgen.processor.generator;

import java.util.List;

import javax.lang.model.element.Modifier;

import org.springframework.javapoet.AnnotationSpec;
import org.springframework.javapoet.ClassName;
import org.springframework.javapoet.FieldSpec;
import org.springframework.javapoet.MethodSpec;
import org.springframework.javapoet.ParameterSpec;
import org.springframework.javapoet.ParameterizedTypeName;
import org.springframework.javapoet.TypeSpec;

import com.bariskokulu.crudgen.processor.component.EntityElement;
import com.bariskokulu.crudgen.processor.component.FieldElement;
import com.bariskokulu.crudgen.util.TypeNames;
import com.bariskokulu.crudgen.util.Util;

public class EntityServiceGenerator {

	public static void generate(EntityElement element, Util util) {
		if(element.getControllerPath().isEmpty() || element.getServiceTypeName() == null) return;
		TypeSpec.Builder clazz = TypeSpec.classBuilder(element.getServiceName())
				.addAnnotation(AnnotationSpec.builder(TypeNames.SERVICE).build())
				.addModifiers(Modifier.PUBLIC);
		clazz.addField(FieldSpec.builder(element.getRepoTypeName(), "repo", Modifier.PRIVATE, Modifier.FINAL).build());
		clazz.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
				.addParameter(element.getRepoTypeName(), "repo")
				.addStatement("this.repo = repo")
				.build());
		clazz.addMethod(MethodSpec.methodBuilder("get")
				.addModifiers(Modifier.PUBLIC)
				.addParameter(element.getIdTypeName(), "id")
				.addStatement("return repo.findById(id).orElse(null)")
				.returns(element.getTypeName())
				.build());
		clazz.addMethod(MethodSpec.methodBuilder("get")
				.addModifiers(Modifier.PUBLIC)
				.addStatement("return repo.findAll()")
				.returns(ParameterizedTypeName.get(ClassName.get(List.class), element.getTypeName()))
				.build());
		clazz.addMethod(MethodSpec.methodBuilder("get")
				.addModifiers(Modifier.PUBLIC)
				.addParameter(TypeNames.PAGEABLE, "pageable")
				.addStatement("return repo.findAll(pageable)")
				.returns(ParameterizedTypeName.get(TypeNames.PAGE, element.getTypeName()))
				.build());
		clazz.addMethod(MethodSpec.methodBuilder("delete")
				.addModifiers(Modifier.PUBLIC)
				.addParameter(element.getIdTypeName(), "id")
				.addStatement("repo.deleteById(id)")
				.build());
		clazz.addMethod(MethodSpec.methodBuilder("save").addModifiers(Modifier.PUBLIC)
				.addParameter(ParameterSpec.builder(element.getTypeName(), "entity").build())
				.addStatement("return repo.save(entity)")
				.returns(element.getTypeName())
				.build());
		for(FieldElement field : element.getFields()) {
			if(field.isFindBy()) {
				clazz.addMethod(MethodSpec.methodBuilder("findBy"+field.getNameCapitalized())
						.addModifiers(Modifier.PUBLIC)
						.addParameter(ClassName.get(field.getType()), field.getName())
						.addStatement("return repo.findBy$L($L)", field.getNameCapitalized(), field.getName())
						.returns(element.getTypeName())
						.build());
			}
			if(field.isFindAllBy()) {
				clazz.addMethod(MethodSpec.methodBuilder("findAllBy"+field.getNameCapitalized())
						.addModifiers(Modifier.PUBLIC)
						.addParameter(ClassName.get(field.getType()), field.getName())
						.addStatement("return repo.findAllBy$L($L)", field.getNameCapitalized(), field.getName())
						.returns(ParameterizedTypeName.get(ClassName.get(List.class), element.getTypeName()))
						.build());
				clazz.addMethod(MethodSpec.methodBuilder("findAllBy"+field.getNameCapitalized())
						.addModifiers(Modifier.PUBLIC)
						.addParameter(ClassName.get(field.getType()), field.getName())
						.addParameter(TypeNames.PAGEABLE, "pageable")
						.addStatement("return repo.findAllBy$L($L, pageable)", field.getNameCapitalized(), field.getName())
						.returns(ParameterizedTypeName.get(TypeNames.PAGE, element.getTypeName()))
						.build());
			}
		}
		util.saveFile("com.bariskokulu.crudgen", clazz.build());
	}

}
