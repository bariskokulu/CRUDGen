package com.bariskokulu.crudgen.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;


public class Util {

	public static final int MAX_BATCH_SIZE = 500;
	public static final int MAX_PAGE_SIZE = 500;

	public static List<String> pathVariableNamesFromPath(String path) {
		List<String> names = new ArrayList<>();
		if (path == null) {
			return names;
		}
		int i = 0;
		while ((i = path.indexOf('{', i)) >= 0) {
			int end = path.indexOf('}', i);
			if (end <= i) {
				break;
			}
			names.add(path.substring(i + 1, end));
			i = end + 1;
		}
		return names;
	}

	public static void log(String text, ProcessingEnvironment processingEnv) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, text);
	}

	public static void warn(String text, ProcessingEnvironment processingEnv) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, text);
	}

	public static void error(String text, ProcessingEnvironment processingEnv) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, text);
	}

	public static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	public static void saveFile(String path, TypeSpec typeSpec, ProcessingEnvironment processingEnv) {
		JavaFile javaFile = JavaFile.builder(path, typeSpec).build();
		try {
			javaFile.writeTo(processingEnv.getFiler());
		} catch (IOException e) {
			error("Failed to save file: "+path, processingEnv);
			error(e.getMessage(), processingEnv);
		}
	}
	
	public static boolean isInvalidJavaIdentifier(String name) {
	    if(name == null || name.isEmpty()) return true;
	    if(!Character.isJavaIdentifierStart(name.charAt(0))) return true;
	    for(int i = 1; i < name.length(); i++) {
	        if (!Character.isJavaIdentifierPart(name.charAt(i))) return true;
	    }
	    return false;
	}

	public static boolean isInvalidPackageName(String packageName) {
	    if(packageName == null || packageName.isEmpty()) return true;
	    String[] parts = packageName.split("\\.");
	    for(String part : parts) {
	        if(isInvalidJavaIdentifier(part)) return true;
	    }
	    return false;
	}

	public static List<AnnotationSpec> beanValidationAnnotationSpecs(Element element) {
		List<AnnotationSpec> specs = new ArrayList<>();
		for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
			if (isBeanValidationAnnotation(mirror)) {
				specs.add(AnnotationSpec.get(mirror));
			}
		}
		return specs;
	}

	public static boolean isBeanValidationAnnotation(AnnotationMirror mirror) {
		Element type = mirror.getAnnotationType().asElement();
		if (!(type instanceof TypeElement)) {
			return false;
		}
		String qn = ((TypeElement) type).getQualifiedName().toString();
		return qn.startsWith("javax.validation.") || qn.startsWith("jakarta.validation.");
	}

	public static AnnotationSpec pathVariable(String bindingName) {
		return AnnotationSpec.builder(TypeNames.PATH_VARIABLE).addMember("value", "$S", bindingName).build();
	}

	public static AnnotationSpec requestParam(String bindingName, boolean required) {
		AnnotationSpec.Builder builder = AnnotationSpec.builder(TypeNames.REQUEST_PARAM)
				.addMember("value", "$S", bindingName);
		if (!required) {
			builder.addMember("required", "$L", false);
		}
		return builder.build();
	}

	public static String springBindingName(Element element, String fallback) {
		for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
			if (!isSpringPathVariable(mirror) && !isSpringRequestParam(mirror)) {
				continue;
			}
			for (Map.Entry<? extends ExecutableElement, ? extends javax.lang.model.element.AnnotationValue> entry
					: mirror.getElementValues().entrySet()) {
				String key = entry.getKey().getSimpleName().toString();
				if ("value".equals(key) || "name".equals(key)) {
					Object value = entry.getValue().getValue();
					if (value instanceof String && !((String) value).isEmpty()) {
						return (String) value;
					}
				}
			}
		}
		return fallback;
	}

	public static boolean isSpringWebBindingAnnotation(AnnotationMirror mirror) {
		return isSpringPathVariable(mirror) || isSpringRequestParam(mirror) || isSpringRequestBody(mirror);
	}

	private static boolean isSpringPathVariable(AnnotationMirror mirror) {
		return "org.springframework.web.bind.annotation.PathVariable"
				.equals(annotationQualifiedName(mirror));
	}

	private static boolean isSpringRequestParam(AnnotationMirror mirror) {
		return "org.springframework.web.bind.annotation.RequestParam"
				.equals(annotationQualifiedName(mirror));
	}

	private static boolean isSpringRequestBody(AnnotationMirror mirror) {
		return "org.springframework.web.bind.annotation.RequestBody"
				.equals(annotationQualifiedName(mirror));
	}

	private static String annotationQualifiedName(AnnotationMirror mirror) {
		Element type = mirror.getAnnotationType().asElement();
		if (type instanceof TypeElement) {
			return ((TypeElement) type).getQualifiedName().toString();
		}
		return "";
	}

}
