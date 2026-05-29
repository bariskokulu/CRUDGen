package com.example.crudgen.simple;

import com.bariskokulu.crudgen.annotation.CrudGen;
import com.bariskokulu.crudgen.annotation.DTOField;

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
		controllerPath = "/api/widgets",
		dtos = { "Read", "Create" },
		securityService = false,
		lifecycleHooks = false,
		openApi = false,
		logging = false
)
public class SimpleWidget {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@DTOField(dto = "Read")
	@DTOField(dto = "Create")
	@NotBlank
	@Size(max = 200)
	private String name;

}
