package com.bariskokulu.crudgen.processor.component;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

import com.bariskokulu.crudgen.annotation.CrudGen;
import com.bariskokulu.crudgen.annotation.DTOField;
import com.bariskokulu.crudgen.util.Util;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import lombok.Getter;

@Getter
public class DTOFieldElement extends BaseElement {

	private final Element element;
	private final TypeMirror type;
	private final TypeName typeName;
	private final String name;
	private final String nameCapitalized;
	private final boolean relation;
	private final boolean relationCollection;
	private final boolean nestedProjection;
	private final String entityFieldName;
	private final String mappingSource;
	private final ClassName relationEntityType;
	private final TypeName relationIdTypeName;

	public DTOFieldElement(Element element, DTOField annotation, ProcessingEnvironment processingEnv) {
		this(element, annotation, processingEnv, false);
	}

	private DTOFieldElement(Element element, DTOField annotation, ProcessingEnvironment processingEnv,
			boolean nestedProjection) {
		this.element = element;
		this.entityFieldName = element.getSimpleName().toString();
		this.nestedProjection = nestedProjection;
		this.mappingSource = null;
		this.relation = annotation.relation();
		if (nestedProjection) {
			throw new IllegalStateException("Use nestedProjection factory");
		}
		if (relation) {
			TypeMirror collectionElement = Util.collectionElementType(element.asType());
			if (collectionElement != null) {
				this.relationCollection = true;
				TypeElement related = Util.toTypeElement(collectionElement);
				if (related == null) {
					Util.error("@DTOField(relation=true) on collection requires a declared element type: "
							+ entityFieldName, processingEnv);
					setInvalid(true);
					this.type = element.asType();
					this.typeName = TypeName.get(this.type);
					this.relationEntityType = null;
					this.relationIdTypeName = null;
				} else {
					this.relationEntityType = ClassName.get(related);
					TypeMirror idType = Util.idTypeOf(related);
					if (idType == null) {
						Util.error("relation=true collection element " + related.getSimpleName()
								+ " has no @Id field: " + entityFieldName, processingEnv);
						setInvalid(true);
						this.type = element.asType();
						this.typeName = TypeName.get(this.type);
						this.relationIdTypeName = null;
					} else {
						this.type = idType;
						this.typeName = ParameterizedTypeName.get(ClassName.get(List.class), TypeName.get(idType));
						this.relationIdTypeName = TypeName.get(idType);
					}
				}
				this.name = Util.isBlank(annotation.fieldName()) ? entityFieldName + "Ids" : annotation.fieldName();
			} else if (Util.looksLikeEntityReference(element.asType(), processingEnv)) {
				this.relationCollection = false;
				TypeElement related = Util.toTypeElement(element.asType());
				this.relationEntityType = ClassName.get(related);
				TypeMirror idType = Util.idTypeOf(related);
				if (idType == null) {
					Util.error("relation=true target " + related.getSimpleName() + " has no @Id field: "
							+ entityFieldName, processingEnv);
					setInvalid(true);
					this.type = element.asType();
					this.typeName = TypeName.get(this.type);
					this.relationIdTypeName = null;
				} else {
					this.type = idType;
					this.typeName = TypeName.get(idType);
					this.relationIdTypeName = TypeName.get(idType);
				}
				this.name = Util.isBlank(annotation.fieldName()) ? entityFieldName + "Id" : annotation.fieldName();
			} else {
				Util.error("@DTOField(relation=true) requires a collection with @Id element type or an entity reference: "
						+ entityFieldName, processingEnv);
				setInvalid(true);
				this.relationCollection = false;
				this.type = element.asType();
				this.typeName = TypeName.get(this.type);
				this.relationEntityType = null;
				this.relationIdTypeName = null;
				this.name = entityFieldName;
			}
			if (annotation.nestedRead() && relationCollection) {
				Util.error("nestedRead is not supported on collection relation fields: " + entityFieldName, processingEnv);
				setInvalid(true);
			}
		} else {
			this.relationCollection = false;
			if (annotation.nestedRead()) {
				Util.error("nestedRead requires relation=true: " + entityFieldName, processingEnv);
				setInvalid(true);
			}
			this.type = element.asType();
			this.typeName = TypeName.get(this.type);
			this.relationEntityType = null;
			this.relationIdTypeName = null;
			this.name = Util.isBlank(annotation.fieldName()) ? entityFieldName : annotation.fieldName();
		}
		this.nameCapitalized = name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	private DTOFieldElement(Element element, String name, TypeMirror type, String mappingSource) {
		this.element = element;
		this.entityFieldName = null;
		this.relation = false;
		this.relationCollection = false;
		this.nestedProjection = true;
		this.mappingSource = mappingSource;
		this.relationEntityType = null;
		this.relationIdTypeName = null;
		this.type = type;
		this.typeName = TypeName.get(type);
		this.name = name;
		this.nameCapitalized = name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	public boolean isNestedRead() {
		if (nestedProjection || !relation || relationCollection) {
			return false;
		}
		for (DTOField annotation : element.getAnnotationsByType(DTOField.class)) {
			if (annotation.nestedRead()) {
				return true;
			}
		}
		return false;
	}

	public static List<DTOFieldElement> nestedReadProjections(DTOFieldElement relationField, TypeElement relatedEntity,
			ProcessingEnvironment processingEnv) {
		List<DTOFieldElement> projections = new ArrayList<>();
		if (relatedEntity == null) {
			return projections;
		}
		String prefix = relationField.getEntityFieldName();
		for (Element enclosed : relatedEntity.getEnclosedElements()) {
			if (enclosed.getKind() != ElementKind.FIELD) {
				continue;
			}
			for (DTOField annotation : enclosed.getAnnotationsByType(DTOField.class)) {
				if (!"Read".equals(annotation.dto()) || annotation.relation()) {
					continue;
				}
				String nestedEntityField = enclosed.getSimpleName().toString();
				String projectionName;
				if (Util.isBlank(annotation.fieldName())) {
					projectionName = prefix + nestedEntityField.substring(0, 1).toUpperCase() + nestedEntityField.substring(1);
				} else {
					String nestedDtoName = annotation.fieldName();
					projectionName = prefix + nestedDtoName.substring(0, 1).toUpperCase() + nestedDtoName.substring(1);
				}
				projections.add(new DTOFieldElement(enclosed, projectionName, enclosed.asType(),
						prefix + "." + nestedEntityField));
			}
		}
		return projections;
	}

	public static ClassName generatedRepositoryType(TypeElement relatedEntity, ProcessingEnvironment processingEnv) {
		CrudGen annotation = relatedEntity.getAnnotation(CrudGen.class);
		if (annotation == null) {
			return null;
		}
		try {
			Class<?> customRepo = annotation.customRepo();
			if (customRepo != null && customRepo != Void.class) {
				return ClassName.get(annotation.customRepo());
			}
		} catch (MirroredTypeException e) {
			if (e.getTypeMirror() != null && !"java.lang.Void".equals(e.getTypeMirror().toString())) {
				return (ClassName) ClassName.get(e.getTypeMirror());
			}
		}
		String pkg = Util.isBlank(annotation.packageName())
				? processingEnv.getElementUtils().getPackageOf(relatedEntity).getQualifiedName().toString()
				: annotation.packageName();
		String repositoryName = Util.isBlank(annotation.repositoryName())
				? relatedEntity.getSimpleName().toString() + "Repository"
				: annotation.repositoryName();
		return ClassName.get(pkg, repositoryName);
	}

}
