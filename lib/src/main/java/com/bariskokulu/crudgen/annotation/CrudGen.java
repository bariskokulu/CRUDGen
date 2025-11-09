package com.bariskokulu.crudgen.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.bariskokulu.crudgen.util.RepoType;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface CrudGen {

	public RepoType repo() default RepoType.JPA;
	public boolean service() default false;
	public String controllerPath() default "";
	
	public String[] dtos() default {};
	public String controllerName() default "";
	public String serviceName() default "";;
	public String repositoryName() default "";
	public String packageName() default "";
	public Class<?> customRepo() default Void.class;
	
}
