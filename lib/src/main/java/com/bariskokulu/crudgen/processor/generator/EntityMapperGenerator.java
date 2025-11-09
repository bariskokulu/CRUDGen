package com.bariskokulu.crudgen.processor.generator;


import javax.lang.model.element.Modifier;

import com.bariskokulu.crudgen.processor.component.DTOElement;
import com.bariskokulu.crudgen.processor.component.EntityElement;
import com.bariskokulu.crudgen.util.TypeNames;
import com.bariskokulu.crudgen.util.Util;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

public class EntityMapperGenerator {

	public static void generate(EntityElement element, Util util) {
		TypeSpec.Builder clazz = TypeSpec.interfaceBuilder(element.getMapperName())
				.addAnnotation(AnnotationSpec.builder(TypeNames.MAPPER).build())
				.addModifiers(Modifier.PUBLIC);
		clazz.addMethod(MethodSpec.methodBuilder("get").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addParameter(element.getTypeName(), "entity")
				.returns(element.getDtos().get("Read").getTypeName())
				.build());
		if(element.getDtos().containsKey("Create")) {
			clazz.addMethod(MethodSpec.methodBuilder("create").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
					.addParameter(element.getDtos().get("Create").getTypeName(), "dto")
					.returns(element.getTypeName())
					.build());
		}
		if(element.getDtos().containsKey("Update")) {
			DTOElement dto = element.getDtos().get("Update");
			clazz.addMethod(MethodSpec.methodBuilder("toPatch").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
					.addParameter(element.getTypeName(), "entity")
					.returns(dto.getTypeName())
					.build());
			clazz.addMethod(MethodSpec.methodBuilder("patch").addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
					.addAnnotation(AnnotationSpec.builder(TypeNames.BEAN_MAPPING).addMember("nullValuePropertyMappingStrategy", "$T.$L", TypeNames.NULL_STRATEGY, "SET_TO_NULL").build())
					.addParameter(ParameterSpec.builder(element.getTypeName(), "entity").addAnnotation(AnnotationSpec.builder(TypeNames.MAPPING_TARGET).build()).build())
					.addParameter(dto.getTypeName(), "dto")
					.build());
		}
		util.saveFile("com.bariskokulu.crudgen", clazz.build());
	}

}
