package com.bariskokulu.crudgen.processor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import com.bariskokulu.crudgen.annotation.CrudGen;
import com.bariskokulu.crudgen.annotation.EndpointGen;
import com.bariskokulu.crudgen.processor.component.EntityElement;
import com.bariskokulu.crudgen.processor.component.UseCaseServiceElement;
import com.bariskokulu.crudgen.processor.generator.EntityControllerGenerator;
import com.bariskokulu.crudgen.processor.generator.EntityDTOGenerator;
import com.bariskokulu.crudgen.processor.generator.EntityMapperGenerator;
import com.bariskokulu.crudgen.processor.generator.EntityRepositoryGenerator;
import com.bariskokulu.crudgen.processor.generator.EntityServiceGenerator;
import com.bariskokulu.crudgen.processor.generator.UseCaseControllerGenerator;
import com.bariskokulu.crudgen.util.Util;
import com.google.auto.service.AutoService;

@SupportedSourceVersion(SourceVersion.RELEASE_25)
@SupportedAnnotationTypes({
	"com.bariskokulu.crudgen.annotation.CrudGen",
	"com.bariskokulu.crudgen.annotation.EndpointGen"
})
@AutoService(Processor.class)
public class Generator extends AbstractProcessor {

	private List<EntityElement> entityElements;
	private List<UseCaseServiceElement> serviceElements;
	private HashSet<String> processedElements = new HashSet<String>();

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			Util.warn("Processing is over.", processingEnv);
			return false;
		}
		entityElements = roundEnv.getElementsAnnotatedWith(CrudGen.class).stream()
				.filter(e -> e.getKind() == ElementKind.CLASS)
				.filter(e -> !processedElements.contains(((TypeElement) e).getQualifiedName().toString()))
				.map(e -> new EntityElement(e, processingEnv))
				.filter(e -> !e.isInvalid())
				.collect(Collectors.toList());
		serviceElements = roundEnv.getElementsAnnotatedWith(EndpointGen.class).stream()
				.filter(e -> e.getKind() == ElementKind.CLASS)
				.filter(e -> !processedElements.contains(((TypeElement) e).getQualifiedName().toString()))
				.map(e -> new UseCaseServiceElement(e))
				.filter(e -> !e.isInvalid())
				.collect(Collectors.toList());
		entityElements.forEach(e -> EntityDTOGenerator.generate(e, processingEnv));
		entityElements.forEach(e -> EntityMapperGenerator.generate(e, processingEnv));
		entityElements.forEach(e -> EntityRepositoryGenerator.generate(e, processingEnv));
		entityElements.forEach(e -> EntityServiceGenerator.generate(e, processingEnv));
		entityElements.forEach(e -> EntityControllerGenerator.generate(e, processingEnv));
		serviceElements.forEach(e -> UseCaseControllerGenerator.generate(e, processingEnv));

		processedElements.addAll(entityElements.stream().map(t -> t.getElement().getQualifiedName().toString()).collect(Collectors.toList()));
		processedElements.addAll(serviceElements.stream().map(t -> t.getElement().getQualifiedName().toString()).collect(Collectors.toList()));
		return false;
	}
	
	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

}
