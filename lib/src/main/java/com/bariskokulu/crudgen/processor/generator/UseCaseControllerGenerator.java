package com.bariskokulu.crudgen.processor.generator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import com.bariskokulu.crudgen.processor.component.EndpointElement;
import com.bariskokulu.crudgen.processor.component.ParameterElement;
import com.bariskokulu.crudgen.processor.component.UseCaseServiceElement;
import com.bariskokulu.crudgen.util.OpenApiUtil;
import com.bariskokulu.crudgen.util.TypeNames;
import com.bariskokulu.crudgen.util.Util;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class UseCaseControllerGenerator {

	private static final String SPRING_PATH_VARIABLE = "org.springframework.web.bind.annotation.PathVariable";
	private static final String JAKARTA_WS_PATH_PARAM = "jakarta.websocket.server.PathParam";
	private static final String JAVAX_WS_PATH_PARAM = "javax.websocket.server.PathParam";
	private static final String SPRING_REQUEST_BODY = "org.springframework.web.bind.annotation.RequestBody";
	private static final String SPRING_REQUEST_PARAM = "org.springframework.web.bind.annotation.RequestParam";

	public static void generate(UseCaseServiceElement element, ProcessingEnvironment processingEnv) {
		TypeSpec.Builder clazz = TypeSpec.classBuilder(element.getControllerName())
				.addAnnotation(AnnotationSpec.builder(TypeNames.CONTROLLER).build())
				.addAnnotation(AnnotationSpec.builder(TypeNames.REQUEST_MAPPING).addMember("value", "$S", element.getPath()).build())
				.addModifiers(Modifier.PUBLIC);
		if (element.isOpenApi()) {
			clazz.addAnnotation(OpenApiUtil.buildTagAnnotation(element.getName(), "Operations for " + element.getName()));
		}
		if (element.isLogging()) {
			clazz.addField(FieldSpec.builder(TypeNames.LOGGER, "logger", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL).initializer("$T.getLogger($T.class)", TypeNames.LOGGER_FACTORY, element.getControllerTypeName()).build());
		}
		if (element.isSecureEndpoints()) {
			clazz.addField(FieldSpec.builder(TypeNames.CRUDGEN_SECURITY_SERVICE, "securityService", Modifier.PRIVATE, Modifier.FINAL).build());
		}
		clazz.addField(FieldSpec.builder(element.getTypeName(), "service", Modifier.PRIVATE, Modifier.FINAL).build());
		MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).addParameter(element.getTypeName(), "service").addStatement("this.service = service");
		if (element.isSecureEndpoints()) {
			constructorBuilder.addParameter(TypeNames.CRUDGEN_SECURITY_SERVICE, "securityService")
					.addStatement("this.securityService = securityService");
		}
		clazz.addMethod(constructorBuilder.build());
		for (EndpointElement endpoint : element.getEndpoints()) {
			MethodSpec.Builder method = MethodSpec.methodBuilder(endpoint.getName())
					.addModifiers(Modifier.PUBLIC);

			if (element.isOpenApi()) {
				method.addAnnotation(OpenApiUtil.buildOperationAnnotation(endpoint.getName(),
						"Executes the " + endpoint.getName() + " use case"));

				if (endpoint.getReturnType() != null) {
					method.addAnnotation(OpenApiUtil.buildApiResponsesAnnotation("200", false));
				} else {
					method.addAnnotation(OpenApiUtil.buildApiResponsesAnnotation("204", false));
				}
			}

			for (ParameterElement param : endpoint.getParams()) {
				TypeName paramTypeName = TypeName.get(param.getType());
				ParameterSpec.Builder paramBuilder = ParameterSpec.builder(paramTypeName, param.getName());
				paramBuilder.addAnnotations(param.getElement().getAnnotationMirrors().stream().map(AnnotationSpec::get).collect(Collectors.toList()));

				if (element.isOpenApi()) {
					String paramLocation = determineParameterType(param);
					if ("BODY".equals(paramLocation)) {
						paramBuilder.addAnnotation(OpenApiUtil.buildRequestBodyAnnotation(param.getName()));
					} else {
						boolean isRequired = isParameterRequired(param);
						paramBuilder.addAnnotation(OpenApiUtil.buildParameterAnnotation(param.getName(),
								"Parameter: " + param.getName(), paramLocation, isRequired));
					}
				}

				method.addParameter(paramBuilder.build());
			}
			String paramString = endpoint.getParams().stream().map(ParameterElement::getName).collect(Collectors.joining(", "));
			method.addCode(checkThenAddLog(element, endpoint.getName() + "() called with params: " + (paramString.isEmpty() ? "no parameters" : paramString), endpoint.getParams().stream().map(ParameterElement::getName).toArray(String[]::new)));
			method.addCode(checkThenAddSecurity(element, endpoint.getName(), endpoint.getParams().stream().map(ParameterElement::getName).collect(Collectors.joining(", "))));
			if (endpoint.getReturnType() != null) {
				TypeName returnTypeName = TypeName.get(endpoint.getReturnType());
				method.addStatement("$T result = service.$L($L)", returnTypeName, endpoint.getName(), paramString);
				method.addCode(checkThenAddLog(element, endpoint.getName() + "() returning result {}", "result"));
				method.addStatement("return $T.ok(result)", TypeNames.RESPONSE_ENTITY);
				method.returns(ParameterizedTypeName.get(TypeNames.RESPONSE_ENTITY, returnTypeName));
			} else {
				method.addStatement("service.$L($L)", endpoint.getName(), paramString);
				method.addCode(checkThenAddLog(element, endpoint.getName() + "() completed"));
			}
			method.addAnnotation(AnnotationSpec.builder(ClassName.bestGuess("org.springframework.web.bind.annotation." + endpoint.getHttpMethod().text() + "Mapping")).addMember("value", "$S", endpoint.getPath()).build());
			clazz.addMethod(method.build());
		}
		Util.saveFile(element.getPackageName(), clazz.build(), processingEnv);
	}

	public static String checkThenAddSecurity(UseCaseServiceElement element, String method, String params) {
		return element.isSecureEndpoints() ? "securityService.checkUseAccess(\"" + method + "\"" + (params.length() > 0 ? ", " : "") + params + ");\n" : "";
	}

	public static String checkThenAddLog(UseCaseServiceElement element, String message, String... args) {
		if (!element.isLogging()) {
			return "";
		}
		StringBuilder code = new StringBuilder("logger.debug(");
		code.append("\"").append(message).append("\"");
		if (args.length > 0) {
			code.append(", ").append(String.join(", ", args));
		}
		code.append(");\n");
		return code.toString();
	}

	private static boolean hasAnyQualifiedAnnotation(Element element, String... qualifiedNames) {
		List<String> names = Arrays.asList(qualifiedNames);
		return element.getAnnotationMirrors().stream().anyMatch(am -> {
			Element t = am.getAnnotationType().asElement();
			if (t instanceof TypeElement) {
				return names.contains(((TypeElement) t).getQualifiedName().toString());
			}
			return false;
		});
	}

	private static String determineParameterType(ParameterElement param) {
		Element e = param.getElement();
		if (hasAnyQualifiedAnnotation(e, SPRING_PATH_VARIABLE, JAKARTA_WS_PATH_PARAM, JAVAX_WS_PATH_PARAM)) {
			return "PATH";
		}
		if (hasAnyQualifiedAnnotation(e, SPRING_REQUEST_BODY)) {
			return "BODY";
		}
		return "QUERY";
	}

	private static boolean isParameterRequired(ParameterElement param) {
		Element e = param.getElement();
		if (hasAnyQualifiedAnnotation(e, SPRING_PATH_VARIABLE, JAKARTA_WS_PATH_PARAM, JAVAX_WS_PATH_PARAM)
				|| hasAnyQualifiedAnnotation(e, SPRING_REQUEST_BODY)) {
			return true;
		}
		return e.getAnnotationMirrors().stream()
				.filter(am -> {
					Element t = am.getAnnotationType().asElement();
					if (t instanceof TypeElement) {
						return SPRING_REQUEST_PARAM.equals(((TypeElement) t).getQualifiedName().toString());
					}
					return false;
				})
				.findFirst()
				.map(am -> am.getElementValues().entrySet().stream()
						.noneMatch(entry -> entry.getKey().getSimpleName().toString().equals("required")
								&& entry.getValue().getValue().toString().equals("false")))
				.orElse(true);
	}

}
