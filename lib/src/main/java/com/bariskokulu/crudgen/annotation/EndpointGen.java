package com.bariskokulu.crudgen.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface EndpointGen {

	public String controllerPath();
	public String controllerName() default "";
	public String packageName() default "";
	
	public boolean securityService() default true;
	public boolean logging() default true;
	public boolean openApi() default true;
	
}
