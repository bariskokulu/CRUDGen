package com.example.crudgen.complex;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.bariskokulu.crudgen.annotation.CrudGen;
import com.bariskokulu.crudgen.annotation.DTOField;
import com.bariskokulu.crudgen.annotation.simple.FindAllBy;
import com.bariskokulu.crudgen.annotation.simple.FindBy;
import com.bariskokulu.crudgen.util.RepoType;

import lombok.Getter;
import lombok.Setter;

@Document(collection = "complex_mongo_tags")
@Getter
@Setter
@CrudGen(
		repo = RepoType.MONGO,
		controllerPath = "/api/mongo-tags",
		dtos = { "Read", "Create", "Update" },
		securityService = true,
		lifecycleHooks = true,
		openApi = true,
		logging = true
)
public class ComplexMongoTag {

	@Id
	private String id;

	@FindBy
	@DTOField(dto = "Read")
	@DTOField(dto = "Create")
	@DTOField(dto = "Update")
	private String slug;

	@FindAllBy
	@DTOField(dto = "Read")
	@DTOField(dto = "Create")
	@DTOField(dto = "Update")
	private String realm;

}
