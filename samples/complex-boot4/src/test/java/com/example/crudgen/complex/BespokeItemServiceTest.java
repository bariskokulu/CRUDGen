package com.example.crudgen.complex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ComplexBoot4TestApplication.class)
@ActiveProfiles("test")
class BespokeItemServiceTest {

	@Autowired
	private BespokeItemService service;

	@Test
	void customControllerEntityStillGetsGeneratedService() {
		BespokeItem item = new BespokeItem();
		item.setExternalKey("k1");
		item.setPayload("data");
		BespokeItem saved = service.save(item);
		assertNotNull(saved.getId());
		assertEquals("k1", service.findByExternalKey("k1").getExternalKey());
	}

}
