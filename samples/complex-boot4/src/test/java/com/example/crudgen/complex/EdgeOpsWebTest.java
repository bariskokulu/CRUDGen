package com.example.crudgen.complex;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

class EdgeOpsWebTest extends WebTestBase {

	@Test
	void pathAndOptionalQuery() throws Exception {
		mockMvc.perform(get("/api/edge/by-code/ABC"))
				.andExpect(status().isOk())
				.andExpect(content().string("ABC"));

		mockMvc.perform(get("/api/edge/filter?tag=vip"))
				.andExpect(status().isOk())
				.andExpect(content().string("vip"));

		mockMvc.perform(get("/api/edge/filter"))
				.andExpect(status().isOk())
				.andExpect(content().string(""));
	}

}
