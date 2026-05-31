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
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

public class EntityServiceGenerator {

	public static void generate(EntityElement element, ProcessingEnvironment processingEnv) {
		if(element.getServiceTypeName() == null) return;
		if(element.getCustomServiceTypeName() != null) return;
		TypeSpec.Builder clazz = TypeSpec.classBuilder(element.getServiceName())
				.addAnnotation(AnnotationSpec.builder(TypeNames.SERVICE).build())
				.addModifiers(Modifier.PUBLIC);
		if(element.getExtendServiceTypeName() != null) {
			clazz.addSuperinterface(element.getExtendServiceTypeName());
		}
		clazz.addField(FieldSpec.builder(element.getEffectiveRepoTypeName(), "repo", Modifier.PRIVATE, Modifier.FINAL).build());
		if(element.isLogging()) {
			clazz.addField(FieldSpec.builder(TypeNames.LOGGER, "logger", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer("$T.getLogger($T.class)", TypeNames.LOGGER_FACTORY, element.getServiceTypeName()).build());
		}
		MethodSpec.Builder constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
				.addParameter(element.getEffectiveRepoTypeName(), "repo")
				.addStatement("this.repo = repo");
		clazz.addMethod(constructor.build());
		clazz.addMethod(MethodSpec.methodBuilder("get")
				.addModifiers(Modifier.PUBLIC)
				.addParameter(element.getIdTypeName(), "id")
				.addCode(checkThenAddLog(element, "get() called with id: {}", "id"))
				.addStatement("$T entity = repo.findById(id).orElse(null)", element.getTypeName())
				.addCode(checkThenAddLog(element, "get returning {}", "entity"))
				.addStatement("return entity")
				.returns(element.getTypeName())
				.build());
		clazz.addMethod(MethodSpec.methodBuilder("getAll")
				.addModifiers(Modifier.PUBLIC)
				.addCode(checkThenAddLog(element, "getAll() called"))
				.addStatement("$T entities = repo.findAll()", ParameterizedTypeName.get(ClassName.get(List.class), element.getTypeName()))
				.addCode(checkThenAddLog(element, "getAll() returning {} entities", "entities.size()"))
				.addStatement("return entities")
				.returns(ParameterizedTypeName.get(ClassName.get(List.class), element.getTypeName()))
				.build());
		clazz.addMethod(MethodSpec.methodBuilder("getPaged")
				.addModifiers(Modifier.PUBLIC)
				.addParameter(TypeNames.PAGEABLE, "pageable")
				.addCode(checkThenAddLog(element, "getPaged() called with pageable: {}", "pageable"))
				.addStatement("$T page = repo.findAll(pageable)", ParameterizedTypeName.get(TypeNames.PAGE, element.getTypeName()))
				.addCode(checkThenAddLog(element, "getPaged() returning {} entities", "page.getNumberOfElements()"))
				.addStatement("return page")
				.returns(ParameterizedTypeName.get(TypeNames.PAGE, element.getTypeName()))
				.build());
		clazz.addMethod(MethodSpec.methodBuilder("delete")
				.addAnnotation(TypeNames.TRANSACTIONAL)
				.addModifiers(Modifier.PUBLIC)
				.addParameter(element.getIdTypeName(), "id")
				.addCode(checkThenAddLog(element, "delete() called with id: {}", "id"))
				.addStatement("repo.deleteById(id)")
				.addCode(checkThenAddLog(element, "delete() completed for id: {}", "id"))
				.build());
		clazz.addMethod(MethodSpec.methodBuilder("deleteAll")
				.addAnnotation(TypeNames.TRANSACTIONAL)
				.addModifiers(Modifier.PUBLIC)
				.addParameter(ParameterSpec.builder(
						ParameterizedTypeName.get(ClassName.get(List.class), element.getIdTypeName()),
						"ids").build())
				.addCode(checkThenAddLog(element, "deleteAll() called with ids: {}", "ids"))
				.addStatement("repo.deleteAllById(ids)")
				.addCode(checkThenAddLog(element, "deleteAll() completed for ids: {}", "ids"))
				.build());
		clazz.addMethod(MethodSpec.methodBuilder("save").addModifiers(Modifier.PUBLIC)
				.addAnnotation(TypeNames.TRANSACTIONAL)
				.addParameter(ParameterSpec.builder(element.getTypeName(), "entity").build())
				.addCode(checkThenAddLog(element, "save() called with entity: {}", "entity"))
				.addStatement("entity = repo.save(entity)")
				.addCode(checkThenAddLog(element, "save() returning entity: {}", "entity"))
				.addStatement("return entity")
				.returns(element.getTypeName())
				.build());
		clazz.addMethod(MethodSpec.methodBuilder("saveAll")
			    .addAnnotation(TypeNames.TRANSACTIONAL)
			    .addModifiers(Modifier.PUBLIC)
			    .addParameter(ParameterSpec.builder(ParameterizedTypeName.get(ClassName.get(List.class), element.getTypeName()), "entities").build())
			    .addCode(checkThenAddLog(element, "saveAll() called with {} entities", "entities.size()"))
			    .addStatement("List<$T> savedEntities = repo.saveAll(entities)", element.getTypeName())
			    .addCode(checkThenAddLog(element, "saveAll() returning {} entities", "savedEntities.size()"))
			    .addStatement("return savedEntities")
			    .returns(ParameterizedTypeName.get(ClassName.get(List.class), element.getTypeName()))
			    .build());
		for(FieldElement field : element.getFields()) {
			if(field.isFindBy()) {
				clazz.addMethod(MethodSpec.methodBuilder("findBy"+field.getNameCapitalized())
						.addModifiers(Modifier.PUBLIC)
						.addParameter(ClassName.get(field.getType()), field.getName())
						.addCode(checkThenAddLog(element, "findBy"+field.getNameCapitalized()+"() called with "+field.getName()+": {}", field.getName()))
						.addStatement("$T result = repo.findBy$L($L)", element.getTypeName(), field.getNameCapitalized(), field.getName())
						.addCode(checkThenAddLog(element, "findBy"+field.getNameCapitalized()+"() returning: {}", "result"))
						.addStatement("return result")
						.returns(element.getTypeName())
						.build());
			}
			if(field.isFindAllBy()) {
				clazz.addMethod(MethodSpec.methodBuilder("findAllBy"+field.getNameCapitalized())
						.addModifiers(Modifier.PUBLIC)
						.addParameter(ClassName.get(field.getType()), field.getName())
						.addCode(checkThenAddLog(element, "findAllBy"+field.getNameCapitalized()+"() called with "+field.getName()+": {}", field.getName()))
						.addStatement("$T result = repo.findAllBy$L($L)", ParameterizedTypeName.get(ClassName.get(List.class), element.getTypeName()), field.getNameCapitalized(), field.getName())
						.addCode(checkThenAddLog(element, "findAllBy"+field.getNameCapitalized()+"() returning {} entities", "result.size()"))
						.addStatement("return result")
						.returns(ParameterizedTypeName.get(ClassName.get(List.class), element.getTypeName()))
						.build());
				clazz.addMethod(MethodSpec.methodBuilder("findAllBy"+field.getNameCapitalized()+"Paged")
						.addModifiers(Modifier.PUBLIC)
						.addParameter(ClassName.get(field.getType()), field.getName())
						.addParameter(TypeNames.PAGEABLE, "pageable")
						.addCode(checkThenAddLog(element, "findAllBy"+field.getNameCapitalized()+"Paged() called with "+field.getName()+": {} and pageable: {}", "\""+field.getName()+"\"", "pageable"))
						.addStatement("$T result = repo.findAllBy$L($L, pageable)", ParameterizedTypeName.get(TypeNames.PAGE, element.getTypeName()), field.getNameCapitalized(), field.getName())
						.addCode(checkThenAddLog(element, "findAllBy"+field.getNameCapitalized()+"Paged() returning {} entities", "result.getNumberOfElements()"))
						.addStatement("return result")
						.returns(ParameterizedTypeName.get(TypeNames.PAGE, element.getTypeName()))
						.build());
			}
		}
		Util.saveFile(element.getPackageName(), clazz.build(), processingEnv);
	}

	public static String checkThenAddLog(EntityElement element, String message, String... args) {
	    if (!element.isLogging()) return "";
	    StringBuilder code = new StringBuilder("logger.debug(");
	    code.append("\"").append(message).append("\"");
	    if (args.length > 0) {
	        code.append(", ").append(String.join(", ", args));
	    }
	    code.append(");\n");
	    return code.toString();
	}

}
