package com.example.crudgen.complex;

import com.bariskokulu.crudgen.annotation.CrudGen;
import com.bariskokulu.crudgen.annotation.DTOField;
import com.bariskokulu.crudgen.annotation.simple.FindAllBy;
import com.bariskokulu.crudgen.annotation.simple.FindBy;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@CrudGen(
		packageName = "com.example.crudgen.complex.gen",
		controllerPath = "/api/mega-products",
		repositoryName = "MegaProductDataStore",
		serviceName = "MegaProductDomainService",
		controllerName = "MegaProductRestPort",
		dtos = { "Read", "Create", "Update" },
		extendRepo = MegaProductRepositoryBase.class,
		extendService = MegaProductServiceMixin.class,
		extendController = MegaProductControllerMixin.class,
		securityService = true,
		lifecycleHooks = true,
		openApi = true,
		logging = true
)
public class MegaProduct {

	@Id
	private Long id;

	@DTOField(dto = "Read", fieldName = "displayTitle")
	@DTOField(dto = "Create", fieldName = "displayTitle")
	@DTOField(dto = "Update", fieldName = "displayTitle")
	private String internalTitle;

	@FindBy
	@DTOField(dto = "Read")
	@DTOField(dto = "Create")
	@DTOField(dto = "Update")
	private String sku;

	@FindAllBy
	@DTOField(dto = "Read")
	@DTOField(dto = "Create")
	@DTOField(dto = "Update")
	private String category;

}
