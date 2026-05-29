package com.example.crudgen.complex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ComplexBoot4TestApplication.class)
@ActiveProfiles("test")
class HeadlessTaskServiceTest {

	@Autowired
	private HeadlessTaskService service;

	@Test
	void serviceOnlyEntityPersists() {
		HeadlessTask task = new HeadlessTask();
		task.setJobCode("JOB-1");
		HeadlessTask saved = service.save(task);
		assertNotNull(saved.getId());
		HeadlessTask loaded = service.get(saved.getId());
		assertEquals("JOB-1", loaded.getJobCode());
	}

}
