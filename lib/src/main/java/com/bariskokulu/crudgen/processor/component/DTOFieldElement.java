package com.bariskokulu.crudgen.processor.component;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import com.bariskokulu.crudgen.annotation.http.DTOField;

import lombok.Getter;

@Getter
public class DTOFieldElement {

	private final Element element;
	private final TypeMirror type;
	private final String name;
	private final String nameCapitalized;
	
	public DTOFieldElement(Element element, DTOField annotation) {
		this.element = element;
		this.type = element.asType();
		this.name = annotation.fieldName().isBlank() ? element.getSimpleName().toString() : annotation.fieldName();
		this.nameCapitalized = name.substring(0, 1).toUpperCase() + name.substring(1);
	}
	
}
