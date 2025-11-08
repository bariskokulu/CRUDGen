package com.bariskokulu.crudgen.util;

import java.io.IOException;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.tools.Diagnostic;

import org.springframework.javapoet.JavaFile;
import org.springframework.javapoet.TypeSpec;


public class Util {

	private static Util instance;
	
	public static Util instance() {
		if(instance == null) instance = new Util();
		return instance;
	}
	
	public ProcessingEnvironment processingEnv;
	public RoundEnvironment roundEnv;

	public void log(String text) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, text);
	}

	public void warn(String text) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, text);
	}

	public void error(String text) {
		processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, text);
	}

	public void saveFile(String path, TypeSpec typeSpec) {
		JavaFile javaFile = JavaFile.builder(path, typeSpec).build();
		javaFile.toJavaFileObject().delete();
		try {
			javaFile.writeTo(processingEnv.getFiler());
		} catch (IOException e) {
			error("Failed to save file: "+path);
			e.printStackTrace();
		}
	}
	
}
