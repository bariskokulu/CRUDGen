package com.example.crudgen.complex;

import com.bariskokulu.crudgen.annotation.CrudGen;
import com.bariskokulu.crudgen.annotation.DTOField;
import com.example.crudgen.complex.custom.ManualShelfService;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@CrudGen(
		packageName = "com.example.crudgen.complex.gen",
		controllerPath = "/api/manual-shelves",
		dtos = { "Read", "Create" },
		customService = ManualShelfService.class,
		securityService = true,
		lifecycleHooks = true,
		openApi = true,
		logging = true
)
public class ManualShelf {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@DTOField(dto = "Read")
	@DTOField(dto = "Create")
	@NotBlank
	@Size(max = 32)
	private String code;

}
