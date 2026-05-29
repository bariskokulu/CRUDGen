package com.example.crudgen.complex;

import com.bariskokulu.crudgen.annotation.CrudGen;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@CrudGen(
		service = true,
		controllerPath = "",
		securityService = true,
		lifecycleHooks = true,
		openApi = false,
		logging = true
)
public class ComplexHeadlessJob {

	@Id
	private Long id;

	private String jobCode;

}
