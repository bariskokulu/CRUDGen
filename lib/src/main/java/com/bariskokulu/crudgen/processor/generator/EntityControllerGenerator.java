package com.bariskokulu.crudgen.processor.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;

import com.bariskokulu.crudgen.processor.component.DTOElement;
import com.bariskokulu.crudgen.processor.component.EntityElement;
import com.bariskokulu.crudgen.processor.component.FieldElement;
import com.bariskokulu.crudgen.util.OpenApiUtil;
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

	public static void generate(EntityElement element, ProcessingEnvironment processingEnv) {
		if(element.getCustomControllerTypeName() != null) return;
		if(Util.isBlank(element.getControllerPath())) return;
		boolean needsJsonPatch = element.getDtos().containsKey("Update");
		TypeSpec.Builder clazz = TypeSpec.classBuilder(element.getControllerName())
				.addAnnotation(AnnotationSpec.builder(TypeNames.REST_CONTROLLER).build())
				.addAnnotation(AnnotationSpec.builder(TypeNames.REQUEST_MAPPING).addMember("value", "$S", element.getControllerPath()).build())
				.addModifiers(Modifier.PUBLIC);
		if(element.isOpenApi()) {
			clazz.addAnnotation(OpenApiUtil.buildTagAnnotation(element.getName(), "Operations for " + element.getName()));
		}
		if(element.getExtendControllerTypeName() != null) {
			clazz.addSuperinterface(element.getExtendControllerTypeName());
		}
		clazz.addField(FieldSpec.builder(element.getServiceTypeName(), "service", Modifier.PRIVATE, Modifier.FINAL).build());
		clazz.addField(FieldSpec.builder(element.getMapperTypeName(), "mapper", Modifier.PRIVATE, Modifier.FINAL).build());
		if (needsJsonPatch) {
			clazz.addField(FieldSpec.builder(TypeNames.OBJECT_MAPPER, "objectMapper", Modifier.PRIVATE, Modifier.FINAL).build());
		}
		clazz.addField(FieldSpec.builder(TypeNames.VALIDATOR, "validator", Modifier.PRIVATE, Modifier.FINAL).build());
		if(element.isLogging()) {
			clazz.addField(FieldSpec.builder(TypeNames.LOGGER, "logger", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer("$T.getLogger($T.class)", TypeNames.LOGGER_FACTORY, element.getControllerTypeName()).build());
		}
		MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC)
				.addParameter(element.getServiceTypeName(), "service")
				.addParameter(element.getMapperTypeName(), "mapper");
		if (needsJsonPatch) {
			constructorBuilder.addParameter(TypeNames.OBJECT_MAPPER, "objectMapper");
		}
		constructorBuilder.addParameter(TypeNames.VALIDATOR, "validator")
				.addStatement("this.service = service")
				.addStatement("this.mapper = mapper");
		if (needsJsonPatch) {
			constructorBuilder.addStatement("this.objectMapper = objectMapper");
		}
		constructorBuilder.addStatement("this.validator = validator");
		if(element.isSecureEndpoints()) {
			constructorBuilder.addParameter(TypeNames.CRUDGEN_SECURITY_SERVICE, "securityService")
			.addStatement("this.securityService = securityService");
			clazz.addField(FieldSpec.builder(TypeNames.CRUDGEN_SECURITY_SERVICE, "securityService", Modifier.PRIVATE, Modifier.FINAL).build());
		}
		if(element.isLifecycleHooks()) {
			ParameterizedTypeName lifecycleType = ParameterizedTypeName.get(TypeNames.LIFECYCLE_CALLBACKS_INTERFACE, element.getTypeName());
			constructorBuilder.addParameter(
					ParameterSpec.builder(lifecycleType, "lifecycleHooks")
							.addAnnotation(ClassName.get("org.springframework.lang", "Nullable"))
							.build())
					.addStatement("this.lifecycleHooks = lifecycleHooks");
			clazz.addField(FieldSpec.builder(lifecycleType, "lifecycleHooks", Modifier.PRIVATE, Modifier.FINAL).build());
		}
		clazz.addMethod(constructorBuilder.build());

		DTOElement readDTO = element.getDtos().get("Read");
		if(readDTO == null) {
			Util.error("A \"Read\" DTO is required for entity "+element.getName(), processingEnv);
			return;
		}

		ClassName returnDtoTypeName = readDTO.getTypeName();

		MethodSpec.Builder getMethod = MethodSpec.methodBuilder("get")
				.addAnnotation(AnnotationSpec.builder(TypeNames.GET_MAPPING).addMember("value", "$S", "/{id}").build())
				.addModifiers(Modifier.PUBLIC);
		addOpenApiMethodDocs(element, getMethod,
				"Get " + element.getName() + " by ID",
				"Retrieves a single " + element.getName() + " entity by its ID",
				"200", true);
		getMethod.addParameter(pathVariableIdParam(element, "The ID of the " + element.getName() + " to retrieve").build())
				.addCode(checkThenAddLog(element, "GET /{id} called with id {}", "id"))
				.addCode(checkThenAddSecurity(element, "get", "id"))
				.addStatement("$T entity = service.get(id)", element.getTypeName())
				.addStatement("if(entity == null) throw new $T($T.NOT_FOUND, \"Entity with id \"+ id + \" not found.\")", TypeNames.RESOURCE_STATUS_EXCEPTION, TypeNames.HTTP_STATUS)
				.addStatement("$T returnDto = mapper.get(entity)", returnDtoTypeName)
				.addCode(checkThenAddLog(element, "GET /{id} returning entity {}", "returnDto"))
				.addStatement("return $T.ok(returnDto)", TypeNames.RESPONSE_ENTITY)
				.returns(ParameterizedTypeName.get(TypeNames.RESPONSE_ENTITY, returnDtoTypeName));
		clazz.addMethod(getMethod.build());

		MethodSpec.Builder getAllMethod = MethodSpec.methodBuilder("getAll")
				.addAnnotation(AnnotationSpec.builder(TypeNames.GET_MAPPING).addMember("value", "$S", "/").build())
				.addModifiers(Modifier.PUBLIC);
		addOpenApiMethodDocs(element, getAllMethod,
				"Get all " + element.getName() + "s",
				"Retrieves all " + element.getName() + " entities",
				"200", false);
		getAllMethod.addCode(checkThenAddLog(element, "GET / called"))
				.addCode(checkThenAddSecurity(element, "getAll", ""))
				.addStatement("List<$T> returnDtos = service.getAll().stream().map(t -> mapper.get(t)).collect($T.toList())", returnDtoTypeName, ClassName.get(Collectors.class))
				.addCode(checkThenAddLog(element, "GET / returning {} entities", "returnDtos.size()"))
				.addStatement("return $T.ok(returnDtos)", TypeNames.RESPONSE_ENTITY)
				.returns(ParameterizedTypeName.get(TypeNames.RESPONSE_ENTITY, ParameterizedTypeName.get(ClassName.get(List.class), returnDtoTypeName)));
		clazz.addMethod(getAllMethod.build());

		MethodSpec.Builder getPagedMethod = MethodSpec.methodBuilder("getPaged")
				.addAnnotation(AnnotationSpec.builder(TypeNames.GET_MAPPING).addMember("value", "$S", "/paged").build())
				.addModifiers(Modifier.PUBLIC);
		addOpenApiMethodDocs(element, getPagedMethod,
				"Get paginated " + element.getName() + "s",
				"Retrieves a paginated list of " + element.getName() + " entities",
				"200", false);
		addPageAndSizeQueryParams(element, getPagedMethod);
		getPagedMethod
				.addCode(checkThenAddLog(element, "GET /getPaged called with page {}, size {}", "page", "size"))
				.addCode(checkThenAddSecurity(element, "getPaged", "page, size"))
				.addStatement("Page<$T> pageResult = service.getPaged($T.of(page, size)).map(t -> mapper.get(t))", returnDtoTypeName, TypeNames.PAGE_REQUEST)
				.addCode(checkThenAddLog(element, "GET /getPaged returning {} entities", "pageResult.getNumberOfElements()"))
				.addStatement("return $T.ok(pageResult)", TypeNames.RESPONSE_ENTITY)
				.returns(ParameterizedTypeName.get(TypeNames.RESPONSE_ENTITY, ParameterizedTypeName.get(TypeNames.PAGE, returnDtoTypeName)));
		clazz.addMethod(getPagedMethod.build());
		MethodSpec.Builder deleteMethod = MethodSpec.methodBuilder("delete")
				.addAnnotation(AnnotationSpec.builder(TypeNames.DELETE_MAPPING).addMember("value", "$S", "/{id}").build())
				.addModifiers(Modifier.PUBLIC);
		addOpenApiMethodDocs(element, deleteMethod,
				"Delete " + element.getName() + " by ID",
				"Deletes a " + element.getName() + " entity by its ID",
				"204", true);
		deleteMethod.addParameter(pathVariableIdParam(element, "The ID of the " + element.getName() + " to delete").build())
				.addCode(checkThenAddLog(element, "DELETE /{id} called with id {}", "id"))
				.addCode(checkThenAddSecurity(element, "delete", "id"))
				.addStatement("$T existing = service.get(id)", element.getTypeName())
				.addStatement("if(existing == null) throw new $T($T.NOT_FOUND, \"Entity with id \"+ id + \" not found.\")", TypeNames.RESOURCE_STATUS_EXCEPTION, TypeNames.HTTP_STATUS)
				.addCode(checkThenAddLifeCycleHook(element, "beforeDelete", "existing"))
				.addStatement("service.delete(id)")
				.addCode(checkThenAddLifeCycleHook(element, "afterDelete", "existing"))
				.addCode(checkThenAddLog(element, "DELETE /{id} completed for id {}", "id"))
				.addStatement("return $T.noContent().build()", TypeNames.RESPONSE_ENTITY)
				.returns(ParameterizedTypeName.get(TypeNames.RESPONSE_ENTITY, ClassName.get(Void.class)));
		clazz.addMethod(deleteMethod.build());
		MethodSpec.Builder deleteBatchMethod = MethodSpec.methodBuilder("deleteBatch")
				.addAnnotation(AnnotationSpec.builder(TypeNames.DELETE_MAPPING)
						.addMember("value", "$S", "/batch")
						.build())
				.addModifiers(Modifier.PUBLIC);
		addOpenApiMethodDocs(element, deleteBatchMethod,
				"Delete multiple " + element.getName() + "s",
				"Deletes multiple " + element.getName() + " entities by their IDs",
				"204", false);
		ParameterSpec.Builder deleteBatchParam = ParameterSpec.builder(
				ParameterizedTypeName.get(ClassName.get(List.class), element.getIdTypeName()), 
				"ids")
			.addAnnotation(TypeNames.REQUEST_BODY);
		if(element.isOpenApi()) {
			deleteBatchParam.addAnnotation(OpenApiUtil.buildRequestBodyAnnotation("ids"));
		}
		deleteBatchMethod.addParameter(deleteBatchParam.build())
				.addCode(checkThenAddLog(element, "DELETE /batch called with ids {}", "ids"))
				.addCode(checkThenAddSecurity(element, "deleteBatch", "ids"))
				.addStatement("$T toDelete = new $T<>()", ParameterizedTypeName.get(ClassName.get(List.class), element.getTypeName()), ClassName.get(ArrayList.class))
				.addCode("for ($T id : ids) {\n", element.getIdTypeName())
				.addStatement("  $T e = service.get(id)", element.getTypeName())
				.addStatement("  if (e != null) toDelete.add(e)")
				.addCode("}\n")
				.addCode(batchLifeCycleHook(element, "beforeDeleteBatch", "toDelete"))
				.addStatement("service.deleteAll(ids)")
				.addCode(batchLifeCycleHook(element, "afterDeleteBatch", "toDelete"))
				.addCode(checkThenAddLog(element, "DELETE /batch completed for ids {}", "ids"))
				.addStatement("return $T.noContent().build()", TypeNames.RESPONSE_ENTITY)
				.returns(ParameterizedTypeName.get(TypeNames.RESPONSE_ENTITY, ClassName.get(Void.class)));
		clazz.addMethod(deleteBatchMethod.build());
		if(element.getDtos().containsKey("Create")) {
			MethodSpec.Builder createMethod = MethodSpec.methodBuilder("create")
					.addModifiers(Modifier.PUBLIC)
					.addAnnotation(AnnotationSpec.builder(TypeNames.POST_MAPPING).build());
			addOpenApiMethodDocs(element, createMethod,
					"Create new " + element.getName(),
					"Creates a new " + element.getName() + " entity",
					"201", false);
			ParameterSpec.Builder createBodyParam = ParameterSpec.builder(element.getDtos().get("Create").getTypeName(), "body")
				.addAnnotation(TypeNames.VALID)
				.addAnnotation(TypeNames.REQUEST_BODY);
			if(element.isOpenApi()) {
				createBodyParam.addAnnotation(OpenApiUtil.buildRequestBodyAnnotation("body"));
			}
			createMethod.addParameter(createBodyParam.build())
					.addCode(checkThenAddLog(element, "POST / called with body {}", "body"))
					.addCode(checkThenAddSecurity(element, "create", "body"))
					.addStatement("$T draft = mapper.create(body)", element.getTypeName())
					.addCode(checkThenAddLifeCycleHook(element, "beforeCreate", "draft"))
					.addStatement("$T created = service.save(draft)", element.getTypeName())
					.addStatement("$T returnDto = mapper.get(created)", returnDtoTypeName)
					.addCode(checkThenAddLifeCycleHook(element, "afterCreate", "created"))
					.addCode(checkThenAddLog(element, "POST / created entity {}", "returnDto"))
					.addStatement("return $T.ok(returnDto)", TypeNames.RESPONSE_ENTITY)
					.returns(ParameterizedTypeName.get(TypeNames.RESPONSE_ENTITY, returnDtoTypeName));
			clazz.addMethod(createMethod.build());
			MethodSpec.Builder createBatchMethod = MethodSpec.methodBuilder("createBatch")
					.addAnnotation(AnnotationSpec.builder(TypeNames.POST_MAPPING)
							.addMember("value", "$S", "/batch")
							.build())
					.addModifiers(Modifier.PUBLIC);
			addOpenApiMethodDocs(element, createBatchMethod,
					"Create multiple " + element.getName() + "s",
					"Creates multiple " + element.getName() + " entities",
					"201", false);
			ParameterSpec.Builder createBatchBodyParam = ParameterSpec.builder(
					ParameterizedTypeName.get(ClassName.get(List.class), element.getDtos().get("Create").getTypeName()), 
					"bodies")
				.addAnnotation(TypeNames.VALID)
				.addAnnotation(TypeNames.REQUEST_BODY);
			if(element.isOpenApi()) {
				createBatchBodyParam.addAnnotation(OpenApiUtil.buildRequestBodyAnnotation("bodies"));
			}
			createBatchMethod.addParameter(createBatchBodyParam.build())
					.addCode(checkThenAddLog(element, "POST /batch called with bodies {}", "bodies"))
					.addCode(checkThenAddSecurity(element, "createBatch", "bodies"))
					.addStatement("$T drafts = bodies.stream().map(mapper::create).collect($T.toList())", 
							ParameterizedTypeName.get(ClassName.get(List.class), element.getTypeName()),
							ClassName.get(Collectors.class))
					.addCode(batchLifeCycleHook(element, "beforeCreateBatch", "drafts"))
					.addStatement("$T entities = service.saveAll(drafts)", 
							ParameterizedTypeName.get(ClassName.get(List.class), element.getTypeName()))
					.addStatement("$T returnDtos = entities.stream().map(mapper::get).collect($T.toList())", 
							ParameterizedTypeName.get(ClassName.get(List.class), returnDtoTypeName),
							ClassName.get(Collectors.class))
					.addCode(batchLifeCycleHook(element, "afterCreateBatch", "entities"))
					.addCode(checkThenAddLog(element, "POST /batch created {} entities", "returnDtos.size()"))
					.addStatement("return $T.ok(returnDtos)", TypeNames.RESPONSE_ENTITY)
					.returns(ParameterizedTypeName.get(TypeNames.RESPONSE_ENTITY, 
							ParameterizedTypeName.get(ClassName.get(List.class), returnDtoTypeName)));
			clazz.addMethod(createBatchMethod.build());
		}
		if (needsJsonPatch) {
			DTOElement dto = element.getDtos().get("Update");
			MethodSpec.Builder updateMethod = MethodSpec.methodBuilder("update")
					.addModifiers(Modifier.PUBLIC)
					.addAnnotation(AnnotationSpec.builder(TypeNames.PATCH_MAPPING).addMember("value", "$S", "/{id}").addMember("consumes", "$S", "application/json-patch+json").build());
			addOpenApiMethodDocs(element, updateMethod,
					"Update " + element.getName() + " by ID",
					"Updates a " + element.getName() + " entity by its ID using JSON Patch",
					"200", true);
			ParameterSpec.Builder updateBodyParam = ParameterSpec.builder(TypeNames.JSON_NODE, "body")
				.addAnnotation(TypeNames.REQUEST_BODY);
			if(element.isOpenApi()) {
				updateBodyParam.addAnnotation(OpenApiUtil.buildRequestBodyAnnotation("body"));
			}
			updateMethod.addParameter(pathVariableIdParam(element, "The ID of the " + element.getName() + " to update").build())
					.addParameter(updateBodyParam.build())
					.addCode(checkThenAddLog(element, "PATCH /{id} called with id {}, body {}", "id", "body"))
					.addCode(checkThenAddSecurity(element, "update", "id, body"))
					.addStatement("$T existing = service.get(id)", element.getTypeName())
					.addStatement("if(existing == null) throw new $T($T.NOT_FOUND, \"Entity with id \"+ id + \" not found.\")", TypeNames.RESOURCE_STATUS_EXCEPTION, TypeNames.HTTP_STATUS)
					.addCode(checkThenAddLifeCycleHook(element, "beforeUpdate", "existing"))
					.addStatement("$T patchedDto", dto.getTypeName())
					.addCode("try {\n")
					.addStatement("patchedDto = objectMapper.treeToValue($T.apply(body, objectMapper.valueToTree(mapper.toPatch(existing))), $T.class)", TypeNames.JSON_PATCH, dto.getTypeName())
					.addCode("} catch($T | $T | $T e) {\n", TypeNames.JSON_PROCESSING_EXCEPTION, IllegalArgumentException.class, TypeNames.JSON_PATCH_EXCEPTION)
					.addStatement("throw new $T(\"Invalid patch\", e)", IllegalArgumentException.class)
					.addCode("}\n")
					.addStatement("$T violations = validator.validate(patchedDto)", ParameterizedTypeName.get(ClassName.get(Set.class), ParameterizedTypeName.get(TypeNames.CONSTRAINT_VIOLATION, dto.getTypeName())))
					.addStatement("if(!violations.isEmpty()) throw new $T(violations)", TypeNames.CONSTRAINT_EXCEPTION)
					.addStatement("mapper.patch(existing, patchedDto)")
					.addStatement("$T updated = service.save(existing)", element.getTypeName())
					.addStatement("$T returnDto = mapper.get(updated)", returnDtoTypeName)
					.addCode(checkThenAddLifeCycleHook(element, "afterUpdate", "updated"))
					.addCode(checkThenAddLog(element, "PATCH /{id} updated entity {}", "returnDto"))
					.addStatement("return $T.ok(returnDto)", TypeNames.RESPONSE_ENTITY)
					.returns(ParameterizedTypeName.get(TypeNames.RESPONSE_ENTITY, returnDtoTypeName));
			clazz.addMethod(updateMethod.build());
			MethodSpec.Builder updateBatchMethod = MethodSpec.methodBuilder("updateBatch")
					.addAnnotation(AnnotationSpec.builder(TypeNames.PATCH_MAPPING)
							.addMember("value", "$S", "/batch")
							.addMember("consumes", "$S", "application/json-patch+json")
							.build())
					.addModifiers(Modifier.PUBLIC);
			addOpenApiMethodDocs(element, updateBatchMethod,
					"Update multiple " + element.getName() + "s",
					"Updates multiple " + element.getName() + " entities using JSON Patch",
					"200", false);
			ParameterSpec.Builder updateBatchParam = ParameterSpec.builder(
					ParameterizedTypeName.get(ClassName.get(Map.class), 
							element.getIdTypeName(), 
							TypeNames.JSON_NODE), 
					"patches")
				.addAnnotation(TypeNames.REQUEST_BODY);
			if(element.isOpenApi()) {
				updateBatchParam.addAnnotation(OpenApiUtil.buildRequestBodyAnnotation("patches"));
			}
			updateBatchMethod.addParameter(updateBatchParam.build())
					.addCode(checkThenAddLog(element, "PATCH /batch called with patches {}", "patches"))
					.addCode(checkThenAddSecurity(element, "updateBatch", "patches"))
					.addCode(checkThenAddLifeCycleHookNoArgs(element, "beforeUpdateBatch"))
					.addStatement("$T results = new $T<>()", 
							ParameterizedTypeName.get(ClassName.get(List.class), returnDtoTypeName),
							ClassName.get(ArrayList.class))
					.addStatement("$T savedForHooks = new $T<>()", 
							ParameterizedTypeName.get(ClassName.get(List.class), element.getTypeName()),
							ClassName.get(ArrayList.class))
					.addCode("for($T<$T, $T> entry : patches.entrySet()) {\n", 
							ClassName.get(Map.Entry.class), element.getIdTypeName(), TypeNames.JSON_NODE)
					.addStatement("    $T id = entry.getKey()", element.getIdTypeName())
					.addStatement("    $T patch = entry.getValue()", TypeNames.JSON_NODE)
					.addStatement("    $T existing = service.get(id)", element.getTypeName())
					.addStatement("    if(existing == null) throw new $T($T.NOT_FOUND, \"Entity with id \"+ id + \" not found.\")", 
							TypeNames.RESOURCE_STATUS_EXCEPTION, TypeNames.HTTP_STATUS)
					.addStatement("    $T patchedDto", dto.getTypeName())
					.addCode("    try {\n")
					.addStatement("        patchedDto = objectMapper.treeToValue($T.apply(patch, objectMapper.valueToTree(mapper.toPatch(existing))), $T.class)", 
							TypeNames.JSON_PATCH, dto.getTypeName())
					.addCode("} catch($T | $T | $T e) {\n", TypeNames.JSON_PROCESSING_EXCEPTION, IllegalArgumentException.class, TypeNames.JSON_PATCH_EXCEPTION)
					.addStatement("        throw new $T(\"Invalid patch for entity \" + id, e)", IllegalArgumentException.class)
					.addCode("    }\n")
					.addStatement("    $T violations = validator.validate(patchedDto)", 
							ParameterizedTypeName.get(ClassName.get(Set.class), 
									ParameterizedTypeName.get(TypeNames.CONSTRAINT_VIOLATION, dto.getTypeName())))
					.addStatement("    if(!violations.isEmpty()) throw new $T(violations)", TypeNames.CONSTRAINT_EXCEPTION)
					.addStatement("    mapper.patch(existing, patchedDto)")
					.addStatement("    $T updated = service.save(existing)", element.getTypeName())
					.addStatement("    savedForHooks.add(updated)")
					.addStatement("    results.add(mapper.get(updated))")
					.addCode("}\n")
					.addCode(batchLifeCycleHook(element, "afterUpdateBatch", "savedForHooks"))
					.addCode(checkThenAddLog(element, "PATCH /batch updated {} entities", "results.size()"))
					.addStatement("return $T.ok(results)", TypeNames.RESPONSE_ENTITY)
					.returns(ParameterizedTypeName.get(TypeNames.RESPONSE_ENTITY, 
							ParameterizedTypeName.get(ClassName.get(List.class), returnDtoTypeName)));
			clazz.addMethod(updateBatchMethod.build());
		}
		for(FieldElement field : element.getFields()) {
			if(field.isFindBy()) {
				MethodSpec.Builder findByMethod = MethodSpec.methodBuilder("findBy"+field.getNameCapitalized())
						.addAnnotation(AnnotationSpec.builder(TypeNames.GET_MAPPING).addMember("value", "$S", "/findBy"+field.getNameCapitalized()).build())
						.addModifiers(Modifier.PUBLIC);
				addOpenApiMethodDocs(element, findByMethod,
						"Find " + element.getName() + " by " + field.getName(),
						"Finds a single " + element.getName() + " entity by " + field.getName(),
						"200", true);
				ParameterSpec.Builder findByParam = ParameterSpec.builder(ClassName.get(field.getType()), field.getName())
					.addAnnotation(TypeNames.REQUEST_PARAM);
				if(element.isOpenApi()) {
					findByParam.addAnnotation(OpenApiUtil.buildParameterAnnotation(field.getName(), "The " + field.getName() + " value to search for", "QUERY", true));
				}
				findByMethod.addParameter(findByParam.build())
						.addCode(checkThenAddLog(element, "GET /findBy"+field.getNameCapitalized()+"() called with "+field.getName()+" {}", field.getName()))
						.addCode(checkThenAddSecurity(element, "findBy"+field.getNameCapitalized(), field.getName()))
						.addStatement("$T returnDto = mapper.get(service.findBy$L($L))", returnDtoTypeName, field.getNameCapitalized(), field.getName())
						.addCode(checkThenAddLog(element, "GET /findBy"+field.getNameCapitalized()+"() returning entity {}", "returnDto"))
						.addStatement("return $T.ok(returnDto)", TypeNames.RESPONSE_ENTITY)
						.returns(ParameterizedTypeName.get(TypeNames.RESPONSE_ENTITY, returnDtoTypeName));
				clazz.addMethod(findByMethod.build());
			}
			if(field.isFindAllBy()) {
				MethodSpec.Builder findAllByMethod = MethodSpec.methodBuilder("findAllBy"+field.getNameCapitalized())
						.addAnnotation(AnnotationSpec.builder(TypeNames.GET_MAPPING).addMember("value", "$S", "/findAllBy"+field.getNameCapitalized()).build())
						.addModifiers(Modifier.PUBLIC);
				addOpenApiMethodDocs(element, findAllByMethod,
						"Find all " + element.getName() + "s by " + field.getName(),
						"Finds all " + element.getName() + " entities matching the " + field.getName() + " value",
						"200", false);
				ParameterSpec.Builder findAllByParam = ParameterSpec.builder(ClassName.get(field.getType()), field.getName())
					.addAnnotation(TypeNames.REQUEST_PARAM);
				if(element.isOpenApi()) {
					findAllByParam.addAnnotation(OpenApiUtil.buildParameterAnnotation(field.getName(), "The " + field.getName() + " value to search for", "QUERY", true));
				}
				findAllByMethod.addParameter(findAllByParam.build())
						.addCode(checkThenAddLog(element, "GET /findAllBy"+field.getNameCapitalized()+"() called with "+field.getName()+" {}", field.getName()))
						.addCode(checkThenAddSecurity(element, "findAllBy"+field.getNameCapitalized(), field.getName()))
						.addStatement("$T returnDtos = service.findAllBy$L($L).stream().map(t -> mapper.get(t)).collect($T.toList())", 
								ParameterizedTypeName.get(ClassName.get(List.class), returnDtoTypeName), 
								field.getNameCapitalized(), field.getName(), 
								ClassName.get(Collectors.class))
						.addCode(checkThenAddLog(element, "GET /findAllBy"+field.getNameCapitalized()+"() returning {} entities", "returnDtos.size()"))
						.addStatement("return $T.ok(returnDtos)", TypeNames.RESPONSE_ENTITY)
						.returns(ParameterizedTypeName.get(TypeNames.RESPONSE_ENTITY, ParameterizedTypeName.get(ClassName.get(List.class), returnDtoTypeName)));
				clazz.addMethod(findAllByMethod.build());
				MethodSpec.Builder findAllByPagedMethod = MethodSpec.methodBuilder("findAllBy"+field.getNameCapitalized()+"Paged")
						.addAnnotation(AnnotationSpec.builder(TypeNames.GET_MAPPING).addMember("value", "$S", "/findAllBy"+field.getNameCapitalized()+"/paged").build())
						.addModifiers(Modifier.PUBLIC);
				addOpenApiMethodDocs(element, findAllByPagedMethod,
						"Find paginated " + element.getName() + "s by " + field.getName(),
						"Finds a paginated list of " + element.getName() + " entities matching the " + field.getName() + " value",
						"200", false);
				ParameterSpec.Builder findAllByPagedFieldParam = ParameterSpec.builder(ClassName.get(field.getType()), field.getName())
					.addAnnotation(TypeNames.REQUEST_PARAM);
				if(element.isOpenApi()) {
					findAllByPagedFieldParam.addAnnotation(OpenApiUtil.buildParameterAnnotation(field.getName(), "The " + field.getName() + " value to search for", "QUERY", true));
				}
				findAllByPagedMethod.addParameter(findAllByPagedFieldParam.build());
				addPageAndSizeQueryParams(element, findAllByPagedMethod);
				findAllByPagedMethod
						.addCode(checkThenAddLog(element, "GET /findAllBy"+field.getNameCapitalized()+"Paged called with "+field.getName()+" {}, page {}, size {}", field.getName(), "page", "size"))
						.addCode(checkThenAddSecurity(element, "findAllBy"+field.getNameCapitalized()+"Paged", field.getName()+", page, size"))
						.addStatement("Page<$T> pageResult = service.findAllBy$LPaged($L, $T.of(page, size)).map(t -> mapper.get(t))", 
								returnDtoTypeName, 
								field.getNameCapitalized(), field.getName(), 
								TypeNames.PAGE_REQUEST)
						.addCode(checkThenAddLog(element, "GET /findAllBy"+field.getNameCapitalized()+"Paged returning {} entities", "pageResult.getNumberOfElements()"))
						.addStatement("return $T.ok(pageResult)", TypeNames.RESPONSE_ENTITY)
						.returns(ParameterizedTypeName.get(TypeNames.RESPONSE_ENTITY, ParameterizedTypeName.get(TypeNames.PAGE, returnDtoTypeName)));
				clazz.addMethod(findAllByPagedMethod.build());
			}
		}
		Util.saveFile(element.getPackageName(), clazz.build(), processingEnv);
	}

	private static void addOpenApiMethodDocs(EntityElement element, MethodSpec.Builder method, String summary, String description, String successCode, boolean includeNotFound) {
		if (!element.isOpenApi()) {
			return;
		}
		method.addAnnotation(OpenApiUtil.buildOperationAnnotation(summary, description));
		method.addAnnotation(OpenApiUtil.buildApiResponsesAnnotation(successCode, includeNotFound));
	}

	private static void addPageAndSizeQueryParams(EntityElement element, MethodSpec.Builder method) {
		ParameterSpec.Builder pageParam = ParameterSpec.builder(ClassName.get(Integer.class).unbox(), "page")
				.addAnnotation(TypeNames.REQUEST_PARAM)
				.addAnnotation(AnnotationSpec.builder(TypeNames.MIN).addMember("value", "0").build());
		if (element.isOpenApi()) {
			pageParam.addAnnotation(OpenApiUtil.buildParameterAnnotation("page", "Page number (0-indexed)", "QUERY", false));
		}
		ParameterSpec.Builder sizeParam = ParameterSpec.builder(ClassName.get(Integer.class).unbox(), "size")
				.addAnnotation(TypeNames.REQUEST_PARAM)
				.addAnnotation(AnnotationSpec.builder(TypeNames.MIN).addMember("value", "0").build());
		if (element.isOpenApi()) {
			sizeParam.addAnnotation(OpenApiUtil.buildParameterAnnotation("size", "Page size", "QUERY", false));
		}
		method.addParameter(pageParam.build()).addParameter(sizeParam.build());
	}

	private static ParameterSpec.Builder pathVariableIdParam(EntityElement element, String description) {
		ParameterSpec.Builder idParam = ParameterSpec.builder(element.getIdTypeName(), "id")
				.addAnnotation(AnnotationSpec.builder(TypeNames.PATH_VARIABLE).build());
		if (element.isOpenApi()) {
			idParam.addAnnotation(OpenApiUtil.buildParameterAnnotation("id", description, "PATH", true));
		}
		return idParam;
	}

	public static String checkThenAddSecurity(EntityElement element, String method, String params) {
		return element.isSecureEndpoints() ? "securityService.checkEntityAccess(\""+element.getName()+"\", \""+method+"\""+(params.length() > 0 ? ", " : "")+params+");\n" : "";
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
	
	public static String checkThenAddLifeCycleHook(EntityElement element, String methodName, String argList) {
		if (!element.isLifecycleHooks()) {
			return "";
		}
		return "if (lifecycleHooks != null) { lifecycleHooks." + methodName + "(" + argList + "); }\n";
	}

	public static String checkThenAddLifeCycleHookNoArgs(EntityElement element, String methodName) {
		if (!element.isLifecycleHooks()) {
			return "";
		}
		return "if (lifecycleHooks != null) { lifecycleHooks." + methodName + "(); }\n";
	}

	public static String batchLifeCycleHook(EntityElement element, String methodName, String listName) {
		if (!element.isLifecycleHooks()) {
			return "";
		}
		return "if (lifecycleHooks != null && !" + listName + ".isEmpty()) { lifecycleHooks." + methodName + "(" + listName + "); }\n";
	}

}
