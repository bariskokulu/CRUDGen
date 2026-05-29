package com.bariskokulu.crudgen.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.bariskokulu.crudgen.util.HTTPMethod;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface Endpoint {
	
	HTTPMethod method();
	String path();
	
}