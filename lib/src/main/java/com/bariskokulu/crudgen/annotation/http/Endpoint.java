package com.bariskokulu.crudgen.annotation.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.bariskokulu.crudgen.util.HTTPMethod;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface Endpoint {
	
	HTTPMethod method();
	String path();
	
}