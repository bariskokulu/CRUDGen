package com.bariskokulu.crudgen.util;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import com.squareup.javapoet.ClassName;

public class TypeNames {

	public static final ClassName REST_CONTROLLER = ClassName.bestGuess("org.springframework.web.bind.annotation.RestController");
	public static final ClassName REQUEST_MAPPING = ClassName.bestGuess("org.springframework.web.bind.annotation.RequestMapping");
	public static ClassName OBJECT_MAPPER = ClassName.bestGuess("com.fasterxml.jackson.databind.ObjectMapper");
	public static ClassName VALIDATOR = ClassName.bestGuess("jakarta.validation.Validator");
	public static final ClassName GET_MAPPING = ClassName.bestGuess("org.springframework.web.bind.annotation.GetMapping");
	public static final ClassName PATH_VARIABLE = ClassName.bestGuess("org.springframework.web.bind.annotation.PathVariable");
	public static final ClassName RESPONSE_ENTITY = ClassName.bestGuess("org.springframework.http.ResponseEntity");
	public static final ClassName REQUEST_PARAM = ClassName.bestGuess("org.springframework.web.bind.annotation.RequestParam");
	public static final ClassName DELETE_MAPPING = ClassName.bestGuess("org.springframework.web.bind.annotation.DeleteMapping");
	public static final ClassName REQUEST_BODY = ClassName.bestGuess("org.springframework.web.bind.annotation.RequestBody");
	public static final ClassName POST_MAPPING = ClassName.bestGuess("org.springframework.web.bind.annotation.PostMapping");
	public static final ClassName PATCH_MAPPING = ClassName.bestGuess("org.springframework.web.bind.annotation.PatchMapping");
	public static ClassName JSON_NODE = ClassName.bestGuess("com.fasterxml.jackson.databind.JsonNode");
	public static ClassName JSON_PATCH = ClassName.bestGuess("com.flipkart.zjsonpatch.JsonPatch");
	public static ClassName CONSTRAINT_VIOLATION = ClassName.bestGuess("jakarta.validation.ConstraintViolation");
	public static final ClassName PAGE_REQUEST = ClassName.bestGuess("org.springframework.data.domain.PageRequest");
	public static ClassName VALID = ClassName.bestGuess("jakarta.validation.Valid");
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
	public static final ClassName RESOURCE_STATUS_EXCEPTION = ClassName.bestGuess("org.springframework.web.server.ResponseStatusException");
	public static final ClassName HTTP_STATUS = ClassName.bestGuess("org.springframework.http.HttpStatus");
	public static ClassName MIN = ClassName.bestGuess("jakarta.validation.constraints.Min");
	public static ClassName MAX = ClassName.bestGuess("jakarta.validation.constraints.Max");
	public static ClassName CONSTRAINT_EXCEPTION = ClassName.bestGuess("jakarta.validation.ConstraintViolationException");
	public static final ClassName TRANSACTIONAL = ClassName.bestGuess("org.springframework.transaction.annotation.Transactional");
	public static ClassName JSON_PROCESSING_EXCEPTION = ClassName.bestGuess("com.fasterxml.jackson.core.JsonProcessingException");
	public static final ClassName JSON_PATCH_EXCEPTION = ClassName.bestGuess("com.flipkart.zjsonpatch.JsonPatchApplicationException");
	public static final ClassName LOGGER = ClassName.bestGuess("org.slf4j.Logger");
	public static final ClassName LOGGER_FACTORY = ClassName.bestGuess("org.slf4j.LoggerFactory");

	public static final ClassName CRUDGEN_SECURITY_SERVICE = ClassName.get("com.bariskokulu.crudgen.security", "CrudGenSecurityService");
	public static final ClassName LIFECYCLE_CALLBACKS_INTERFACE = ClassName.get("com.bariskokulu.crudgen.lifecycle", "EntityLifecycleCallbacks");

	public static final ClassName OPERATION = ClassName.bestGuess("io.swagger.v3.oas.annotations.Operation");
	public static final ClassName API_RESPONSE = ClassName.bestGuess("io.swagger.v3.oas.annotations.responses.ApiResponse");
	public static final ClassName API_RESPONSES = ClassName.bestGuess("io.swagger.v3.oas.annotations.responses.ApiResponses");
	public static final ClassName PARAMETER = ClassName.bestGuess("io.swagger.v3.oas.annotations.Parameter");
	public static final ClassName TAG = ClassName.bestGuess("io.swagger.v3.oas.annotations.tags.Tag");
	public static final ClassName PARAMETER_IN = ClassName.bestGuess("io.swagger.v3.oas.annotations.enums.ParameterIn");
	public static final ClassName REQUEST_BODY_OPENAPI = ClassName.bestGuess("io.swagger.v3.oas.annotations.parameters.RequestBody");

	private static boolean initialized;

	public static boolean validateJsonPatchStack(ProcessingEnvironment env) {
		Elements elements = env.getElementUtils();
		if (elements.getTypeElement(OBJECT_MAPPER.canonicalName()) == null) {
			Util.error("Update DTO needs Jackson databind on the compile classpath (e.g. spring-boot-starter-json).", env);
			return false;
		}
		if (elements.getTypeElement(JSON_NODE.canonicalName()) == null) {
			Util.error("Update DTO needs jackson-databind JsonNode on the compile classpath.", env);
			return false;
		}
		if (elements.getTypeElement(JSON_PROCESSING_EXCEPTION.canonicalName()) == null) {
			Util.error("Update DTO needs Jackson core exception types on the compile classpath.", env);
			return false;
		}
		if (elements.getTypeElement(JSON_PATCH.canonicalName()) == null) {
			if (elements.getTypeElement("tools.jackson.databind.ObjectMapper") != null) {
				Util.error("Update DTO needs io.github.vishwakarma:zjsonpatch:0.6.x+ (Jackson 3: Jackson3JsonPatch).", env);
			} else {
				Util.error("Update DTO needs com.flipkart.zjsonpatch:zjsonpatch:0.4.x (Jackson 2: JsonPatch).", env);
			}
			return false;
		}
		if (elements.getTypeElement(JSON_PATCH_EXCEPTION.canonicalName()) == null) {
			Util.error("Update DTO needs zjsonpatch on the compile classpath (JsonPatchApplicationException missing).", env);
			return false;
		}
		return true;
	}

	private static void configureJackson(ProcessingEnvironment env) {
		Elements elements = env.getElementUtils();
		TypeElement om3 = elements.getTypeElement("tools.jackson.databind.ObjectMapper");
		boolean useJackson3 = om3 != null;
		if (useJackson3) {
			OBJECT_MAPPER = ClassName.bestGuess("tools.jackson.databind.ObjectMapper");
			JSON_NODE = ClassName.bestGuess("tools.jackson.databind.JsonNode");
			JSON_PATCH = ClassName.bestGuess("com.flipkart.zjsonpatch.Jackson3JsonPatch");
			JSON_PROCESSING_EXCEPTION = ClassName.bestGuess("tools.jackson.core.JacksonException");
		} else {
			OBJECT_MAPPER = ClassName.bestGuess("com.fasterxml.jackson.databind.ObjectMapper");
			JSON_NODE = ClassName.bestGuess("com.fasterxml.jackson.databind.JsonNode");
			JSON_PATCH = ClassName.bestGuess("com.flipkart.zjsonpatch.JsonPatch");
			JSON_PROCESSING_EXCEPTION = ClassName.bestGuess("com.fasterxml.jackson.core.JsonProcessingException");
		}
	}

	public static synchronized void init(ProcessingEnvironment env) {
		if (initialized) {
			return;
		}
		Elements elements = env.getElementUtils();
		TypeElement jakartaValidator = elements.getTypeElement("jakarta.validation.Validator");
		TypeElement javaxValidator = elements.getTypeElement("javax.validation.Validator");
		String prefix;
		if (javaxValidator != null && jakartaValidator == null) {
			prefix = "javax";
		} else {
			prefix = "jakarta";
		}
		VALIDATOR = ClassName.bestGuess(prefix + ".validation.Validator");
		CONSTRAINT_VIOLATION = ClassName.bestGuess(prefix + ".validation.ConstraintViolation");
		VALID = ClassName.bestGuess(prefix + ".validation.Valid");
		MIN = ClassName.bestGuess(prefix + ".validation.constraints.Min");
		MAX = ClassName.bestGuess(prefix + ".validation.constraints.Max");
		CONSTRAINT_EXCEPTION = ClassName.bestGuess(prefix + ".validation.ConstraintViolationException");
		configureJackson(env);
		initialized = true;
	}

}
