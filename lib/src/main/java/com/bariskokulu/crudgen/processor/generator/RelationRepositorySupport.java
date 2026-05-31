package com.bariskokulu.crudgen.processor.generator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import com.bariskokulu.crudgen.processor.component.DTOElement;
import com.bariskokulu.crudgen.processor.component.DTOFieldElement;
import com.bariskokulu.crudgen.processor.component.EntityElement;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

public final class RelationRepositorySupport {

	private RelationRepositorySupport() {
	}

	public static Map<ClassName, String> relationRepositories(EntityElement element, ProcessingEnvironment processingEnv) {
		Map<ClassName, String> repositories = new LinkedHashMap<>();
		List<DTOElement> dtos = new ArrayList<>();
		if (element.getDtos().containsKey("Create")) {
			dtos.add(element.getDtos().get("Create"));
		}
		if (element.getDtos().containsKey("Update")) {
			dtos.add(element.getDtos().get("Update"));
		}
		for (DTOElement dto : dtos) {
			for (DTOFieldElement field : dto.getFields()) {
				if (!field.isRelation() || field.isNestedProjection() || field.getRelationEntityType() == null) {
					continue;
				}
				TypeElement related = processingEnv.getElementUtils().getTypeElement(field.getRelationEntityType().canonicalName());
				if (related == null) {
					continue;
				}
				ClassName repoType = DTOFieldElement.generatedRepositoryType(related, processingEnv);
				if (repoType != null) {
					repositories.putIfAbsent(repoType, repoFieldName(repoType));
				}
			}
		}
		return repositories;
	}

	public static void addRepositoryFields(TypeSpec.Builder clazz, Map<ClassName, String> repositories) {
		for (Map.Entry<ClassName, String> entry : repositories.entrySet()) {
			clazz.addField(FieldSpec.builder(entry.getKey(), entry.getValue(), javax.lang.model.element.Modifier.PRIVATE,
					javax.lang.model.element.Modifier.FINAL).build());
		}
	}

	public static void addRepositoryConstructorParams(Builder constructor, Map<ClassName, String> repositories) {
		for (Map.Entry<ClassName, String> entry : repositories.entrySet()) {
			constructor.addParameter(ParameterSpec.builder(entry.getKey(), entry.getValue()).build());
			constructor.addStatement("this.$L = $L", entry.getValue(), entry.getValue());
		}
	}

	public static String repoFieldName(ClassName repoType) {
		String simple = repoType.simpleName();
		return Character.toLowerCase(simple.charAt(0)) + simple.substring(1);
	}

}
