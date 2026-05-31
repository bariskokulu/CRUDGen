package com.example.crudgen.complex;

import com.bariskokulu.crudgen.annotation.CrudGen;
import com.bariskokulu.crudgen.annotation.DTOField;
import com.bariskokulu.crudgen.annotation.simple.FindBy;
import com.example.crudgen.complex.custom.BespokeItemController;

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
		controllerPath = "/api/bespoke",
		customController = BespokeItemController.class,
		dtos = { "Read", "Create" },
		securityService = false,
		lifecycleHooks = false,
		openApi = true,
		logging = true)
public class BespokeItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@FindBy
	@DTOField(dto = "Read")
	@DTOField(dto = "Create")
	private String externalKey;

	@DTOField(dto = "Read")
	@DTOField(dto = "Create")
	private String payload;

}
