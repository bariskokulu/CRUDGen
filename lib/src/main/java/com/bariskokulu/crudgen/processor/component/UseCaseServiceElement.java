package com.bariskokulu.crudgen.processor.component;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import com.bariskokulu.crudgen.annotation.Endpoint;
import com.bariskokulu.crudgen.annotation.EndpointGen;
import com.bariskokulu.crudgen.util.Util;
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
	private final boolean secureEndpoints;
	private final boolean logging;
	private final boolean openApi;
	private final ClassName controllerTypeName;

	public UseCaseServiceElement(Element element, ProcessingEnvironment processingEnv) {
		this.element = (TypeElement) element;
		endpoints = element.getEnclosedElements().stream().filter(t -> t.getKind()==ElementKind.METHOD && t.getAnnotation(Endpoint.class) != null).map(t -> new EndpointElement((ExecutableElement)t)).collect(Collectors.toList());
		this.name = element.getSimpleName().toString();
		EndpointGen annotation = element.getAnnotation(EndpointGen.class);
		this.path = annotation.controllerPath();
		this.typeName = ClassName.get((TypeElement) element);
		this.controllerName = Util.isBlank(annotation.controllerName()) ? this.typeName.simpleName()+"Controller" : annotation.controllerName();
		this.packageName = Util.isBlank(annotation.packageName()) ? this.typeName.packageName() : annotation.packageName();
		
		if (!controllerName.isEmpty() && Util.isInvalidJavaIdentifier(controllerName)) {
			Util.error("Invalid controller name: " + controllerName, processingEnv);
			setInvalid(true);
		}
		if (Util.isInvalidPackageName(packageName)) {
			Util.error("Invalid package name: " + packageName, processingEnv);
			setInvalid(true);
		}

		this.secureEndpoints = annotation.securityService();
		this.logging = annotation.logging();
		this.openApi = annotation.openApi();
		this.controllerTypeName = !Util.isBlank(path) ? ClassName.get(packageName, controllerName) : null;
	}

}
