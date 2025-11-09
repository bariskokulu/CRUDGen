package com.bariskokulu.crudgen.processor.component;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import com.bariskokulu.crudgen.annotation.http.DTOField;
import com.squareup.javapoet.ClassName;

import lombok.Getter;

@Getter
public class DTOElement extends BaseElement {

	private final String name;
	private final ClassName typeName;
	private final List<DTOFieldElement> fields;
	
	public DTOElement(String type, EntityElement entityElement) {
		this.name = entityElement.getName()+type+"DTO";
		this.typeName = ClassName.get(entityElement.getPackageName(), this.name);
		this.fields = entityElement.getElement().getEnclosedElements().stream().filter(t -> t.getKind()==ElementKind.FIELD)
				.map(t -> new AbstractMap.SimpleEntry<Element, DTOField>(t, Arrays.stream(t.getAnnotationsByType(DTOField.class)).filter(a -> a.dto().equals(type)).findFirst().orElse(null)))
				.filter(t -> t.getValue() != null)
				.map(t -> new DTOFieldElement(t.getKey(), t.getValue()))
				.collect(Collectors.toList());
	}

}
