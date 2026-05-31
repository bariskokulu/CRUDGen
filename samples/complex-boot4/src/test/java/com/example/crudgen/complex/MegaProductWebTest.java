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

	@Autowired
	private ProductCategoryRepository productCategoryRepository;

	@Autowired
	private ProductTagRepository productTagRepository;

	@BeforeEach
	void cleanProducts() {
		megaProductStore.deleteAll();
		productTagRepository.deleteAll();
		productCategoryRepository.deleteAll();
	}

	@Test
	void crudAndQueryEndpoints() throws Exception {
		mockMvc.perform(post("/api/mega-products").contentType(MediaType.APPLICATION_JSON).content(PRODUCT_JSON))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.sku").value("SKU-1"))
				.andExpect(jsonPath("$.category").value("gadgets"));

		Long productId = megaProductStore.findAll().stream()
				.filter(p -> "SKU-1".equals(p.getSku()))
				.findFirst()
				.orElseThrow()
				.getId();

		mockMvc.perform(get("/api/mega-products/" + productId))
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
		mockMvc.perform(patch("/api/mega-products/" + productId)
						.contentType("application/json-patch+json")
						.content(patch))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.sku").value("SKU-2"));

		mockMvc.perform(delete("/api/mega-products/" + productId))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/mega-products/" + productId))
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

	@Test
	void manyToOneRelationRoundTrip() throws Exception {
		mockMvc.perform(post("/api/product-categories").contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"gadgets\"}"))
				.andExpect(status().isCreated());

		Long categoryId = productCategoryRepository.findAll().get(0).getId();
		String productJson = "{\"displayTitle\":\"Phone\",\"sku\":\"SKU-R\",\"category\":\"gadgets\",\"productCategoryId\":"
				+ categoryId + "}";

		mockMvc.perform(post("/api/mega-products").contentType(MediaType.APPLICATION_JSON).content(productJson))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.productCategoryId").value(categoryId.intValue()));

		Long productId = megaProductStore.findAll().stream()
				.filter(p -> "SKU-R".equals(p.getSku()))
				.findFirst()
				.orElseThrow()
				.getId();

		mockMvc.perform(get("/api/mega-products/" + productId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.productCategoryId").value(categoryId.intValue()))
				.andExpect(jsonPath("$.productCategoryCode").value("gadgets"));
	}

	@Test
	void oneToManyRelationRoundTrip() throws Exception {
		mockMvc.perform(post("/api/product-tags").contentType(MediaType.APPLICATION_JSON).content("{\"label\":\"red\"}"))
				.andExpect(status().isCreated());
		mockMvc.perform(post("/api/product-tags").contentType(MediaType.APPLICATION_JSON).content("{\"label\":\"blue\"}"))
				.andExpect(status().isCreated());

		Long tagId1 = productTagRepository.findAll().stream().filter(t -> "red".equals(t.getLabel())).findFirst().orElseThrow().getId();
		Long tagId2 = productTagRepository.findAll().stream().filter(t -> "blue".equals(t.getLabel())).findFirst().orElseThrow().getId();

		String productJson = "{\"displayTitle\":\"Tagged\",\"sku\":\"SKU-T\",\"category\":\"misc\",\"tagsIds\":[" + tagId1 + "," + tagId2 + "]}";
		mockMvc.perform(post("/api/mega-products").contentType(MediaType.APPLICATION_JSON).content(productJson))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.tagsIds", hasSize(2)));

		Long productId = megaProductStore.findAll().stream()
				.filter(p -> "SKU-T".equals(p.getSku()))
				.findFirst()
				.orElseThrow()
				.getId();

		mockMvc.perform(get("/api/mega-products/" + productId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.tagsIds", hasSize(2)));
	}

	@Test
	void patchUpdatesRelations() throws Exception {
		mockMvc.perform(post("/api/product-categories").contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"old\"}"))
				.andExpect(status().isCreated());
		mockMvc.perform(post("/api/product-categories").contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"new\"}"))
				.andExpect(status().isCreated());

		Long oldCatId = productCategoryRepository.findAll().stream().filter(c -> "old".equals(c.getCode())).findFirst().orElseThrow().getId();
		Long newCatId = productCategoryRepository.findAll().stream().filter(c -> "new".equals(c.getCode())).findFirst().orElseThrow().getId();

		mockMvc.perform(post("/api/mega-products").contentType(MediaType.APPLICATION_JSON)
						.content("{\"displayTitle\":\"P\",\"sku\":\"SKU-P\",\"category\":\"misc\",\"productCategoryId\":" + oldCatId + "}"))
				.andExpect(status().isCreated());

		Long productId = megaProductStore.findAll().stream().filter(p -> "SKU-P".equals(p.getSku())).findFirst().orElseThrow().getId();

		String patch = "[{\"op\":\"replace\",\"path\":\"/productCategoryId\",\"value\":" + newCatId + "}]";
		mockMvc.perform(patch("/api/mega-products/" + productId)
						.contentType("application/json-patch+json")
						.content(patch))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.productCategoryId").value(newCatId.intValue()))
				.andExpect(jsonPath("$.productCategoryCode").value("new"));
	}

	@Test
	void invalidRelationFkReturnsBadRequest() throws Exception {
		mockMvc.perform(post("/api/mega-products").contentType(MediaType.APPLICATION_JSON)
						.content("{\"displayTitle\":\"Bad\",\"sku\":\"SKU-BAD\",\"category\":\"x\",\"productCategoryId\":999999}"))
				.andExpect(status().isBadRequest());
	}

}
