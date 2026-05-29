package com.bariskokulu.crudgen.util;

import com.squareup.javapoet.AnnotationSpec;

public class OpenApiUtil {

	public static AnnotationSpec buildTagAnnotation(String name, String description) {
		return AnnotationSpec.builder(TypeNames.TAG)
			.addMember("name", "$S", name)
			.addMember("description", "$S", description)
			.build();
	}

	public static AnnotationSpec buildOperationAnnotation(String summary, String description) {
		return AnnotationSpec.builder(TypeNames.OPERATION)
			.addMember("summary", "$S", summary)
			.addMember("description", "$S", description)
			.build();
	}

	public static AnnotationSpec buildApiResponsesAnnotation(String successCode, boolean includeNotFound) {
		AnnotationSpec.Builder builder = AnnotationSpec.builder(TypeNames.API_RESPONSES);
		
		AnnotationSpec.Builder successResponse = AnnotationSpec.builder(TypeNames.API_RESPONSE)
			.addMember("responseCode", "$S", successCode)
			.addMember("description", "$S", "Success");
		
		if (includeNotFound) {
			builder.addMember("value", "{$L, $L, $L}", 
				successResponse.build(),
				AnnotationSpec.builder(TypeNames.API_RESPONSE)
					.addMember("responseCode", "$S", "404")
					.addMember("description", "$S", "Entity not found")
					.build(),
				AnnotationSpec.builder(TypeNames.API_RESPONSE)
					.addMember("responseCode", "$S", "400")
					.addMember("description", "$S", "Bad request")
					.build());
		} else {
			builder.addMember("value", "{$L, $L}", 
				successResponse.build(),
				AnnotationSpec.builder(TypeNames.API_RESPONSE)
					.addMember("responseCode", "$S", "400")
					.addMember("description", "$S", "Bad request")
					.build());
		}
		
		return builder.build();
	}

	public static AnnotationSpec buildParameterAnnotation(String name, String description, String paramType, boolean required) {
		return AnnotationSpec.builder(TypeNames.PARAMETER)
			.addMember("name", "$S", name)
			.addMember("description", "$S", description)
			.addMember("required", "$L", required)
			.addMember("in", "$T.$L", TypeNames.PARAMETER_IN, paramType)
			.build();
	}

	public static AnnotationSpec buildRequestBodyAnnotation(String name) {
		return AnnotationSpec.builder(TypeNames.REQUEST_BODY_OPENAPI)
			.addMember("description", "$S", "Request body: " + name)
			.addMember("required", "true")
			.build();
	}

}

