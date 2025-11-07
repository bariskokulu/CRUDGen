package com.bariskokulu.crudgen.processor.generator;

import java.util.Map;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import org.springframework.javapoet.AnnotationSpec;
import org.springframework.javapoet.ClassName;
import org.springframework.javapoet.FieldSpec;
import org.springframework.javapoet.MethodSpec;
import org.springframework.javapoet.ParameterSpec;
import org.springframework.javapoet.TypeSpec;

import com.bariskokulu.crudgen.processor.component.DTOElement;
import com.bariskokulu.crudgen.processor.component.EntityElement;
import com.bariskokulu.crudgen.util.Util;

public class EntityDTOGenerator {

	public static void generate(EntityElement element, Util util) {
		for(Map.Entry<String, DTOElement> dto : element.getDtos().entrySet()) {
			generateOne(dto.getValue(), util);
		}
	}

	private static void generateOne(DTOElement dtoElement, Util util) {
		TypeSpec.Builder clazz = TypeSpec.classBuilder(dtoElement.getName())
				.addModifiers(Modifier.PUBLIC);
		clazz.addFields(dtoElement.getFields().stream().map(t -> FieldSpec.builder(ClassName.get(t.getType()), t.getName(), Modifier.PRIVATE, Modifier.FINAL).addAnnotations(t.getElement().getAnnotationMirrors().stream().filter(a -> !((TypeElement)a.getAnnotationType().asElement()).getQualifiedName().toString().startsWith("com.bariskokulu.crudgen")).map(AnnotationSpec::get).collect(Collectors.toList())).build()).collect(Collectors.toList()));
		MethodSpec.Builder constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
				.addParameters(dtoElement.getFields().stream().map(t -> ParameterSpec.builder(ClassName.get(t.getType()), t.getName()).build()).collect(Collectors.toList()));
		dtoElement.getFields().forEach(t -> constructor.addStatement("this."+t.getName()+" = "+t.getName()));
		clazz.addMethod(constructor.build());
		util.saveFile("com.bariskokulu.crudgen", clazz.build());
		//			TypeSpec.Builder clazz = TypeSpec.recordBuilder(dtoElement.getName())
		//					.addModifiers(Modifier.PUBLIC);
		//			clazz.recordConstructor(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
		//					.addParameters(dtoElement.getFields().stream().map(t -> ParameterSpec.builder(ClassName.get(t.getType()), t.getName()).addAnnotations(t.getElement().getAnnotationMirrors().stream().filter(a -> !((TypeElement)a.getAnnotationType().asElement()).getQualifiedName().toString().startsWith("com.bariskokulu.crudgen")).map(AnnotationSpec::get).collect(Collectors.toList())).build()).collect(Collectors.toList()))
		//					.build());
		//			util.saveFile("com.bariskokulu.crudgen", clazz.build());
	}

}
