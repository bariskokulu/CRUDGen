package com.bariskokulu.crudgen.util;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.tools.Diagnostic;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;


public class Util {

	public ProcessingEnvironment processingEnv;
	public RoundEnvironment roundEnv;

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
	
}
