package com.bariskokulu.crudgen.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
@Repeatable(DTOField.List.class)
public @interface DTOField {

	String dto();
	String fieldName() default "";
	boolean relation() default false;
	boolean nestedRead() default false;

	@Documented
	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.CLASS)
	public @interface List {
		DTOField[] value();
	}

}