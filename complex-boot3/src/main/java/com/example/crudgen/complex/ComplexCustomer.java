package com.example.crudgen.complex;

import com.bariskokulu.crudgen.annotation.CrudGen;
import com.bariskokulu.crudgen.util.RepoType;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@CrudGen(
		repo = RepoType.PLAIN,
		customRepo = ComplexCustomerRepositoryStub.class,
		securityService = false,
		lifecycleHooks = false,
		openApi = false,
		logging = false
)
public class ComplexCustomer {

	@Id
	private Long id;

	private String code;

}
