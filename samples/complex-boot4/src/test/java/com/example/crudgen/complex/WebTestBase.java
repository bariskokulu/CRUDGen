package com.example.crudgen.complex;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = ComplexBoot4TestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class WebTestBase {

	@Autowired
	protected MockMvc mockMvc;

	@BeforeEach
	void resetDatabase() {
	}

}
