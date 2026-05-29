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
		service = true,
		customRepo = PlainCustomerRepositoryImpl.class,
		securityService = false,
		lifecycleHooks = false,
		openApi = false,
		logging = false
)
public class PlainCustomer {

	@Id
	private Long id;

	private String code;

}
