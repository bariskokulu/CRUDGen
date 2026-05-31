package com.bariskokulu.crudgen.processor.generator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import com.bariskokulu.crudgen.processor.component.DTOElement;
import com.bariskokulu.crudgen.processor.component.DTOFieldElement;
import com.bariskokulu.crudgen.processor.component.EntityElement;
import com.bariskokulu.crudgen.util.TypeNames;
import com.bariskokulu.crudgen.util.Util;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class EntityRelationApplierGenerator {

	public static boolean hasRelationBindings(EntityElement element) {
		return !collectBindings(element).isEmpty();
	}

	public static void generate(EntityElement element, ProcessingEnvironment processingEnv) {
		if (element.getMapperTypeName() == null || element.getCustomControllerTypeName() != null) {
			return;
		}
		List<DTOFieldElement> bindings = collectBindings(element);
		if (bindings.isEmpty()) {
			return;
		}
		for (DTOFieldElement field : bindings) {
			TypeElement related = processingEnv.getElementUtils()
					.getTypeElement(field.getRelationEntityType().canonicalName());
			if (related == null) {
				Util.error("Relation field " + field.getEntityFieldName() + " references unknown type "
						+ field.getRelationEntityType().canonicalName(), processingEnv);
				return;
			}
			if (DTOFieldElement.generatedRepositoryType(related, processingEnv) == null) {
				Util.error("Relation field " + field.getEntityFieldName() + " requires related entity "
						+ related.getSimpleName() + " to be annotated with @CrudGen (repository needed).",
						processingEnv);
				return;
			}
		}
		String applierName = element.getName() + "RelationApplier";
		Map<ClassName, String> repositories = RelationRepositorySupport.relationRepositories(element, processingEnv);
		TypeSpec.Builder clazz = TypeSpec.classBuilder(applierName)
				.addAnnotation(AnnotationSpec.builder(ClassName.get("org.springframework.stereotype", "Component")).build())
				.addModifiers(Modifier.PUBLIC);
		RelationRepositorySupport.addRepositoryFields(clazz, repositories);
		MethodSpec.Builder ctor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
		RelationRepositorySupport.addRepositoryConstructorParams(ctor, repositories);
		clazz.addMethod(ctor.build());
		if (element.getDtos().containsKey("Create")) {
			DTOElement createDto = element.getDtos().get("Create");
			MethodSpec.Builder method = MethodSpec.methodBuilder("applyForCreate")
					.addModifiers(Modifier.PUBLIC)
					.addParameter(element.getTypeName(), "entity")
					.addParameter(createDto.getTypeName(), "dto");
			for (DTOFieldElement field : bindingsForDto(bindings, createDto)) {
				method.addStatement("apply$LForCreate(entity, dto.get$L())", capitalize(field.getEntityFieldName()),
						field.getNameCapitalized());
			}
			clazz.addMethod(method.build());
		}
		if (element.getDtos().containsKey("Update")) {
			DTOElement updateDto = element.getDtos().get("Update");
			MethodSpec.Builder method = MethodSpec.methodBuilder("applyForUpdate")
					.addModifiers(Modifier.PUBLIC)
					.addParameter(element.getTypeName(), "entity")
					.addParameter(updateDto.getTypeName(), "dto");
			for (DTOFieldElement field : bindingsForDto(bindings, updateDto)) {
				method.addStatement("apply$LForUpdate(entity, dto.get$L())", capitalize(field.getEntityFieldName()),
						field.getNameCapitalized());
			}
			clazz.addMethod(method.build());
		}
		for (DTOFieldElement field : bindings) {
			clazz.addMethod(buildApplyForCreate(element, field));
			clazz.addMethod(buildApplyForUpdate(element, field));
			clazz.addMethod(buildResolveMethod(field, repositories, processingEnv));
		}
		Util.saveFile(element.getPackageName(), clazz.build(), processingEnv);
	}

	private static List<DTOFieldElement> collectBindings(EntityElement element) {
		Map<String, DTOFieldElement> byEntityField = new LinkedHashMap<>();
		for (DTOElement dto : element.getDtos().values()) {
			for (DTOFieldElement field : dto.getFields()) {
				if (field.isRelation() && !field.isNestedProjection() && field.getRelationEntityType() != null) {
					byEntityField.putIfAbsent(field.getEntityFieldName(), field);
				}
			}
		}
		return new ArrayList<>(byEntityField.values());
	}

	private static List<DTOFieldElement> bindingsForDto(List<DTOFieldElement> bindings, DTOElement dto) {
		Set<String> names = dto.getFields().stream()
				.filter(f -> f.isRelation() && !f.isNestedProjection())
				.map(DTOFieldElement::getEntityFieldName)
				.collect(Collectors.toSet());
		return bindings.stream().filter(b -> names.contains(b.getEntityFieldName())).collect(Collectors.toList());
	}

	private static MethodSpec buildApplyForCreate(EntityElement element, DTOFieldElement field) {
		String cap = capitalize(field.getEntityFieldName());
		MethodSpec.Builder method = MethodSpec.methodBuilder("apply" + cap + "ForCreate")
				.addModifiers(Modifier.PRIVATE)
				.addParameter(element.getTypeName(), "entity");
		if (field.isRelationCollection()) {
			method.addParameter(ParameterizedTypeName.get(ClassName.get(List.class), field.getRelationIdTypeName()), "relationIds")
					.beginControlFlow("if (relationIds == null)")
					.addStatement("return")
					.endControlFlow()
					.addStatement("entity.set$L(resolve$L(relationIds))", cap, cap);
		} else {
			method.addParameter(field.getRelationIdTypeName(), "relationId")
					.beginControlFlow("if (relationId == null)")
					.addStatement("return")
					.endControlFlow()
					.addStatement("entity.set$L(resolve$L(relationId))", cap, cap);
		}
		return method.build();
	}

	private static MethodSpec buildApplyForUpdate(EntityElement element, DTOFieldElement field) {
		String cap = capitalize(field.getEntityFieldName());
		MethodSpec.Builder method = MethodSpec.methodBuilder("apply" + cap + "ForUpdate")
				.addModifiers(Modifier.PRIVATE)
				.addParameter(element.getTypeName(), "entity");
		if (field.isRelationCollection()) {
			method.addParameter(ParameterizedTypeName.get(ClassName.get(List.class), field.getRelationIdTypeName()), "relationIds")
					.beginControlFlow("if (relationIds == null)")
					.addStatement("entity.set$L(null)", cap)
					.addStatement("return")
					.endControlFlow()
					.addStatement("entity.set$L(resolve$L(relationIds))", cap, cap);
		} else {
			method.addParameter(field.getRelationIdTypeName(), "relationId")
					.beginControlFlow("if (relationId == null)")
					.addStatement("entity.set$L(null)", cap)
					.addStatement("return")
					.endControlFlow()
					.addStatement("entity.set$L(resolve$L(relationId))", cap, cap);
		}
		return method.build();
	}

	private static MethodSpec buildResolveMethod(DTOFieldElement field, Map<ClassName, String> repositories,
			ProcessingEnvironment processingEnv) {
		String cap = capitalize(field.getEntityFieldName());
		TypeElement related = processingEnv.getElementUtils().getTypeElement(field.getRelationEntityType().canonicalName());
		ClassName repoType = DTOFieldElement.generatedRepositoryType(related, processingEnv);
		String repoField = RelationRepositorySupport.repoFieldName(repoType);
		String label = field.getRelationEntityType().simpleName();
		if (field.isRelationCollection()) {
			TypeMirror entityCollectionType = field.getElement().asType();
			boolean asSet = Util.isSetType(entityCollectionType);
			ClassName resolvedCollectionType = asSet ? ClassName.get(LinkedHashSet.class) : ClassName.get(ArrayList.class);
			String idGetter = Util.idGetterExpression("item", related);
			MethodSpec.Builder method = MethodSpec.methodBuilder("resolve" + cap)
					.addModifiers(Modifier.PRIVATE)
					.addParameter(ParameterizedTypeName.get(ClassName.get(List.class), field.getRelationIdTypeName()), "relationIds")
					.returns(TypeName.get(entityCollectionType));
			method.addStatement("$T<$T> uniqueIds = new $T<>()", LinkedHashSet.class, field.getRelationIdTypeName(), LinkedHashSet.class)
					.beginControlFlow("for ($T id : relationIds)", field.getRelationIdTypeName())
					.beginControlFlow("if (id != null)")
					.addStatement("uniqueIds.add(id)")
					.endControlFlow()
					.endControlFlow()
					.addStatement("$T<$T, $T> loaded = new $T<>()", ClassName.get(java.util.HashMap.class),
							field.getRelationIdTypeName(), field.getRelationEntityType(), ClassName.get(java.util.HashMap.class))
					.beginControlFlow("for ($T item : $L.findAllById(uniqueIds))", field.getRelationEntityType(), repoField)
					.addStatement("loaded.put($L, item)", idGetter)
					.endControlFlow()
					.addStatement("$T<$T> resolved = new $T<>()", resolvedCollectionType, field.getRelationEntityType(),
							resolvedCollectionType)
					.beginControlFlow("for ($T id : relationIds)", field.getRelationIdTypeName())
					.beginControlFlow("if (id == null)")
					.addStatement("continue")
					.endControlFlow()
					.addStatement("$T item = loaded.get(id)", field.getRelationEntityType())
					.beginControlFlow("if (item == null)")
					.addStatement("throw new $T($T.BAD_REQUEST, \"$L with id \" + id + \" not found.\")",
							TypeNames.RESOURCE_STATUS_EXCEPTION, TypeNames.HTTP_STATUS, label)
					.endControlFlow()
					.addStatement("resolved.add(item)")
					.endControlFlow()
					.addStatement("return resolved");
			return method.build();
		}
		MethodSpec.Builder method = MethodSpec.methodBuilder("resolve" + cap)
				.addModifiers(Modifier.PRIVATE)
				.addParameter(field.getRelationIdTypeName(), "relationId")
				.returns(field.getRelationEntityType());
		method.addStatement("return $L.findById(relationId).orElseThrow(() -> new $T($T.BAD_REQUEST, \"$L with id \" + relationId + \" not found.\"))",
				repoField, TypeNames.RESOURCE_STATUS_EXCEPTION, TypeNames.HTTP_STATUS, label);
		return method.build();
	}

	private static String capitalize(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

}
