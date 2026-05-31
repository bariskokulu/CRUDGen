package com.bariskokulu.crudgen.processor;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

import com.bariskokulu.crudgen.annotation.CrudGen;
import com.bariskokulu.crudgen.annotation.EndpointGen;
import com.bariskokulu.crudgen.processor.component.EntityElement;
import com.bariskokulu.crudgen.processor.component.UseCaseServiceElement;
import com.bariskokulu.crudgen.processor.generator.EntityControllerGenerator;
import com.bariskokulu.crudgen.processor.generator.EntityDTOGenerator;
import com.bariskokulu.crudgen.processor.generator.EntityLifecycleCallbacksInterfaceGenerator;
import com.bariskokulu.crudgen.processor.generator.EntityMapperGenerator;
import com.bariskokulu.crudgen.processor.generator.EntityRelationApplierGenerator;
import com.bariskokulu.crudgen.processor.generator.EntityRepositoryGenerator;
import com.bariskokulu.crudgen.processor.generator.EntityServiceGenerator;
import com.bariskokulu.crudgen.processor.generator.SecurityServiceInterfaceGenerator;
import com.bariskokulu.crudgen.processor.generator.UseCaseControllerGenerator;
import com.bariskokulu.crudgen.util.TypeNames;
import com.bariskokulu.crudgen.util.Util;

@SupportedAnnotationTypes({
	"com.bariskokulu.crudgen.annotation.CrudGen",
	"com.bariskokulu.crudgen.annotation.EndpointGen"
})
public class Generator extends AbstractProcessor {

	private List<EntityElement> entityElements;
	private List<UseCaseServiceElement> serviceElements;
	private boolean securityServiceGenerated;
	private boolean lifecycleHooksGenerated;

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (roundEnv.processingOver()) {
			return false;
		}

		TypeNames.init(processingEnv);

		entityElements = roundEnv.getElementsAnnotatedWith(CrudGen.class).stream()
				.filter(e -> e.getKind() == ElementKind.CLASS)
				.map(e -> new EntityElement(e, processingEnv))
				.filter(e -> !e.isInvalid())
				.collect(Collectors.toList());
		for (EntityElement entity : entityElements) {
			if (entity.getDtos().containsKey("Update") && !TypeNames.hasJsonPatchStack(processingEnv)) {
				TypeNames.validateJsonPatchStack(processingEnv);
				entity.setInvalid(true);
			}
			if (entity.getCustomControllerTypeName() != null && EntityRelationApplierGenerator.hasRelationBindings(entity)) {
				Util.error(entity.getName() + ": customController is incompatible with @DTOField(relation=true). "
						+ "Use the generated controller or call " + entity.getName()
						+ "RelationApplier from the custom controller.", processingEnv);
				entity.setInvalid(true);
			}
		}
		entityElements = entityElements.stream().filter(e -> !e.isInvalid()).collect(Collectors.toList());
		serviceElements = roundEnv.getElementsAnnotatedWith(EndpointGen.class).stream()
				.filter(e -> e.getKind() == ElementKind.CLASS)
				.map(e -> new UseCaseServiceElement(e, processingEnv))
				.filter(e -> !e.isInvalid())
				.collect(Collectors.toList());
		entityElements.forEach(e -> EntityDTOGenerator.generate(e, processingEnv));
		entityElements.forEach(e -> EntityMapperGenerator.generate(e, processingEnv));
		entityElements.forEach(e -> EntityRelationApplierGenerator.generate(e, processingEnv));
		entityElements.forEach(e -> EntityRepositoryGenerator.generate(e, processingEnv));
		entityElements.forEach(e -> EntityServiceGenerator.generate(e, processingEnv));
		entityElements.forEach(e -> EntityControllerGenerator.generate(e, processingEnv));
		serviceElements.forEach(e -> UseCaseControllerGenerator.generate(e, processingEnv));
		boolean needSecurity = entityElements.stream().anyMatch(EntityElement::isSecureEndpoints)
				|| serviceElements.stream().anyMatch(UseCaseServiceElement::isSecureEndpoints);
		boolean needLifecycle = entityElements.stream().anyMatch(EntityElement::isLifecycleHooks);
		if (needSecurity && !securityServiceGenerated) {
			SecurityServiceInterfaceGenerator.generate(processingEnv);
			securityServiceGenerated = true;
		}
		if (needLifecycle && !lifecycleHooksGenerated) {
			EntityLifecycleCallbacksInterfaceGenerator.generate(processingEnv);
			lifecycleHooksGenerated = true;
		}

		return !entityElements.isEmpty() || !serviceElements.isEmpty();
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

}
