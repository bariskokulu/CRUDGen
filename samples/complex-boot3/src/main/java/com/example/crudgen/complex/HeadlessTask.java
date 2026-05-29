package com.example.crudgen.complex;

import com.bariskokulu.crudgen.annotation.CrudGen;

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
		service = true,
		securityService = true,
		lifecycleHooks = true,
		openApi = false,
		logging = true
)
public class HeadlessTask {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String jobCode;

}
