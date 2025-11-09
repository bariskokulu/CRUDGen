package com.bariskokulu.crudgen.processor.component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;

import com.bariskokulu.crudgen.annotation.CrudGen;
import com.bariskokulu.crudgen.util.RepoType;
import com.bariskokulu.crudgen.util.Util;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import lombok.Getter;

@Getter
public class EntityElement extends BaseElement {

	private final TypeElement element;
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
	private final String packageName;
	private final ClassName repoTypeName;
	private final ClassName serviceTypeName;
	private final ClassName mapperTypeName;
	private final Map<String, DTOElement> dtos;
	private final TypeName customRepoTypeName;

	public EntityElement(Element element, ProcessingEnvironment processingEnv) {
		this.element = (TypeElement) element;
		CrudGen annotation = element.getAnnotation(CrudGen.class);
		this.repoType = annotation.repo();
		Element idElement = element.getEnclosedElements().stream().filter(e -> e.getAnnotationMirrors().stream().filter(a -> a.getAnnotationType().asElement().getSimpleName().toString().equals("org.springframework.data.annotation.Id")).findFirst().isPresent()).findFirst().orElse(null);
		if(idElement != null) this.idTypeName = ClassName.get(idElement.asType());
		else this.idTypeName = ClassName.get(Long.class);
		this.typeName = ClassName.get((TypeElement) element);
		this.packageName = annotation.packageName().isBlank() ? this.typeName.packageName() : annotation.packageName();
		this.fields = element.getEnclosedElements().stream().filter(e -> e.getKind()==ElementKind.FIELD).map(e -> new FieldElement(e)).collect(Collectors.toList());
		this.mapperName = this.typeName.simpleName()+"Mapper";
		this.controllerPath = annotation.controllerPath();
		this.controllerName = annotation.controllerName().isBlank() ? this.typeName.simpleName()+"Controller" : annotation.controllerName();
		this.serviceName = annotation.serviceName().isBlank() ? this.typeName.simpleName()+"Service" : annotation.serviceName();
		this.repositoryName = annotation.repositoryName().isBlank() ? this.typeName.simpleName()+"Repository" : annotation.repositoryName();
		this.name = element.getSimpleName().toString();
		this.repoTypeName = ClassName.get(packageName, repositoryName);
		this.serviceTypeName = annotation.service() || !controllerPath.isBlank() ? ClassName.get(packageName, serviceName) : null;
		this.mapperTypeName = !controllerPath.isBlank() ? ClassName.get(packageName, mapperName) : null;
		Set<String> dtosList = new HashSet<String>(Set.of(annotation.dtos()));
		if(!dtosList.contains("Read")) {
//			throw new IllegalArgumentException("A Read DTO is required.");
			Util.error("A \"Read\" DTO is required for entity "+name, processingEnv);
			setInvalid(true);
		}
		this.dtos = dtosList.stream().collect(Collectors.toMap(t -> t, t -> new DTOElement(t, this)));
		TypeName _customRepo = null;
		try {
			Class<?> customRepo = annotation.customRepo();
			if(customRepo != null && customRepo != Void.class) _customRepo = ClassName.get(annotation.customRepo());
		} catch (MirroredTypeException e) {
			if(e.getTypeMirror() != null && !"java.lang.Void".equals(e.getTypeMirror().toString())) _customRepo = ClassName.get(e.getTypeMirror());
		}
		customRepoTypeName = _customRepo;
	}

}
