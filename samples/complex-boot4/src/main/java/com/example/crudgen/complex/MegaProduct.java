package com.example.crudgen.complex;

import com.bariskokulu.crudgen.annotation.CrudGen;
import com.bariskokulu.crudgen.annotation.DTOField;
import com.bariskokulu.crudgen.annotation.simple.FindAllBy;
import com.bariskokulu.crudgen.annotation.simple.FindBy;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@CrudGen(
		packageName = "com.example.crudgen.complex.gen",
		controllerPath = "/api/mega-products",
		repositoryName = "MegaProductStore",
		serviceName = "MegaProductService",
		controllerName = "MegaProductController",
		dtos = { "Read", "Create", "Update" },
		extendRepo = MegaProductRepoExt.class,
		extendService = MegaProductServiceExt.class,
		extendController = MegaProductControllerExt.class,
		securityService = true,
		lifecycleHooks = true,
		openApi = true,
		logging = true)
public class MegaProduct {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_category_id")
	@DTOField(dto = "Read", relation = true, nestedRead = true)
	@DTOField(dto = "Create", relation = true)
	@DTOField(dto = "Update", relation = true)
	private ProductCategory productCategory;

	@OneToMany
	@JoinColumn(name = "mega_product_id")
	@DTOField(dto = "Read", relation = true)
	@DTOField(dto = "Create", relation = true)
	@DTOField(dto = "Update", relation = true)
	private List<ProductTag> tags;

}
