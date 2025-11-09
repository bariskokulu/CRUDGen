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

	public static void saveFile(String path, TypeSpec typeSpec, ProcessingEnvironment processingEnv) {
		JavaFile javaFile = JavaFile.builder(path, typeSpec).build();
		
		// yes this is necessary because otherwise it somehow conflicts with classes generated in the previous build ( not the previous processing round )
		javaFile.toJavaFileObject().delete();
		try {
			javaFile.writeTo(processingEnv.getFiler());
		} catch (IOException e) {
			error("Failed to save file: "+path, processingEnv);
			e.printStackTrace();
		}
	}
	
}
