package com.bariskokulu.crudgen.processor.generator;

import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import com.bariskokulu.crudgen.processor.component.DTOElement;
import com.bariskokulu.crudgen.processor.component.DTOFieldElement;
import com.bariskokulu.crudgen.processor.component.EntityElement;
import com.bariskokulu.crudgen.util.TypeNames;
import com.bariskokulu.crudgen.util.Util;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

public class EntityDTOGenerator {

	public static void generate(EntityElement element, ProcessingEnvironment processingEnv) {
		for (Map.Entry<String, DTOElement> dto : element.getDtos().entrySet()) {
			generateOne(element, dto.getValue(), processingEnv);
		}
	}

	private static void generateOne(EntityElement element, DTOElement dtoElement, ProcessingEnvironment processingEnv) {
		TypeNames.init(processingEnv);
		if (Util.isBlank(element.getControllerPath())) {
			return;
		}
		TypeSpec.Builder clazz = TypeSpec.classBuilder(dtoElement.getName())
				.addModifiers(Modifier.PUBLIC);
		for (DTOFieldElement f : dtoElement.getFields()) {
			FieldSpec.Builder field = FieldSpec.builder(ClassName.get(f.getType()), f.getName(), Modifier.PRIVATE, Modifier.FINAL);
			for (AnnotationSpec validation : Util.beanValidationAnnotationSpecs(f.getElement())) {
				field.addAnnotation(validation);
			}
			clazz.addField(field.build());
		}
		MethodSpec.Builder ctor = MethodSpec.constructorBuilder()
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(TypeNames.JSON_CREATOR);
		for (DTOFieldElement f : dtoElement.getFields()) {
			ParameterSpec.Builder param = ParameterSpec.builder(ClassName.get(f.getType()), f.getName())
					.addAnnotation(AnnotationSpec.builder(TypeNames.JSON_PROPERTY)
							.addMember("value", "$S", f.getName())
							.build());
			ctor.addParameter(param.build());
			ctor.addStatement("this.$L = $L", f.getName(), f.getName());
		}
		clazz.addMethod(ctor.build());
		for (DTOFieldElement f : dtoElement.getFields()) {
			clazz.addMethod(MethodSpec.methodBuilder("get" + f.getNameCapitalized())
					.addModifiers(Modifier.PUBLIC)
					.returns(ClassName.get(f.getType()))
					.addStatement("return this.$L", f.getName())
					.build());
		}
		Util.saveFile(element.getPackageName(), clazz.build(), processingEnv);
	}

}
