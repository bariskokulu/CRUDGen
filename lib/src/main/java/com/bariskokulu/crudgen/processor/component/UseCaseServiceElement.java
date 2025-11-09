package com.bariskokulu.crudgen.processor.component;

import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;

import com.bariskokulu.crudgen.annotation.EndpointGen;
import com.bariskokulu.crudgen.annotation.http.Endpoint;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import lombok.Getter;

@Getter
public class UseCaseServiceElement {

	private final Element element;
	private final List<EndpointElement> endpoints;
	private final String name;
	private final String controllerName;
	private final String path;
	private final TypeName typeName;

	public UseCaseServiceElement(Element element) {
		this.element = element;
		endpoints = element.getEnclosedElements().stream().filter(t -> t.getKind()==ElementKind.METHOD && t.getAnnotation(Endpoint.class) != null).map(t -> new EndpointElement((ExecutableElement)t)).collect(Collectors.toList());
		this.name = element.getSimpleName().toString();
		EndpointGen annotation = element.getAnnotation(EndpointGen.class);
		this.controllerName = annotation.controllerName();
		this.path = annotation.controllerPath();
		this.typeName = ClassName.get(element.asType());
	}

}
