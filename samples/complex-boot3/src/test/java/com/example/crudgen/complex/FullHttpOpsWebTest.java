package com.example.crudgen.complex;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class FullHttpOpsWebTest extends WebTestBase {

	@Test
	void allHttpMethods() throws Exception {
		mockMvc.perform(get("/api/ops/ping"))
				.andExpect(status().isOk())
				.andExpect(content().string("pong"));

		mockMvc.perform(post("/api/ops/echo").contentType(MediaType.TEXT_PLAIN).content("hi"))
				.andExpect(status().isOk())
				.andExpect(content().string("hi"));

		mockMvc.perform(put("/api/ops/items/7").contentType(MediaType.TEXT_PLAIN).content("x"))
				.andExpect(status().isOk())
				.andExpect(content().string("7:x"));

		mockMvc.perform(patch("/api/ops/items/7").contentType(MediaType.TEXT_PLAIN).content("y"))
				.andExpect(status().isOk())
				.andExpect(content().string("7:y"));

		mockMvc.perform(delete("/api/ops/items/7"))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/ops/search?q=term&limit=3"))
				.andExpect(status().isOk())
				.andExpect(content().string("term:3"));

		mockMvc.perform(get("/api/ops/search?q=solo"))
				.andExpect(status().isOk())
				.andExpect(content().string("solo"));
	}

}
