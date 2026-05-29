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
	private final ClassName controllerTypeName;
	private final Map<String, DTOElement> dtos;
	private final TypeName customRepoTypeName;
	private final TypeName customServiceTypeName;
	private final TypeName customControllerTypeName;
	private final TypeName extendRepoTypeName;
	private final TypeName extendServiceTypeName;
	private final TypeName extendControllerTypeName;
	private final boolean secureEndpoints;
	private final boolean logging;
	private final boolean openApi;
	private final boolean lifecycleHooks;

	public EntityElement(Element element, ProcessingEnvironment processingEnv) {
		this.element = (TypeElement) element;
		CrudGen annotation = element.getAnnotation(CrudGen.class);
		this.repoType = annotation.repo();
		Element idElement = element.getEnclosedElements().stream().filter(e -> e.getAnnotationMirrors().stream().filter(a -> {
			String q = ((TypeElement) a.getAnnotationType().asElement()).getQualifiedName().toString();
			return "org.springframework.data.annotation.Id".equals(q) || "jakarta.persistence.Id".equals(q) || "javax.persistence.Id".equals(q);
		}).findFirst().isPresent()).findFirst().orElse(null);
		if(idElement != null) this.idTypeName = ClassName.get(idElement.asType());
		else {
			this.idTypeName = ClassName.get(Long.class);
			Util.error("No field annotated with @Id found in entity "+element.getSimpleName().toString(), processingEnv);
			setInvalid(true);
		}
		this.typeName = ClassName.get((TypeElement) element);
		this.packageName = Util.isBlank(annotation.packageName()) ? this.typeName.packageName() : annotation.packageName();
		this.fields = element.getEnclosedElements().stream().filter(e -> e.getKind()==ElementKind.FIELD).map(e -> new FieldElement(e)).collect(Collectors.toList());
		this.mapperName = this.typeName.simpleName()+"Mapper";
		this.controllerPath = annotation.controllerPath();
		this.controllerName = Util.isBlank(annotation.controllerName()) ? this.typeName.simpleName()+"Controller" : annotation.controllerName();
		this.serviceName = Util.isBlank(annotation.serviceName()) ? this.typeName.simpleName()+"Service" : annotation.serviceName();
		this.repositoryName = Util.isBlank(annotation.repositoryName()) ? this.typeName.simpleName()+"Repository" : annotation.repositoryName();
		this.name = element.getSimpleName().toString();
		this.repoTypeName = ClassName.get(packageName, repositoryName);
		this.serviceTypeName = annotation.service() || !Util.isBlank(controllerPath) ? ClassName.get(packageName, serviceName) : null;
		this.mapperTypeName = !Util.isBlank(controllerPath) ? ClassName.get(packageName, mapperName) : null;
		this.controllerTypeName = !Util.isBlank(controllerPath) ? ClassName.get(packageName, controllerName) : null;
		Set<String> dtosList = new HashSet<String>();
		for (String d : annotation.dtos()) {
			dtosList.add(d);
		}
		if(!Util.isBlank(controllerPath) && !dtosList.contains("Read")) {
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
		TypeName _customService = null;
		try {
			Class<?> customService = annotation.customService();
			if(customService != null && customService != Void.class) _customService = ClassName.get(annotation.customService());
		} catch (MirroredTypeException e) {
			if(e.getTypeMirror() != null && !"java.lang.Void".equals(e.getTypeMirror().toString())) _customService = ClassName.get(e.getTypeMirror());
		}
		customServiceTypeName = _customService;
		TypeName _customController = null;
		try {
			Class<?> customController = annotation.customController();
			if(customController != null && customController != Void.class) _customController = ClassName.get(annotation.customController());
		} catch (MirroredTypeException e) {
			if(e.getTypeMirror() != null && !"java.lang.Void".equals(e.getTypeMirror().toString())) _customController = ClassName.get(e.getTypeMirror());
		}
		customControllerTypeName = _customController;

		TypeName _extendRepo = null;
		try {
			Class<?> extendRepo = annotation.extendRepo();
			if(extendRepo != null && extendRepo != Void.class) _extendRepo = ClassName.get(annotation.extendRepo());
		} catch (MirroredTypeException e) {
			if(e.getTypeMirror() != null && !"java.lang.Void".equals(e.getTypeMirror().toString())) _extendRepo = ClassName.get(e.getTypeMirror());
		}
		extendRepoTypeName = _extendRepo;
		TypeName _extendService = null;
		try {
			Class<?> extendService = annotation.extendService();
			if(extendService != null && extendService != Void.class) _extendService = ClassName.get(annotation.extendService());
		} catch (MirroredTypeException e) {
			if(e.getTypeMirror() != null && !"java.lang.Void".equals(e.getTypeMirror().toString())) _extendService = ClassName.get(e.getTypeMirror());
		}
		extendServiceTypeName = _extendService;
		TypeName _extendController = null;
		try {
			Class<?> extendController = annotation.extendController();
			if(extendController != null && extendController != Void.class) _extendController = ClassName.get(annotation.extendController());
		} catch (MirroredTypeException e) {
			if(e.getTypeMirror() != null && !"java.lang.Void".equals(e.getTypeMirror().toString())) _extendController = ClassName.get(e.getTypeMirror());
		}
		extendControllerTypeName = _extendController;
		
		if (!controllerName.isEmpty() && Util.isInvalidJavaIdentifier(controllerName)) {
			Util.error("Invalid controller name: " + controllerName, processingEnv);
			setInvalid(true);
		}
		if (!serviceName.isEmpty() && Util.isInvalidJavaIdentifier(serviceName)) {
			Util.error("Invalid service name: " + serviceName, processingEnv);
			setInvalid(true);
		}
		if (!repositoryName.isEmpty() && Util.isInvalidJavaIdentifier(repositoryName)) {
			Util.error("Invalid repository name: " + repositoryName, processingEnv);
			setInvalid(true);
		}
		if (!controllerPath.isEmpty() && !controllerPath.startsWith("/")) {
			Util.error("Controller path must start with '/' or be empty: " + controllerPath, processingEnv);
			setInvalid(true);
		}
		if (Util.isInvalidPackageName(packageName)) {
			Util.error("Invalid package name: " + packageName, processingEnv);
			setInvalid(true);
		}

		this.secureEndpoints = annotation.securityService();
		this.logging = annotation.logging();
		this.openApi = annotation.openApi();
		this.lifecycleHooks = annotation.lifecycleHooks();
	}

}
