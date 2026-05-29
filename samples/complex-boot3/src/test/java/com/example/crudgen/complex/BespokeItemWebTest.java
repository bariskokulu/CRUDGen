package com.example.crudgen.complex;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class BespokeItemWebTest extends WebTestBase {

	@Autowired
	private BespokeItemRepository bespokeItemRepository;

	@Autowired
	private BespokeItemService bespokeItemService;

	@BeforeEach
	void cleanItems() {
		bespokeItemRepository.deleteAll();
	}

	@Test
	void customControllerUsesGeneratedService() throws Exception {
		BespokeItem item = new BespokeItem();
		item.setExternalKey("k1");
		item.setPayload("data");
		bespokeItemService.save(item);

		mockMvc.perform(get("/api/bespoke/by-key/k1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.externalKey").value("k1"))
				.andExpect(jsonPath("$.payload").value("data"));

		mockMvc.perform(get("/api/bespoke/by-key/missing"))
				.andExpect(status().isNotFound());
	}

}
