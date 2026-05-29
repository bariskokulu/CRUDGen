package com.example.crudgen.complex;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.example.crudgen.complex.gen.MegaProductStore;

class MegaProductWebTest extends WebTestBase {

	private static final String PRODUCT_JSON = "{\"displayTitle\":\"Phone\",\"sku\":\"SKU-1\",\"category\":\"gadgets\"}";

	@Autowired
	private MegaProductStore megaProductStore;

	@BeforeEach
	void cleanProducts() {
		megaProductStore.deleteAll();
	}

	@Test
	void crudAndQueryEndpoints() throws Exception {
		mockMvc.perform(post("/api/mega-products").contentType(MediaType.APPLICATION_JSON).content(PRODUCT_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.sku").value("SKU-1"))
				.andExpect(jsonPath("$.category").value("gadgets"));

		mockMvc.perform(get("/api/mega-products/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sku").value("SKU-1"));

		mockMvc.perform(get("/api/mega-products/"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));

		mockMvc.perform(get("/api/mega-products/paged?page=0&size=5"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)));

		mockMvc.perform(get("/api/mega-products/findBySku?sku=SKU-1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sku").value("SKU-1"));

		mockMvc.perform(get("/api/mega-products/findBySku?sku=missing"))
				.andExpect(status().isNotFound());

		mockMvc.perform(get("/api/mega-products/findAllByCategory?category=gadgets"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)));

		mockMvc.perform(get("/api/mega-products/findAllByCategory/paged?category=gadgets&page=0&size=5"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)));

		String patch = "[{\"op\":\"replace\",\"path\":\"/sku\",\"value\":\"SKU-2\"}]";
		mockMvc.perform(patch("/api/mega-products/1")
						.contentType("application/json-patch+json")
						.content(patch))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sku").value("SKU-2"));

		mockMvc.perform(delete("/api/mega-products/1"))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/mega-products/1"))
				.andExpect(status().isNotFound());
	}

	@Test
	void batchCreateAndDelete() throws Exception {
		String batch = "[{\"displayTitle\":\"A\",\"sku\":\"A1\",\"category\":\"x\"},{\"displayTitle\":\"B\",\"sku\":\"B1\",\"category\":\"x\"}]";
		mockMvc.perform(post("/api/mega-products/batch").contentType(MediaType.APPLICATION_JSON).content(batch))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$", hasSize(2)));

		String idsJson = megaProductStore.findAll().stream()
				.map(p -> p.getId().toString())
				.collect(java.util.stream.Collectors.joining(",", "[", "]"));

		mockMvc.perform(delete("/api/mega-products/batch")
						.contentType(MediaType.APPLICATION_JSON)
						.content(idsJson))
				.andExpect(status().isNoContent());
	}

	@Test
	void batchPatch() throws Exception {
		String batch = "[{\"displayTitle\":\"A\",\"sku\":\"A1\",\"category\":\"x\"},{\"displayTitle\":\"B\",\"sku\":\"B1\",\"category\":\"x\"}]";
		mockMvc.perform(post("/api/mega-products/batch").contentType(MediaType.APPLICATION_JSON).content(batch))
				.andExpect(status().isCreated());

		var products = megaProductStore.findAll();
		Long id1 = products.get(0).getId();
		Long id2 = products.get(1).getId();

		String patches = "{\"" + id1 + "\":[{\"op\":\"replace\",\"path\":\"/sku\",\"value\":\"A1-new\"}],\"" + id2
				+ "\":[{\"op\":\"replace\",\"path\":\"/sku\",\"value\":\"B1-new\"}]}";

		mockMvc.perform(patch("/api/mega-products/batch")
						.contentType("application/json-patch+json")
						.content(patches))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)));

		mockMvc.perform(get("/api/mega-products/findBySku?sku=A1-new"))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/mega-products/findBySku?sku=B1-new"))
				.andExpect(status().isOk());
	}

}
