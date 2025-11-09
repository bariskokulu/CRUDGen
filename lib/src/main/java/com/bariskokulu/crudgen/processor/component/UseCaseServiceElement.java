package com.bariskokulu.crudgen.processor.component;

import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import com.bariskokulu.crudgen.annotation.EndpointGen;
import com.bariskokulu.crudgen.annotation.http.Endpoint;
import com.squareup.javapoet.ClassName;

import lombok.Getter;

@Getter
public class UseCaseServiceElement extends BaseElement {

	private final TypeElement element;
	private final List<EndpointElement> endpoints;
	private final String name;
	private final String controllerName;
	private final String path;
	private final String packageName;
	private final ClassName typeName;

	public UseCaseServiceElement(Element element) {
		this.element = (TypeElement) element;
		endpoints = element.getEnclosedElements().stream().filter(t -> t.getKind()==ElementKind.METHOD && t.getAnnotation(Endpoint.class) != null).map(t -> new EndpointElement((ExecutableElement)t)).collect(Collectors.toList());
		this.name = element.getSimpleName().toString();
		EndpointGen annotation = element.getAnnotation(EndpointGen.class);
		this.controllerName = annotation.controllerName();
		this.path = annotation.controllerPath();
		this.typeName = ClassName.get((TypeElement) element);
		this.packageName = annotation.packageName().isBlank() ? this.typeName.packageName() : annotation.packageName();
	}

}
