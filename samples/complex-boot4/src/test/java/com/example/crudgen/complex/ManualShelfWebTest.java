package com.example.crudgen.complex;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ManualShelfWebTest extends WebTestBase {

	@Test
	void listCreateAndValidate() throws Exception {
		mockMvc.perform(get("/api/manual-shelves/"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));

		mockMvc.perform(post("/api/manual-shelves")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"code\":\"A1\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.code").value("A1"));

		mockMvc.perform(post("/api/manual-shelves")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"code\":\"\"}"))
				.andExpect(status().isBadRequest());
	}

}
