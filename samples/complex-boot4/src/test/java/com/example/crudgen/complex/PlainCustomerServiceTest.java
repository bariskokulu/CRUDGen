package com.example.crudgen.complex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ComplexBoot4TestApplication.class)
@ActiveProfiles("test")
class PlainCustomerServiceTest {

	@Autowired
	private PlainCustomerService service;

	@Autowired
	private PlainCustomerRepositoryImpl repository;

	@BeforeEach
	void clean() {
		repository.findAll().forEach(c -> repository.deleteById(c.getId()));
	}

	@Test
	void saveAndLoadViaGeneratedService() {
		PlainCustomer draft = new PlainCustomer();
		draft.setCode("C-100");
		PlainCustomer saved = service.save(draft);
		assertNotNull(saved.getId());
		PlainCustomer loaded = service.get(saved.getId());
		assertEquals("C-100", loaded.getCode());
	}

	@Test
	void pagedAndBatchDeleteViaPlainRepo() {
		service.save(create("A"));
		service.save(create("B"));
		service.save(create("C"));

		assertEquals(2, service.getPaged(PageRequest.of(0, 2)).getContent().size());
		assertEquals(3, service.getAll().size());

		var ids = service.getAll().stream().map(PlainCustomer::getId).toList();
		service.deleteAll(ids.subList(0, 2));
		assertEquals(1, service.getAll().size());
	}

	private static PlainCustomer create(String code) {
		PlainCustomer customer = new PlainCustomer();
		customer.setCode(code);
		return customer;
	}

}
