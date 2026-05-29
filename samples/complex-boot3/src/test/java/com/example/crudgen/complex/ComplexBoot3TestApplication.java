package com.example.crudgen.complex;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;

import com.example.crudgen.complex.support.AllowAllSecurityService;
import com.example.crudgen.complex.support.MongoTagTestSupport;
import com.example.crudgen.complex.support.NoopLifecycleCallbacks;

@SpringBootApplication
@ComponentScan(
		basePackages = "com.example.crudgen.complex",
		excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = MongoTag.class))
@Import({ AllowAllSecurityService.class, NoopLifecycleCallbacks.class, MongoTagTestSupport.class })
public class ComplexBoot3TestApplication {
}
