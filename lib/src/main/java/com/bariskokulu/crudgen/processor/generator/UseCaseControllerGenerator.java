package com.bariskokulu.crudgen.processor.generator;

import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import com.bariskokulu.crudgen.processor.component.EndpointElement;
import com.bariskokulu.crudgen.processor.component.ParameterElement;
import com.bariskokulu.crudgen.processor.component.UseCaseServiceElement;
import com.bariskokulu.crudgen.util.TypeNames;
import com.bariskokulu.crudgen.util.Util;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

public class UseCaseControllerGenerator {

	public static void generate(UseCaseServiceElement service, Util util) {
		TypeSpec.Builder clazz = TypeSpec.classBuilder(service.getControllerName() != null && !service.getControllerName().isEmpty() ? service.getControllerName() : service.getName()+"Controller")
				.addAnnotation(AnnotationSpec.builder(TypeNames.CONTROLLER).build())
				.addAnnotation(AnnotationSpec.builder(TypeNames.REQUEST_MAPPING).addMember("value", "$S", service.getPath()).build())
				.addModifiers(Modifier.PUBLIC);
		clazz.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).addParameter(service.getTypeName(), "service").addStatement("this.service = service").build());
		clazz.addField(FieldSpec.builder(service.getTypeName(), "service", Modifier.PRIVATE, Modifier.FINAL).build());
		for(EndpointElement endpoint : service.getEndpoints()) {
			MethodSpec.Builder method = MethodSpec.methodBuilder(endpoint.getName())
					.addModifiers(Modifier.PUBLIC);
			StringBuilder paramString = new StringBuilder();
			for(ParameterElement param : endpoint.getParams()) {
				ParameterSpec.Builder paramBuilder = ParameterSpec.builder(ClassName.get(param.getType()), param.getName());
				paramBuilder.addAnnotations(param.getElement().getAnnotationMirrors().stream().map(AnnotationSpec::get).collect(Collectors.toList()));
				method.addParameter(paramBuilder.build());
				paramString.append(", "+param.getName());
			}
			if(endpoint.getReturnType() != null) {
				method.addStatement("return $T.ok(service.$L($L))", TypeNames.RESPONSE_ENTITY, endpoint.getName(), paramString.append("  ").substring(2));
				method.returns(ParameterizedTypeName.get(TypeNames.RESPONSE_ENTITY, ClassName.get(endpoint.getReturnType())));
			} else {
				method.addStatement("service.$L($L)", endpoint.getName(), paramString.append("  ").substring(2));
			}
			method.addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation."+endpoint.getHttpMethod().text()+"Mapping")).addMember("value", "$S", endpoint.getPath()).build());
			clazz.addMethod(method.build());
		}
		util.saveFile("com.bariskokulu.crudgen", clazz.build());
	}

}
