package com.example.crudgen.complex;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.example.crudgen.complex.gen.ManualShelfCreateDTO;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

class ManualShelfDtoTest {

	private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void createDtoHasValidation() {
		assertFalse(validator.validate(new ManualShelfCreateDTO("")).isEmpty());
		assertTrue(validator.validate(new ManualShelfCreateDTO("OK")).isEmpty());
	}

}
