package com.bariskokulu.crudgen.processor.generator;

import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import com.bariskokulu.crudgen.processor.component.DTOElement;
import com.bariskokulu.crudgen.processor.component.EntityElement;
import com.bariskokulu.crudgen.util.Util;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

public class EntityDTOGenerator {

	public static void generate(EntityElement element, ProcessingEnvironment processingEnv) {
		for(Map.Entry<String, DTOElement> dto : element.getDtos().entrySet()) {
			generateOne(element, dto.getValue(), processingEnv);
		}
	}

	private static void generateOne(EntityElement element, DTOElement dtoElement, ProcessingEnvironment processingEnv) {
		TypeSpec.Builder clazz = TypeSpec.classBuilder(dtoElement.getName())
				.addModifiers(Modifier.PUBLIC);
		clazz.addFields(dtoElement.getFields().stream().map(t -> FieldSpec.builder(ClassName.get(t.getType()), t.getName(), Modifier.PRIVATE, Modifier.FINAL).addAnnotations(t.getElement().getAnnotationMirrors().stream().filter(a -> !((TypeElement)a.getAnnotationType().asElement()).getQualifiedName().toString().startsWith("com.bariskokulu.crudgen")).map(AnnotationSpec::get).collect(Collectors.toList())).build()).collect(Collectors.toList()));
		MethodSpec.Builder constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
				.addParameters(dtoElement.getFields().stream().map(t -> ParameterSpec.builder(ClassName.get(t.getType()), t.getName()).build()).collect(Collectors.toList()));
		dtoElement.getFields().forEach(t -> constructor.addStatement("this."+t.getName()+" = "+t.getName()));
		clazz.addMethod(constructor.build());
		Util.saveFile(element.getPackageName(), clazz.build(), processingEnv);
		//			TypeSpec.Builder clazz = TypeSpec.recordBuilder(dtoElement.getName())
		//					.addModifiers(Modifier.PUBLIC);
		//			clazz.recordConstructor(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
		//					.addParameters(dtoElement.getFields().stream().map(t -> ParameterSpec.builder(ClassName.get(t.getType()), t.getName()).addAnnotations(t.getElement().getAnnotationMirrors().stream().filter(a -> !((TypeElement)a.getAnnotationType().asElement()).getQualifiedName().toString().startsWith("com.bariskokulu.crudgen")).map(AnnotationSpec::get).collect(Collectors.toList())).build()).collect(Collectors.toList()))
		//					.build());
		//			util.saveFile("com.bariskokulu.crudgen", clazz.build());
	}

}
