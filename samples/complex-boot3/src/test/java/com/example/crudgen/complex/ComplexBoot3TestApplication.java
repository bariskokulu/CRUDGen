package com.example.crudgen.complex;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import com.example.crudgen.complex.support.MongoTagTestSupport;
import com.example.crudgen.complex.support.RecordingLifecycleCallbacks;
import com.example.crudgen.complex.support.RecordingSecurityService;

@SpringBootApplication
@ComponentScan(
		basePackages = "com.example.crudgen.complex",
		excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MongoTag.class))
@Import({ RecordingSecurityService.class, RecordingLifecycleCallbacks.class, MongoTagTestSupport.class })
public class ComplexBoot3TestApplication {
}
