package com.example.crudgen.complex;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoAutoConfiguration;
import org.springframework.boot.data.mongodb.autoconfigure.DataMongoRepositoriesAutoConfiguration;
import org.springframework.boot.mongodb.autoconfigure.MongoAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import com.example.crudgen.complex.support.MongoTagTestSupport;
import com.example.crudgen.complex.support.RecordingLifecycleCallbacks;
import com.example.crudgen.complex.support.RecordingSecurityService;

@SpringBootApplication(exclude = {
		MongoAutoConfiguration.class,
		DataMongoAutoConfiguration.class,
		DataMongoRepositoriesAutoConfiguration.class
})
@ComponentScan(
		basePackages = "com.example.crudgen.complex",
		excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MongoTag.class))
@Import({ RecordingSecurityService.class, RecordingLifecycleCallbacks.class, MongoTagTestSupport.class })
public class ComplexBoot4TestApplication {
}
