package com.bariskokulu.crudgen.processor.component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.bariskokulu.crudgen.annotation.Endpoint;
import com.bariskokulu.crudgen.util.HTTPMethod;
import com.bariskokulu.crudgen.util.Util;

import lombok.Getter;

@Getter
public class EndpointElement extends BaseElement {

	private final ExecutableElement element;
	private final HTTPMethod httpMethod;
	private final String path;
	private final String name;
	private final List<ParameterElement> params;
	private final TypeMirror returnType;

	public EndpointElement(ExecutableElement element, ProcessingEnvironment processingEnv) {
		this.element = element;
		Endpoint annotation = element.getAnnotation(Endpoint.class);
		this.httpMethod = annotation.method();
		this.path = annotation.path();
		this.name = element.getSimpleName().toString();
		this.params = element.getParameters().stream().map(ParameterElement::new).collect(Collectors.toList());
		this.returnType = element.getReturnType().getKind() == TypeKind.VOID ? null : element.getReturnType();

		if (Util.isBlank(path) || !path.startsWith("/")) {
			Util.error("Endpoint path must start with '/': " + path + " on " + element.getEnclosingElement().getSimpleName() + "#" + name, processingEnv);
			setInvalid(true);
		}
		Set<String> paramNames = params.stream().map(ParameterElement::getName).collect(Collectors.toCollection(HashSet::new));
		for (String pathVar : Util.pathVariableNamesFromPath(path)) {
			if (!paramNames.contains(pathVar)) {
				Util.error("Endpoint " + name + " path {" + pathVar + "} has no matching method parameter", processingEnv);
				setInvalid(true);
			}
		}
	}

}
