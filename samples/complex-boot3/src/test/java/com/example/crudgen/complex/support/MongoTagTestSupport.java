package com.example.crudgen.complex.support;

import static org.mockito.Mockito.mock;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.example.crudgen.complex.MongoTagRepository;

@TestConfiguration
public class MongoTagTestSupport {

	@Bean
	@Primary
	MongoTagRepository mongoTagRepository() {
		return mock(MongoTagRepository.class);
	}

}
