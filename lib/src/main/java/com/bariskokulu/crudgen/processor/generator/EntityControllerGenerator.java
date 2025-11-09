package com.bariskokulu.crudgen.processor.generator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import com.bariskokulu.crudgen.processor.component.DTOElement;
import com.bariskokulu.crudgen.processor.component.EntityElement;
import com.bariskokulu.crudgen.processor.component.FieldElement;
import com.bariskokulu.crudgen.util.TypeNames;
import com.bariskokulu.crudgen.util.Util;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

public class EntityControllerGenerator {

	public static void generate(EntityElement element, Util util) {
		if(element.getControllerPath().isEmpty()) return;
		TypeSpec.Builder clazz = TypeSpec.classBuilder(element.getControllerName())
				.addAnnotation(AnnotationSpec.builder(TypeNames.REST_CONTROLLER).build())
				.addAnnotation(AnnotationSpec.builder(TypeNames.REQUEST_MAPPING).addMember("value", "$S", element.getControllerPath()).build())
				.addModifiers(Modifier.PUBLIC);
		clazz.addField(FieldSpec.builder(element.getServiceTypeName(), "service", Modifier.PRIVATE, Modifier.FINAL).build());
		clazz.addField(FieldSpec.builder(element.getMapperTypeName(), "mapper", Modifier.PRIVATE, Modifier.FINAL).build());
		clazz.addField(FieldSpec.builder(TypeNames.OBJECT_MAPPER, "objectMapper", Modifier.PRIVATE, Modifier.FINAL).build());
		clazz.addField(FieldSpec.builder(TypeNames.VALIDATOR, "validator", Modifier.PRIVATE, Modifier.FINAL).build());
		clazz.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
				.addParameter(element.getServiceTypeName(), "service")
				.addParameter(element.getMapperTypeName(), "mapper")
				.addParameter(TypeNames.OBJECT_MAPPER, "objectMapper")
				.addParameter(TypeNames.VALIDATOR, "validator")
				.addStatement("this.service = service")
				.addStatement("this.mapper = mapper")
				.addStatement("this.objectMapper = objectMapper")
				.addStatement("this.validator = validator")
				.build());
		ClassName returnDtoTypeName = element.getDtos().get("Read").getTypeName();
		
		clazz.addMethod(MethodSpec.methodBuilder("get")
				.addAnnotation(AnnotationSpec.builder(TypeNames.GET_MAPPING).addMember("value", "$S", "/{id}").build())
				.addModifiers(Modifier.PUBLIC)
				.addParameter(ParameterSpec.builder(element.getIdTypeName(), "id").addAnnotation(AnnotationSpec.builder(TypeNames.PATH_VARIABLE).build()).build())
				.addStatement("$T entity = service.get(id)", element.getTypeName())
				.addStatement("if(entity == null) throw new $T($T.NOT_FOUND, \"Entity with id \"+ id + \" not found.\")", TypeNames.RESOURCE_STATUS_EXCEPTION, TypeNames.HTTP_STATUS)
				.addStatement("return $T.ok(mapper.get(entity))", TypeNames.RESPONSE_ENTITY)
				.returns(ParameterizedTypeName.get(TypeNames.RESPONSE_ENTITY, returnDtoTypeName))
				.build());
		
		clazz.addMethod(MethodSpec.methodBuilder("get")
				.addAnnotation(AnnotationSpec.builder(TypeNames.GET_MAPPING).addMember("value", "$S", "/").build())
				.addModifiers(Modifier.PUBLIC)
				.addStatement("return $T.ok(service.get().stream().map(t -> mapper.get(t)).collect($T.toList()))", TypeNames.RESPONSE_ENTITY, ClassName.get(Collectors.class))
				.returns(ParameterizedTypeName.get(TypeNames.RESPONSE_ENTITY, ParameterizedTypeName.get(ClassName.get(List.class), returnDtoTypeName)))
				.build());
		
		clazz.addMethod(MethodSpec.methodBuilder("get")
				.addAnnotation(AnnotationSpec.builder(TypeNames.GET_MAPPING).addMember("value", "$S", "/paged").build())
				.addModifiers(Modifier.PUBLIC)
				.addParameter(ParameterSpec.builder(ClassName.get(Integer.class).unbox(), "page").addAnnotation(TypeNames.REQUEST_PARAM).addAnnotation(AnnotationSpec.builder(TypeNames.MIN).addMember("value", "0").build()).build())
				.addParameter(ParameterSpec.builder(ClassName.get(Integer.class).unbox(), "size").addAnnotation(TypeNames.REQUEST_PARAM).addAnnotation(AnnotationSpec.builder(TypeNames.MIN).addMember("value", "0").build()).build())
				.addStatement("return $T.ok(service.get($T.of(page, size)).map(t -> mapper.get(t)))", TypeNames.RESPONSE_ENTITY, TypeNames.PAGE_REQUEST)
				.returns(ParameterizedTypeName.get(TypeNames.RESPONSE_ENTITY, ParameterizedTypeName.get(TypeNames.PAGE, returnDtoTypeName)))
				.build());
		clazz.addMethod(MethodSpec.methodBuilder("delete")
				.addAnnotation(AnnotationSpec.builder(TypeNames.DELETE_MAPPING).addMember("value", "$S", "/{id}").build())
				.addModifiers(Modifier.PUBLIC)
				.addParameter(ParameterSpec.builder(element.getIdTypeName(), "id").addAnnotation(AnnotationSpec.builder(TypeNames.PATH_VARIABLE).build()).build())
				.addStatement("service.delete(id)")
				.addStatement("return $T.noContent().build()", TypeNames.RESPONSE_ENTITY)
				.returns(ParameterizedTypeName.get(TypeNames.RESPONSE_ENTITY, ClassName.get(Void.class)))
				.build());
		if(element.getDtos().containsKey("Create")) {
			clazz.addMethod(MethodSpec.methodBuilder("create").addModifiers(Modifier.PUBLIC)
					.addAnnotation(AnnotationSpec.builder(TypeNames.POST_MAPPING).build())
					.addParameter(ParameterSpec.builder(element.getDtos().get("Create").getTypeName(), "body").addAnnotation(TypeNames.VALID).addAnnotation(TypeNames.REQUEST_BODY).build())
					.addStatement("return $T.ok(mapper.get(service.save(mapper.create(body))))", TypeNames.RESPONSE_ENTITY)
					.returns(ParameterizedTypeName.get(TypeNames.RESPONSE_ENTITY, returnDtoTypeName))
					.build());
		}
		if(element.getDtos().containsKey("Update")) {
			DTOElement dto = element.getDtos().get("Update");
			clazz.addMethod(MethodSpec.methodBuilder("update").addModifiers(Modifier.PUBLIC)
					.addAnnotation(AnnotationSpec.builder(TypeNames.PATCH_MAPPING).addMember("value", "$S", "/{id}").addMember("consumes", "$S", "application/json-patch+json").build())
					.addParameter(ParameterSpec.builder(element.getIdTypeName(), "id").addAnnotation(AnnotationSpec.builder(TypeNames.PATH_VARIABLE).build()).build())
					.addParameter(ParameterSpec.builder(TypeNames.JSON_NODE, "body").addAnnotation(TypeNames.REQUEST_BODY).build())
					.addStatement("$T existing = service.get(id)", element.getTypeName())
					.addStatement("if(existing == null) throw new $T($T.NOT_FOUND, \"Entity with id \"+ id + \" not found.\")", TypeNames.RESOURCE_STATUS_EXCEPTION, TypeNames.HTTP_STATUS)
					.addStatement("$T patchedDto", dto.getTypeName())
					.addCode("try {\n")
					.addStatement("patchedDto = objectMapper.treeToValue($T.apply(body, objectMapper.valueToTree(mapper.toPatch(existing))), $T.class)", TypeNames.JSON_PATCH, dto.getTypeName())
					.addCode("} catch($T e) {\n", Exception.class)
					.addStatement("throw new $T(\"Invalid patch\", e)", IllegalArgumentException.class)
					.addCode("}\n")
					.addStatement("$T violations = validator.validate(patchedDto)", ParameterizedTypeName.get(ClassName.get(Set.class), ParameterizedTypeName.get(TypeNames.CONSTRAINT_VIOLATION, dto.getTypeName())))
					.addStatement("if(!violations.isEmpty()) throw new $T(violations)", TypeNames.CONSTRAINT_EXCEPTION)
					.addStatement("mapper.patch(existing, patchedDto)")
					.addStatement("return $T.ok(mapper.get(service.save(existing)))", TypeNames.RESPONSE_ENTITY)
					.returns(ParameterizedTypeName.get(TypeNames.RESPONSE_ENTITY, returnDtoTypeName))
					.build());
		}
		for(FieldElement field : element.getFields()) {
			if(field.isFindBy()) {
				clazz.addMethod(MethodSpec.methodBuilder("findBy"+field.getNameCapitalized())
						.addAnnotation(AnnotationSpec.builder(TypeNames.GET_MAPPING).addMember("value", "$S", "/findBy"+field.getNameCapitalized()).build())
						.addModifiers(Modifier.PUBLIC)
						.addParameter(ParameterSpec.builder(ClassName.get(field.getType()), field.getName()).addAnnotation(TypeNames.REQUEST_PARAM).build())
						.addStatement("return $T.ok(mapper.get(service.findBy$L($L)))", TypeNames.RESPONSE_ENTITY, field.getNameCapitalized(), field.getName())
						.returns(ParameterizedTypeName.get(TypeNames.RESPONSE_ENTITY, returnDtoTypeName))
						.build());
			}
			if(field.isFindAllBy()) {
				clazz.addMethod(MethodSpec.methodBuilder("findAllBy"+field.getNameCapitalized())
						.addAnnotation(AnnotationSpec.builder(TypeNames.GET_MAPPING).addMember("value", "$S", "/findAllBy"+field.getNameCapitalized()).build())
						.addModifiers(Modifier.PUBLIC)
						.addParameter(ParameterSpec.builder(ClassName.get(field.getType()), field.getName()).addAnnotation(TypeNames.REQUEST_PARAM).build())
						.addStatement("return $T.ok(service.findAllBy$L($L).stream().map(t -> mapper.get(t)).collect($T.toList()))", TypeNames.RESPONSE_ENTITY, field.getNameCapitalized(), field.getName(), ClassName.get(Collectors.class))
						.returns(ParameterizedTypeName.get(TypeNames.RESPONSE_ENTITY, ParameterizedTypeName.get(ClassName.get(List.class), returnDtoTypeName)))
						.build());
				clazz.addMethod(MethodSpec.methodBuilder("findAllBy"+field.getNameCapitalized())
						.addAnnotation(AnnotationSpec.builder(TypeNames.GET_MAPPING).addMember("value", "$S", "/findAllBy"+field.getNameCapitalized()+"/paged").build())
						.addModifiers(Modifier.PUBLIC)
						.addParameter(ParameterSpec.builder(ClassName.get(field.getType()), field.getName()).addAnnotation(TypeNames.REQUEST_PARAM).build())
						.addParameter(ParameterSpec.builder(ClassName.get(Integer.class).unbox(), "page").addAnnotation(TypeNames.REQUEST_PARAM).addAnnotation(AnnotationSpec.builder(TypeNames.MIN).addMember("value", "0").build()).build())
						.addParameter(ParameterSpec.builder(ClassName.get(Integer.class).unbox(), "size").addAnnotation(TypeNames.REQUEST_PARAM).addAnnotation(AnnotationSpec.builder(TypeNames.MIN).addMember("value", "0").build()).build())
						.addStatement("return $T.ok(service.findAllBy$L($L, $T.of(page, size)).map(t -> mapper.get(t)))", TypeNames.RESPONSE_ENTITY, field.getNameCapitalized(), field.getName(), TypeNames.PAGE_REQUEST)
						.returns(ParameterizedTypeName.get(TypeNames.RESPONSE_ENTITY, ParameterizedTypeName.get(TypeNames.PAGE, returnDtoTypeName)))
						.build());
			}
		}
		util.saveFile("com.bariskokulu.crudgen", clazz.build());
	}

}
