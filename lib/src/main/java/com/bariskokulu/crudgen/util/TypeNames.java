package com.bariskokulu.crudgen.util;

import org.springframework.javapoet.ClassName;

public class TypeNames {

	public static final ClassName REST_CONTROLLER = ClassName.bestGuess("org.springframework.web.bind.annotation.RestController");
	public static final ClassName REQUEST_MAPPING = ClassName.bestGuess("org.springframework.web.bind.annotation.RequestMapping");
	public static final ClassName OBJECT_MAPPER = ClassName.bestGuess("com.fasterxml.jackson.databind.ObjectMapper");
	public static final ClassName VALIDATOR = ClassName.bestGuess("jakarta.validation.Validator");
	public static final ClassName GET_MAPPING = ClassName.bestGuess("org.springframework.web.bind.annotation.GetMapping");
	public static final ClassName PATH_VARIABLE = ClassName.bestGuess("org.springframework.web.bind.annotation.PathVariable");
	public static final ClassName RESPONSE_ENTITY = ClassName.bestGuess("org.springframework.http.ResponseEntity");
	public static final ClassName REQUEST_PARAM = ClassName.bestGuess("org.springframework.web.bind.annotation.RequestParam");
	public static final ClassName DELETE_MAPPING = ClassName.bestGuess("org.springframework.web.bind.annotation.DeleteMapping");
	public static final ClassName REQUEST_BODY = ClassName.bestGuess("org.springframework.web.bind.annotation.RequestBody");
	public static final ClassName POST_MAPPING = ClassName.bestGuess("org.springframework.web.bind.annotation.PostMapping");
	public static final ClassName PATCH_MAPPING = ClassName.bestGuess("org.springframework.web.bind.annotation.PatchMapping");
	public static final ClassName JSON_NODE = ClassName.bestGuess("com.fasterxml.jackson.databind.JsonNode");
	public static final ClassName JSON_PATCH = ClassName.bestGuess("com.flipkart.zjsonpatch.JsonPatch");
	public static final ClassName CONSTRAINT_VIOLATION = ClassName.bestGuess("jakarta.validation.ConstraintViolation");
	public static final ClassName PAGE_REQUEST = ClassName.bestGuess("org.springframework.data.domain.PageRequest");
	public static final ClassName VALID = ClassName.bestGuess("jakarta.validation.Valid");
	public static final ClassName MAPPER = ClassName.bestGuess("org.mapstruct.Mapper");
	public static final ClassName BEAN_MAPPING = ClassName.bestGuess("org.mapstruct.BeanMapping");
	public static final ClassName MAPPING_TARGET = ClassName.bestGuess("org.mapstruct.MappingTarget");
	public static final ClassName NULL_STRATEGY = ClassName.bestGuess("org.mapstruct.NullValuePropertyMappingStrategy");
	public static final ClassName REPOSITORY = ClassName.bestGuess("org.springframework.stereotype.Repository");
	public static final ClassName JPA_REPOSITORY = ClassName.bestGuess("org.springframework.data.jpa.repository.JpaRepository");
	public static final ClassName MONGO_REPOSITORY = ClassName.bestGuess("org.springframework.data.mongodb.repository.MongoRepository");
	public static final ClassName JPA_SPEC_EXECUTOR = ClassName.bestGuess("org.springframework.data.jpa.repository.JpaSpecificationExecutor");
	public static final ClassName PAGE = ClassName.bestGuess("org.springframework.data.domain.Page");
	public static final ClassName PAGEABLE = ClassName.bestGuess("org.springframework.data.domain.Pageable");
	public static final ClassName SERVICE = ClassName.bestGuess("org.springframework.stereotype.Service");
	public static final ClassName CONTROLLER = ClassName.bestGuess("org.springframework.stereotype.Controller");
	
}
