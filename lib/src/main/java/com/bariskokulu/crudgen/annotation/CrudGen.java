package com.bariskokulu.crudgen.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.bariskokulu.crudgen.util.RepoType;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface CrudGen {

	public RepoType repo() default RepoType.JPA;
	public boolean service() default false;
	public String controllerPath() default "";
	
	public String[] dtos() default {};
	public String controllerName() default "";
	public String serviceName() default "";
	public String repositoryName() default "";
	public String packageName() default "";
	
	public Class<?> customRepo() default Void.class;
	public Class<?> customController() default Void.class;
	public Class<?> customService() default Void.class;
	
	public Class<?> extendRepo() default Void.class;
	public Class<?> extendController() default Void.class;
	public Class<?> extendService() default Void.class;
	
	public boolean securityService() default true;
	public boolean logging() default true;
	public boolean openApi() default true;
	public boolean lifecycleHooks() default true;
	
}
