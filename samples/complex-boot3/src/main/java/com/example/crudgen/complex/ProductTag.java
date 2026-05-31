package com.example.crudgen.complex;

import com.bariskokulu.crudgen.annotation.CrudGen;
import com.bariskokulu.crudgen.annotation.DTOField;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@CrudGen(
		controllerPath = "/api/product-tags",
		dtos = { "Read", "Create" },
		securityService = false,
		lifecycleHooks = false,
		openApi = false,
		logging = false
)
public class ProductTag {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@DTOField(dto = "Read")
	@DTOField(dto = "Create")
	private String label;

}
