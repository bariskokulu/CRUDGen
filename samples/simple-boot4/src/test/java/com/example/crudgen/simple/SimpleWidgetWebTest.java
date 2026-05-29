package com.example.crudgen.simple;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(classes = SimpleBoot4TestApplication.class)
@ActiveProfiles("test")
class SimpleWidgetWebTest {

	@Autowired
	private WebApplicationContext context;

	@Autowired
	private SimpleWidgetRepository widgetRepository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		widgetRepository.deleteAll();
		jdbcTemplate.execute("ALTER TABLE simple_widget ALTER COLUMN id RESTART WITH 1");
		mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
	}

	@Test
	void listIsEmptyInitially() throws Exception {
		mockMvc.perform(get("/api/widgets/"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(0)));
	}

	@Test
	void createReturns201AndGetByIdWorks() throws Exception {
		mockMvc.perform(post("/api/widgets")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"alpha\"}"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.name").value("alpha"));
		mockMvc.perform(get("/api/widgets/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("alpha"));
	}

	@Test
	void createRejectsBlankName() throws Exception {
		mockMvc.perform(post("/api/widgets")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void getMissingIdReturns404() throws Exception {
		mockMvc.perform(get("/api/widgets/99"))
				.andExpect(status().isNotFound());
	}

	@Test
	void pagedEndpointWorks() throws Exception {
		mockMvc.perform(post("/api/widgets")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"paged\"}"))
				.andExpect(status().isCreated());
		mockMvc.perform(get("/api/widgets/paged?page=0&size=10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content", hasSize(1)))
				.andExpect(jsonPath("$.content[0].name").value("paged"));
	}

	@Test
	void deleteReturns204() throws Exception {
		mockMvc.perform(post("/api/widgets")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"name\":\"gone\"}"))
				.andExpect(status().isCreated());
		mockMvc.perform(delete("/api/widgets/1"))
				.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/widgets/1"))
				.andExpect(status().isNotFound());
	}

}
