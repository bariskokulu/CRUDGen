package com.bariskokulu.crudgen.processor.component;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import com.bariskokulu.crudgen.annotation.DTOField;
import com.bariskokulu.crudgen.util.Util;
import com.squareup.javapoet.ClassName;

import lombok.Getter;

@Getter
public class DTOElement extends BaseElement {

	private final String name;
	private final ClassName typeName;
	private final List<DTOFieldElement> fields;
	
	public DTOElement(String type, EntityElement entityElement, ProcessingEnvironment processingEnv) {
		this.name = entityElement.getName()+type+"DTO";
		this.typeName = ClassName.get(entityElement.getPackageName(), this.name);
		List<DTOFieldElement> allFields = entityElement.getElement().getEnclosedElements().stream().filter(t -> t.getKind()==ElementKind.FIELD)
				.map(t -> new AbstractMap.SimpleEntry<Element, DTOField>(t, Arrays.stream(t.getAnnotationsByType(DTOField.class)).filter(a -> a.dto().equals(type)).findFirst().orElse(null)))
				.filter(t -> t.getValue() != null)
				.map(t -> new DTOFieldElement(t.getKey(), t.getValue(), processingEnv))
				.collect(Collectors.toList());
		if (allFields.stream().anyMatch(DTOFieldElement::isInvalid)) {
			setInvalid(true);
		}
		this.fields = expandFields(type, allFields, processingEnv);
		if (this.fields.isEmpty()) {
			Util.error("DTO \"" + type + "\" on entity " + entityElement.getName()
					+ " has no fields; add @DTOField(dto = \"" + type + "\") on entity fields", processingEnv);
			setInvalid(true);
		}
	}

	private static List<DTOFieldElement> expandFields(String dtoType, List<DTOFieldElement> allFields,
			ProcessingEnvironment processingEnv) {
		List<DTOFieldElement> fields = new ArrayList<>();
		for (DTOFieldElement field : allFields) {
			if (field.isInvalid()) {
				continue;
			}
			fields.add(field);
			if (field.isNestedRead() && "Read".equals(dtoType)) {
				TypeElement related = Util.toTypeElement(field.getElement().asType());
				fields.addAll(DTOFieldElement.nestedReadProjections(field, related, processingEnv));
			}
		}
		return fields;
	}

}
