package com.bariskokulu.crudgen.processor.generator;


import java.util.ArrayList;
import java.util.List;
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
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

public class EntityMapperGenerator {

	public static void generate(EntityElement element, ProcessingEnvironment processingEnv) {
		if(element.getMapperTypeName()==null) return;
		TypeSpec.Builder clazz = TypeSpec.interfaceBuilder(element.getMapperName())
				.addAnnotation(AnnotationSpec.builder(TypeNames.MAPPER)
						.addMember("componentModel", "$S", "spring")
						.build())
				.addModifiers(Modifier.PUBLIC);
		DTOElement readDto = element.getDtos().get("Read");
		DTOElement updateDto = element.getDtos().get("Update");
		addCollectionIdMappers(clazz, readDto, updateDto, processingEnv);
		clazz.addMethod(buildGetMethod(element, readDto, processingEnv));
		if(element.getDtos().containsKey("Create")) {
			DTOElement createDto = element.getDtos().get("Create");
			clazz.addMethod(buildCreateMethod(element, createDto));
		}
		if(element.getDtos().containsKey("Update")) {
			clazz.addMethod(buildToPatchMethod(element, updateDto, processingEnv));
			clazz.addMethod(buildPatchMethod(element, updateDto));
		}
		Util.saveFile(element.getPackageName(), clazz.build(), processingEnv);
	}

	private static List<DTOFieldElement> relationFields(DTOElement dto) {
		return dto.getFields().stream().filter(DTOFieldElement::isRelation).collect(Collectors.toList());
	}

	private static void addCollectionIdMappers(TypeSpec.Builder clazz, DTOElement readDto, DTOElement updateDto,
			ProcessingEnvironment processingEnv) {
		java.util.Set<String> added = new java.util.HashSet<>();
		for (DTOElement dto : new DTOElement[] { readDto, updateDto }) {
			if (dto == null) {
				continue;
			}
			for (DTOFieldElement field : relationFields(dto)) {
				if (field.isRelationCollection() && added.add(field.getEntityFieldName())) {
					clazz.addMethod(buildCollectionIdMapper(field, processingEnv));
				}
			}
		}
	}

	private static MethodSpec buildGetMethod(EntityElement element, DTOElement readDto,
			ProcessingEnvironment processingEnv) {
		MethodSpec.Builder method = MethodSpec.methodBuilder("get")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addParameter(element.getTypeName(), "entity")
				.returns(readDto.getTypeName());
		addReadMappings(method, readDto, processingEnv);
		return method.build();
	}

	private static MethodSpec buildCreateMethod(EntityElement element, DTOElement createDto) {
		MethodSpec.Builder method = MethodSpec.methodBuilder("create")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addParameter(createDto.getTypeName(), "dto")
				.returns(element.getTypeName());
		addEntityIgnoreMappings(method, createDto);
		return method.build();
	}

	private static MethodSpec buildToPatchMethod(EntityElement element, DTOElement updateDto,
			ProcessingEnvironment processingEnv) {
		MethodSpec.Builder method = MethodSpec.methodBuilder("toPatch")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addParameter(element.getTypeName(), "entity")
				.returns(updateDto.getTypeName());
		addReadMappings(method, updateDto, processingEnv);
		return method.build();
	}

	private static MethodSpec buildPatchMethod(EntityElement element, DTOElement updateDto) {
		MethodSpec.Builder method = MethodSpec.methodBuilder("patch")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addAnnotation(AnnotationSpec.builder(TypeNames.BEAN_MAPPING)
						.addMember("nullValuePropertyMappingStrategy", "$T.$L", TypeNames.NULL_STRATEGY, "SET_TO_NULL")
						.build())
				.addParameter(ParameterSpec.builder(element.getTypeName(), "entity")
						.addAnnotation(AnnotationSpec.builder(TypeNames.MAPPING_TARGET).build())
						.build())
				.addParameter(updateDto.getTypeName(), "dto");
		addEntityIgnoreMappings(method, updateDto);
		return method.build();
	}

	private static void addReadMappings(MethodSpec.Builder method, DTOElement dto, ProcessingEnvironment processingEnv) {
		List<AnnotationSpec> specs = new ArrayList<>();
		for (DTOFieldElement field : dto.getFields()) {
			if (field.isNestedProjection()) {
				specs.add(AnnotationSpec.builder(TypeNames.MAPPING)
						.addMember("target", "$S", field.getName())
						.addMember("source", "$S", field.getMappingSource())
						.build());
			} else if (field.isRelation() && field.isRelationCollection()) {
				specs.add(AnnotationSpec.builder(TypeNames.MAPPING)
						.addMember("target", "$S", field.getName())
						.addMember("expression", "$S", "java(map" + capitalize(field.getEntityFieldName())
								+ "Ids(entity.get" + capitalize(field.getEntityFieldName()) + "()))")
						.build());
			} else if (field.isRelation()) {
				TypeElement related = processingEnv.getElementUtils()
						.getTypeElement(field.getRelationEntityType().canonicalName());
				String source = related != null
						? Util.idSourcePath(field.getEntityFieldName(), related)
						: field.getEntityFieldName() + ".id";
				specs.add(AnnotationSpec.builder(TypeNames.MAPPING)
						.addMember("target", "$S", field.getName())
						.addMember("source", "$S", source)
						.build());
			}
		}
		addMappingAnnotations(method, specs);
	}

	private static MethodSpec buildCollectionIdMapper(DTOFieldElement field, ProcessingEnvironment processingEnv) {
		String entityFieldCap = capitalize(field.getEntityFieldName());
		TypeMirror entityCollectionType = field.getElement().asType();
		TypeElement related = processingEnv.getElementUtils()
				.getTypeElement(field.getRelationEntityType().canonicalName());
		String idGetter = related != null ? Util.idGetterExpression("item", related) : "item.getId()";
		MethodSpec.Builder method = MethodSpec.methodBuilder("map" + entityFieldCap + "Ids")
				.addModifiers(Modifier.DEFAULT, Modifier.PUBLIC)
				.addParameter(com.squareup.javapoet.TypeName.get(entityCollectionType), field.getEntityFieldName())
				.returns(ParameterizedTypeName.get(ClassName.get(List.class), field.getRelationIdTypeName()));
		method.addStatement("$T<$T> ids = new $T<>()", List.class, field.getRelationIdTypeName(), ArrayList.class)
				.beginControlFlow("if ($L == null)", field.getEntityFieldName())
				.addStatement("return null")
				.endControlFlow()
				.addCode("for ($T item : $L) {\n", field.getRelationEntityType(), field.getEntityFieldName())
				.addStatement("  if (item != null)")
				.addStatement("    ids.add($L)", idGetter)
				.addCode("}\n")
				.addStatement("return ids");
		return method.build();
	}

	private static void addEntityIgnoreMappings(MethodSpec.Builder method, DTOElement dto) {
		List<AnnotationSpec> specs = new ArrayList<>();
		for (DTOFieldElement field : relationFields(dto)) {
			specs.add(AnnotationSpec.builder(TypeNames.MAPPING)
					.addMember("target", "$S", field.getEntityFieldName())
					.addMember("ignore", "$L", true)
					.build());
		}
		addMappingAnnotations(method, specs);
	}

	private static void addMappingAnnotations(MethodSpec.Builder method, List<AnnotationSpec> specs) {
		if (specs.isEmpty()) {
			return;
		}
		if (specs.size() == 1) {
			method.addAnnotation(specs.get(0));
		} else {
			AnnotationSpec.Builder mappings = AnnotationSpec.builder(TypeNames.MAPPINGS);
			for (AnnotationSpec spec : specs) {
				mappings.addMember("value", "$L", spec);
			}
			method.addAnnotation(mappings.build());
		}
	}

	private static String capitalize(String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

}
