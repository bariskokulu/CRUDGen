package com.bariskokulu.crudgen.processor.component;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import com.bariskokulu.crudgen.annotation.simple.FindAllBy;
import com.bariskokulu.crudgen.annotation.simple.FindBy;

import lombok.Getter;

@Getter
public class ParameterElement extends BaseElement {

	private final Element element;
	private final TypeMirror type;
	private final String name;
	private final String nameCapitalized;
	private final boolean findBy;
	private final boolean findAllBy;
	
	public ParameterElement(Element element) {
		this.element = element;
		this.type = element.asType();
		this.name = element.getSimpleName().toString();
		this.nameCapitalized = name.substring(0, 1).toUpperCase() + name.substring(1);
		this.findBy = element.getAnnotation(FindBy.class) != null;
		this.findAllBy = element.getAnnotation(FindAllBy.class) != null;
	}
	
}
