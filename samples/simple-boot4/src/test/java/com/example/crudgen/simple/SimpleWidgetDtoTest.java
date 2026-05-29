package com.example.crudgen.simple;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class SimpleWidgetDtoTest {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void createDtoMirrorsEntityValidation() {
		SimpleWidgetCreateDTO blank = new SimpleWidgetCreateDTO("");
		Set<ConstraintViolation<SimpleWidgetCreateDTO>> violations = validator.validate(blank);
		assertFalse(violations.isEmpty());

		SimpleWidgetCreateDTO ok = new SimpleWidgetCreateDTO("valid");
		assertTrue(validator.validate(ok).isEmpty());
	}

}
