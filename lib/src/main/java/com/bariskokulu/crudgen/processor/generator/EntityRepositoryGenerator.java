package com.bariskokulu.crudgen.processor.generator;

import java.util.List;
import java.util.Optional;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import com.bariskokulu.crudgen.processor.component.EntityElement;
import com.bariskokulu.crudgen.processor.component.FieldElement;
import com.bariskokulu.crudgen.util.TypeNames;
import com.bariskokulu.crudgen.util.Util;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

public class EntityRepositoryGenerator {

	public static void generate(EntityElement element, ProcessingEnvironment processingEnv) {
		if(element.getCustomRepoTypeName() != null) return;
		if (element.getExtendRepoTypeName() != null) {
			validateExtendRepo(element, processingEnv);
		}
		TypeSpec.Builder clazz = TypeSpec.interfaceBuilder(element.getRepositoryName())
				.addAnnotation(AnnotationSpec.builder(TypeNames.REPOSITORY).build())
				.addModifiers(Modifier.PUBLIC);
		if(element.getExtendRepoTypeName() != null) {
			clazz.addSuperinterface(element.getExtendRepoTypeName());
		} else {
			switch(element.getRepoType()) {
			case JPA:
				clazz.addSuperinterface(ParameterizedTypeName.get(
						TypeNames.JPA_REPOSITORY,
						element.getTypeName(),
						element.getIdTypeName()
						))
				.addSuperinterface(ParameterizedTypeName.get(
						TypeNames.JPA_SPEC_EXECUTOR,
						element.getTypeName()
						));
				break;
			case MONGO:
				clazz.addSuperinterface(ParameterizedTypeName.get(
						TypeNames.MONGO_REPOSITORY,
						element.getTypeName(),
						element.getIdTypeName()
						));
				break;
			case PLAIN:
				addPlainPersistenceContract(element, clazz);
				break;
			}
		}
		for(FieldElement field : element.getFields()) {
			if(field.isFindBy()) {
				clazz.addMethod(MethodSpec.methodBuilder("findBy"+field.getNameCapitalized())
						.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
						.addParameter(ClassName.get(field.getType()), field.getName())
						.returns(element.getTypeName())
						.build());
			}
			if(field.isFindAllBy()) {
				clazz.addMethod(MethodSpec.methodBuilder("findAllBy"+field.getNameCapitalized())
						.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
						.addParameter(ClassName.get(field.getType()), field.getName())
						.returns(ParameterizedTypeName.get(ClassName.get(List.class), element.getTypeName()))
						.build());
				clazz.addMethod(MethodSpec.methodBuilder("findAllBy"+field.getNameCapitalized())
						.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
						.addParameter(ClassName.get(field.getType()), field.getName())
						.addParameter(TypeNames.PAGEABLE, "pageable")
						.returns(ParameterizedTypeName.get(TypeNames.PAGE, element.getTypeName()))
						.build());
			}
		}
		Util.saveFile(element.getPackageName(), clazz.build(), processingEnv);
	}

	private static void validateExtendRepo(EntityElement element, ProcessingEnvironment processingEnv) {
		if (element.getRepoType() == com.bariskokulu.crudgen.util.RepoType.PLAIN) {
			return;
		}
		TypeElement extend = processingEnv.getElementUtils()
				.getTypeElement(element.getExtendRepoTypeName().toString());
		if (extend == null) {
			return;
		}
		String requiredFqn = element.getRepoType() == com.bariskokulu.crudgen.util.RepoType.MONGO
				? TypeNames.MONGO_REPOSITORY.canonicalName()
				: TypeNames.JPA_REPOSITORY.canonicalName();
		TypeElement required = processingEnv.getElementUtils().getTypeElement(requiredFqn);
		if (required == null) {
			return;
		}
		if (!declaresSuperinterface(extend, requiredFqn, processingEnv)) {
			Util.error(element.getName() + ": extendRepo " + element.getExtendRepoTypeName()
					+ " must extend " + requiredFqn
					+ ". When extendRepo is set, the generated repository extends only extendRepo (not "
					+ element.getRepoType() + " defaults).", processingEnv);
		}
	}

	private static boolean declaresSuperinterface(TypeElement type, String requiredFqn, ProcessingEnvironment processingEnv) {
		if (type == null) {
			return false;
		}
		if (requiredFqn.equals(type.getQualifiedName().toString())) {
			return true;
		}
		TypeMirror superClass = type.getSuperclass();
		if (superClass.getKind() == TypeKind.DECLARED) {
			TypeElement superType = (TypeElement) ((DeclaredType) superClass).asElement();
			if (declaresSuperinterface(superType, requiredFqn, processingEnv)) {
				return true;
			}
		}
		for (TypeMirror iface : type.getInterfaces()) {
			if (iface.getKind() == TypeKind.DECLARED) {
				TypeElement ifaceType = (TypeElement) ((DeclaredType) iface).asElement();
				if (declaresSuperinterface(ifaceType, requiredFqn, processingEnv)) {
					return true;
				}
			}
		}
		return false;
	}

	private static void addPlainPersistenceContract(EntityElement element, TypeSpec.Builder clazz) {
		ClassName optional = ClassName.get(Optional.class);
		ClassName list = ClassName.get(List.class);
		clazz.addMethod(MethodSpec.methodBuilder("findById")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addParameter(element.getIdTypeName(), "id")
				.returns(ParameterizedTypeName.get(optional, element.getTypeName()))
				.build());
		clazz.addMethod(MethodSpec.methodBuilder("findAll")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.returns(ParameterizedTypeName.get(list, element.getTypeName()))
				.build());
		clazz.addMethod(MethodSpec.methodBuilder("findAll")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addParameter(TypeNames.PAGEABLE, "pageable")
				.returns(ParameterizedTypeName.get(TypeNames.PAGE, element.getTypeName()))
				.build());
		clazz.addMethod(MethodSpec.methodBuilder("save")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addParameter(element.getTypeName(), "entity")
				.returns(element.getTypeName())
				.build());
		clazz.addMethod(MethodSpec.methodBuilder("saveAll")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addParameter(ParameterizedTypeName.get(list, element.getTypeName()), "entities")
				.returns(ParameterizedTypeName.get(list, element.getTypeName()))
				.build());
		clazz.addMethod(MethodSpec.methodBuilder("deleteById")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addParameter(element.getIdTypeName(), "id")
				.returns(TypeName.VOID)
				.build());
		clazz.addMethod(MethodSpec.methodBuilder("deleteAllById")
				.addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
				.addParameter(ParameterizedTypeName.get(list, element.getIdTypeName()), "ids")
				.returns(TypeName.VOID)
				.build());
	}

}
