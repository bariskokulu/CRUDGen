package com.bariskokulu.crudgen.processor.generator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import org.springframework.javapoet.AnnotationSpec;
import org.springframework.javapoet.ClassName;
import org.springframework.javapoet.FieldSpec;
import org.springframework.javapoet.MethodSpec;
import org.springframework.javapoet.ParameterSpec;
import org.springframework.javapoet.ParameterizedTypeName;
import org.springframework.javapoet.TypeSpec;

import com.bariskokulu.crudgen.processor.component.DTOElement;
import com.bariskokulu.crudgen.processor.component.EntityElement;
import com.bariskokulu.crudgen.processor.component.FieldElement;
import com.bariskokulu.crudgen.util.Util;

public class EntityControllerGenerator {

	public static void generate(EntityElement element, Util util) {
		if(element.getControllerPath().isEmpty()) return;
		TypeSpec.Builder clazz = TypeSpec.classBuilder(element.getControllerName())
				.addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.RestController")).build())
				.addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.RequestMapping")).addMember("value", "$S", element.getControllerPath()).build())
				.addModifiers(Modifier.PUBLIC);
		clazz.addField(FieldSpec.builder(element.getServiceTypeName(), "service", Modifier.PRIVATE, Modifier.FINAL).build());
		clazz.addField(FieldSpec.builder(element.getMapperTypeName(), "mapper", Modifier.PRIVATE, Modifier.FINAL).build());
		clazz.addField(FieldSpec.builder(ClassName.bestGuess("com.fasterxml.jackson.databind.ObjectMapper"), "objectMapper", Modifier.PRIVATE, Modifier.FINAL).build());
		clazz.addField(FieldSpec.builder(ClassName.bestGuess("jakarta.validation.Validator"), "validator", Modifier.PRIVATE, Modifier.FINAL).build());
		clazz.addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
				.addParameter(element.getServiceTypeName(), "service")
				.addParameter(element.getMapperTypeName(), "mapper")
				.addParameter(ClassName.bestGuess("com.fasterxml.jackson.databind.ObjectMapper"), "objectMapper")
				.addParameter(ClassName.bestGuess("jakarta.validation.Validator"), "validator")
				.addStatement("this.service = service")
				.addStatement("this.mapper = mapper")
				.addStatement("this.objectMapper = objectMapper")
				.addStatement("this.validator = validator")
				.build());
		ClassName returnDtoTypeName = element.getDtos().get("Read").getTypeName();
		clazz.addMethod(MethodSpec.methodBuilder("get")
				.addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.GetMapping")).addMember("value", "$S", "/{id}").build())
				.addModifiers(Modifier.PUBLIC)
				.addParameter(ParameterSpec.builder(element.getIdTypeName(), "id").addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.PathVariable")).build()).build())
				.addStatement("return $T.ok(mapper.get(service.get(id)))", ClassName.bestGuess("org.springframework.http.ResponseEntity"))
				.returns(ParameterizedTypeName.get(ClassName.bestGuess("org.springframework.http.ResponseEntity"), returnDtoTypeName))
				.build());
		clazz.addMethod(MethodSpec.methodBuilder("get")
				.addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.GetMapping")).addMember("value", "$S", "/").build())
				.addModifiers(Modifier.PUBLIC)
				.addStatement("return $T.ok(service.get().stream().map(t -> mapper.get(t)).collect($T.toList()))", ClassName.bestGuess("org.springframework.http.ResponseEntity"), ClassName.get(Collectors.class))
				.returns(ParameterizedTypeName.get(ClassName.bestGuess("org.springframework.http.ResponseEntity"), ParameterizedTypeName.get(ClassName.get(List.class), returnDtoTypeName)))
				.build());
		clazz.addMethod(MethodSpec.methodBuilder("get")
				.addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.GetMapping")).addMember("value", "$S", "/paged").build())
				.addModifiers(Modifier.PUBLIC)
				.addParameter(ParameterSpec.builder(ClassName.get(Integer.class).unbox(), "page").addAnnotation(ClassName.bestGuess("org.springframework.web.bind.annotation.RequestParam")).build())
				.addParameter(ParameterSpec.builder(ClassName.get(Integer.class).unbox(), "size").addAnnotation(ClassName.bestGuess("org.springframework.web.bind.annotation.RequestParam")).build())
				.addStatement("return $T.ok(service.get($T.of(page, size)).map(t -> mapper.get(t)))", ClassName.bestGuess("org.springframework.http.ResponseEntity"), ClassName.bestGuess("org.springframework.data.domain.PageRequest"))
				.returns(ParameterizedTypeName.get(ClassName.bestGuess("org.springframework.http.ResponseEntity"), ParameterizedTypeName.get(util.pageType, returnDtoTypeName)))
				.build());
		clazz.addMethod(MethodSpec.methodBuilder("delete")
				.addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.DeleteMapping")).addMember("value", "$S", "/{id}").build())
				.addModifiers(Modifier.PUBLIC)
				.addParameter(ParameterSpec.builder(element.getIdTypeName(), "id").addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.PathVariable")).build()).build())
				.addStatement("service.delete(id)")
				.addStatement("return $T.noContent().build()", ClassName.bestGuess("org.springframework.http.ResponseEntity"))
				.returns(ParameterizedTypeName.get(ClassName.bestGuess("org.springframework.http.ResponseEntity"), ClassName.get(Void.class)))
				.build());
		if(element.getDtos().containsKey("Create")) {
			clazz.addMethod(MethodSpec.methodBuilder("create").addModifiers(Modifier.PUBLIC)
					.addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.PostMapping")).build())
					.addParameter(ParameterSpec.builder(element.getDtos().get("Create").getTypeName(), "body").addAnnotation(ClassName.bestGuess("jakarta.validation.Valid")).addAnnotation(ClassName.bestGuess("org.springframework.web.bind.annotation.RequestBody")).build())
					.addStatement("return $T.ok(mapper.get(service.save(mapper.create(body))))", ClassName.bestGuess("org.springframework.http.ResponseEntity"))
					.returns(ParameterizedTypeName.get(ClassName.bestGuess("org.springframework.http.ResponseEntity"), returnDtoTypeName))
					.build());
		}
		if(element.getDtos().containsKey("Update")) {
			DTOElement dto = element.getDtos().get("Update");
			clazz.addMethod(MethodSpec.methodBuilder("update").addModifiers(Modifier.PUBLIC)
					.addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.PatchMapping")).addMember("value", "$S", "/{id}").addMember("consumes", "$S", "application/json-patch+json").build())
					.addParameter(ParameterSpec.builder(element.getIdTypeName(), "id").addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.PathVariable")).build()).build())
					.addParameter(ParameterSpec.builder(ClassName.bestGuess("com.fasterxml.jackson.databind.JsonNode"), "body").addAnnotation(ClassName.bestGuess("org.springframework.web.bind.annotation.RequestBody")).build())
					.addStatement("$T existing = service.get(id)", element.getTypeName())
					.addStatement("$T patchedDto", dto.getTypeName())
					.addCode("try {\n")
					.addStatement("patchedDto = objectMapper.treeToValue($T.apply(body, objectMapper.valueToTree(mapper.toPatch(existing))), $T.class)", ClassName.bestGuess("com.flipkart.zjsonpatch.JsonPatch"), dto.getTypeName())
					.addCode("} catch($T e) {\n", Exception.class)
					.addStatement("throw new $T(\"Invalid patch\", e)", IllegalArgumentException.class)
					.addCode("}\n")
					.addStatement("$T violations = validator.validate(patchedDto)", ParameterizedTypeName.get(ClassName.get(Set.class), ParameterizedTypeName.get(ClassName.bestGuess("jakarta.validation.ConstraintViolation"), dto.getTypeName())))
					.addStatement("if(!violations.isEmpty()) throw new $T(violations)", ClassName.bestGuess("jakarta.validation.ConstraintViolationException"))
					.addStatement("mapper.patch(existing, patchedDto)")
					.addStatement("return $T.ok(mapper.get(service.save(existing)))", ClassName.bestGuess("org.springframework.http.ResponseEntity"))
					.returns(ParameterizedTypeName.get(ClassName.bestGuess("org.springframework.http.ResponseEntity"), returnDtoTypeName))
					.build());
		}
		for(FieldElement field : element.getFields()) {
			if(field.isFindBy()) {
				clazz.addMethod(MethodSpec.methodBuilder("findBy"+field.getNameCapitalized())
						.addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.GetMapping")).addMember("value", "$S", "/findBy"+field.getNameCapitalized()).build())
						.addModifiers(Modifier.PUBLIC)
						.addParameter(ParameterSpec.builder(ClassName.get(field.getType()), field.getName()).addAnnotation(ClassName.bestGuess("org.springframework.web.bind.annotation.RequestParam")).build())
						.addStatement("return $T.ok(mapper.get(service.findBy$L($L)))", ClassName.bestGuess("org.springframework.http.ResponseEntity"), field.getNameCapitalized(), field.getName())
						.returns(ParameterizedTypeName.get(ClassName.bestGuess("org.springframework.http.ResponseEntity"), returnDtoTypeName))
						.build());
			}
			if(field.isFindAllBy()) {
				clazz.addMethod(MethodSpec.methodBuilder("findAllBy"+field.getNameCapitalized())
						.addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.GetMapping")).addMember("value", "$S", "/findAllBy"+field.getNameCapitalized()).build())
						.addModifiers(Modifier.PUBLIC)
						.addParameter(ParameterSpec.builder(ClassName.get(field.getType()), field.getName()).addAnnotation(ClassName.bestGuess("org.springframework.web.bind.annotation.RequestParam")).build())
						.addStatement("return $T.ok(service.findAllBy$L($L).stream().map(t -> mapper.get(t)).collect($T.toList()))", ClassName.bestGuess("org.springframework.http.ResponseEntity"), field.getNameCapitalized(), field.getName(), ClassName.get(Collectors.class))
						.returns(ParameterizedTypeName.get(ClassName.bestGuess("org.springframework.http.ResponseEntity"), ParameterizedTypeName.get(ClassName.get(List.class), returnDtoTypeName)))
						.build());
				clazz.addMethod(MethodSpec.methodBuilder("findAllBy"+field.getNameCapitalized())
						.addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation.GetMapping")).addMember("value", "$S", "/findAllBy"+field.getNameCapitalized()+"/paged").build())
						.addModifiers(Modifier.PUBLIC)
						.addParameter(ParameterSpec.builder(ClassName.get(field.getType()), field.getName()).addAnnotation(ClassName.bestGuess("org.springframework.web.bind.annotation.RequestParam")).build())
						.addParameter(ParameterSpec.builder(ClassName.get(Integer.class).unbox(), "page").addAnnotation(ClassName.bestGuess("org.springframework.web.bind.annotation.RequestParam")).build())
						.addParameter(ParameterSpec.builder(ClassName.get(Integer.class).unbox(), "size").addAnnotation(ClassName.bestGuess("org.springframework.web.bind.annotation.RequestParam")).build())
						.addStatement("return $T.ok(service.findAllBy$L($L, $T.of(page, size)).map(t -> mapper.get(t)))", ClassName.bestGuess("org.springframework.http.ResponseEntity"), field.getNameCapitalized(), field.getName(), ClassName.bestGuess("org.springframework.data.domain.PageRequest"))
						.returns(ParameterizedTypeName.get(ClassName.bestGuess("org.springframework.http.ResponseEntity"), ParameterizedTypeName.get(util.pageType, returnDtoTypeName)))
						.build());
			}
		}
		util.saveFile("com.bariskokulu.crudgen", clazz.build());
	}

}
