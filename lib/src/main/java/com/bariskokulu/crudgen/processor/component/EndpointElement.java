package com.bariskokulu.crudgen.processor.component;

import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.bariskokulu.crudgen.annotation.http.Endpoint;
import com.bariskokulu.crudgen.util.HTTPMethod;

import lombok.Getter;

@Getter
public class EndpointElement extends BaseElement {

	private final ExecutableElement element;
	private final HTTPMethod httpMethod;
	private final String path;
	private final String name;
	private final List<ParameterElement> params;
	private final TypeMirror returnType;

	public EndpointElement(ExecutableElement element) {
		this.element = element;
		Endpoint annotation = element.getAnnotation(Endpoint.class);
		this.httpMethod = annotation.method();
		this.path = annotation.path();
		this.name = element.getSimpleName().toString();
		this.params = element.getParameters().stream().map(t -> new ParameterElement(t)).collect(Collectors.toList());
		this.returnType = element.getReturnType().getKind() == TypeKind.VOID ? null : element.getReturnType();
	}

}
