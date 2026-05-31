package com.example.crudgen.complex;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.example.crudgen.complex.gen.MegaProductStore;
import com.example.crudgen.complex.support.RecordingLifecycleCallbacks;
import com.example.crudgen.complex.support.RecordingSecurityService;

class MegaProductHooksWebTest extends WebTestBase {

	private static final String PRODUCT_JSON = "{\"displayTitle\":\"Hooked\",\"sku\":\"SKU-H\",\"category\":\"misc\"}";

	@Autowired
	private MegaProductStore megaProductStore;

	@Autowired
	private RecordingLifecycleCallbacks lifecycleCallbacks;

	@Autowired
	private RecordingSecurityService securityService;

	@BeforeEach
	void reset() {
		megaProductStore.deleteAll();
		lifecycleCallbacks.reset();
		securityService.reset();
	}

	@Test
	void createInvokesSecurityAndLifecycleHooks() throws Exception {
		mockMvc.perform(post("/api/mega-products").contentType(MediaType.APPLICATION_JSON).content(PRODUCT_JSON))
				.andExpect(status().isCreated());

		assertTrue(lifecycleCallbacks.getBeforeCreateCount() >= 1);
		assertTrue(lifecycleCallbacks.getAfterCreateCount() >= 1);
		assertTrue(securityService.getEntityAccessCalls().stream()
				.anyMatch(call -> call.contains("MegaProduct") && call.endsWith("#create")));
	}

}
