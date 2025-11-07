package com.bariskokulu.crudgen.processor.component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import org.springframework.javapoet.ClassName;
import org.springframework.javapoet.TypeName;

import com.bariskokulu.crudgen.annotation.CrudGen;
import com.bariskokulu.crudgen.util.RepoType;

import lombok.Getter;

@Getter
public class EntityElement {

	private final Element element;
	private final TypeName idTypeName;
	private final ClassName typeName;
	private final RepoType repoType;
	private final List<FieldElement> fields;
	private final String mapperName;
	private final String controllerPath;
	private final String controllerName;
	private final String serviceName;
	private final String repositoryName;
	private final String name;
	private final ClassName repoTypeName;
	private final ClassName serviceTypeName;
	private final ClassName mapperTypeName;
	private final Map<String, DTOElement> dtos;

	public EntityElement(Element element) {
		this.element = element;
		CrudGen annotation = element.getAnnotation(CrudGen.class);
		this.repoType = annotation.repo();
		Element idElement = element.getEnclosedElements().stream().filter(e -> e.getAnnotationMirrors().stream().filter(a -> a.getAnnotationType().asElement().getSimpleName().toString().equals("org.springframework.data.annotation.Id")).findFirst().isPresent()).findFirst().orElse(null);
		if(idElement != null) this.idTypeName = ClassName.get(idElement.asType());
		else this.idTypeName = ClassName.get(Long.class);
		this.typeName = ClassName.get((TypeElement) element);
		this.fields = element.getEnclosedElements().stream().filter(e -> e.getKind()==ElementKind.FIELD).map(e -> new FieldElement(e)).collect(Collectors.toList());
		this.mapperName = this.typeName.simpleName()+"Mapper";
		this.controllerPath = annotation.controllerPath();
		this.controllerName = annotation.controllerName().isBlank() ? this.typeName.simpleName()+"Controller" : annotation.controllerName();
		this.serviceName = annotation.serviceName().isBlank() ? this.typeName.simpleName()+"Service" : annotation.serviceName();
		this.repositoryName = annotation.repositoryName().isBlank() ? this.typeName.simpleName()+"Repository" : annotation.repositoryName();
		this.name = element.getSimpleName().toString();
		this.repoTypeName = ClassName.get("com.bariskokulu.crudgen", repositoryName);
		this.serviceTypeName = annotation.service() || !controllerPath.isBlank() ? ClassName.get("com.bariskokulu.crudgen", serviceName) : null;
		this.mapperTypeName = !controllerPath.isBlank() ? ClassName.get("com.bariskokulu.crudgen", mapperName) : null;
		Set<String> dtosList = new HashSet<String>(Set.of(annotation.dtos()));
		if(!dtosList.contains("Read")) {
			throw new IllegalArgumentException("A Read DTO is required.");
		}
		this.dtos = dtosList.stream().collect(Collectors.toMap(t -> t, t -> new DTOElement(t, this)));
	}

}
