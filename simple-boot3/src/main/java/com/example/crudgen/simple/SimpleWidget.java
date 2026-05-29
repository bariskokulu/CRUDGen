package com.example.crudgen.simple;

import com.bariskokulu.crudgen.annotation.CrudGen;
import com.bariskokulu.crudgen.annotation.DTOField;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
@CrudGen(
		controllerPath = "/api/widgets",
		dtos = { "Read" },
		securityService = false,
		lifecycleHooks = false,
		openApi = false,
		logging = false
)
public class SimpleWidget {

	@Id
	private Long id;

	@DTOField(dto = "Read")
	private String name;

}
