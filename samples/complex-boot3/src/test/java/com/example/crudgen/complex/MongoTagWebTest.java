package com.example.crudgen.complex;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;

class MongoTagWebTest extends WebTestBase {

	@Autowired
	private MongoTagRepository mongoTagRepository;

	@Test
	void createReturns201() throws Exception {
		when(mongoTagRepository.save(any(MongoTag.class))).thenAnswer(inv -> {
			MongoTag tag = inv.getArgument(0);
			tag.setId("tag-new");
			return tag;
		});

		mockMvc.perform(post("/api/mongo-tags")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"slug\":\"alpha\",\"realm\":\"main\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.slug").value("alpha"))
				.andExpect(jsonPath("$.realm").value("main"));
	}

	@Test
	void patchUpdateUsesMockRepository() throws Exception {
		MongoTag existing = new MongoTag();
		existing.setId("tag-1");
		existing.setSlug("old");
		existing.setRealm("main");
		when(mongoTagRepository.findById("tag-1")).thenReturn(Optional.of(existing));
		when(mongoTagRepository.save(any(MongoTag.class))).thenAnswer(inv -> inv.getArgument(0));

		String patch = "[{\"op\":\"replace\",\"path\":\"/slug\",\"value\":\"new\"}]";
		mockMvc.perform(patch("/api/mongo-tags/tag-1")
						.contentType("application/json-patch+json")
						.content(patch))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.slug").value("new"));
	}

	@Test
	void findBySlugUsesMockRepository() throws Exception {
		MongoTag tag = new MongoTag();
		tag.setId("t1");
		tag.setSlug("alpha");
		tag.setRealm("main");
		when(mongoTagRepository.findBySlug("alpha")).thenReturn(tag);

		mockMvc.perform(get("/api/mongo-tags/findBySlug?slug=alpha"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.slug").value("alpha"));
	}

	@Test
	void findBySlugMissingReturns404() throws Exception {
		when(mongoTagRepository.findBySlug("missing")).thenReturn(null);

		mockMvc.perform(get("/api/mongo-tags/findBySlug?slug=missing"))
				.andExpect(status().isNotFound());
	}

	@Test
	void findAllByRealmUsesMockRepository() throws Exception {
		MongoTag tag = new MongoTag();
		tag.setId("t1");
		tag.setSlug("alpha");
		tag.setRealm("main");
		when(mongoTagRepository.findAllByRealm("main")).thenReturn(List.of(tag));

		mockMvc.perform(get("/api/mongo-tags/findAllByRealm?realm=main"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].slug").value("alpha"));
	}

	@Test
	void findAllByRealmPagedUsesMockRepository() throws Exception {
		MongoTag tag = new MongoTag();
		tag.setId("t1");
		tag.setSlug("alpha");
		tag.setRealm("main");
		when(mongoTagRepository.findAllByRealm(eq("main"), any(Pageable.class)))
				.thenReturn(new PageImpl<>(List.of(tag)));

		mockMvc.perform(get("/api/mongo-tags/findAllByRealm/paged?realm=main&page=0&size=5"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].slug").value("alpha"));
	}

}
