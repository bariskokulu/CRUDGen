package com.example.crudgen.complex;

import com.bariskokulu.crudgen.annotation.CrudGen;
import com.bariskokulu.crudgen.annotation.DTOField;
import com.example.crudgen.complex.gen.ComplexManualShelfService;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
@CrudGen(
		packageName = "com.example.crudgen.complex.gen",
		controllerPath = "/api/manual-shelves",
		dtos = { "Read" },
		customService = ComplexManualShelfService.class,
		securityService = true,
		lifecycleHooks = true,
		openApi = true,
		logging = true
)
public class ComplexManualShelf {

	@Id
	private Long id;

	@DTOField(dto = "Read")
	private String code;

}
