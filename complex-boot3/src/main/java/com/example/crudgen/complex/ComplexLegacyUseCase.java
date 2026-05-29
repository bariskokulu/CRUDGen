package com.example.crudgen.complex;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.bariskokulu.crudgen.annotation.Endpoint;
import com.bariskokulu.crudgen.annotation.EndpointGen;
import com.bariskokulu.crudgen.util.HTTPMethod;

@EndpointGen(
		packageName = "com.example.crudgen.complex.gen",
		controllerName = "LegacyEdgeCaseController",
		controllerPath = "/api/legacy-edge",
		securityService = false,
		openApi = false,
		logging = false
)
public class ComplexLegacyUseCase {

	@Endpoint(method = HTTPMethod.GET, path = "/lookup/{code}")
	public String lookup(@PathVariable String code) {
		return code;
	}

	@Endpoint(method = HTTPMethod.GET, path = "/optional-filter")
	public String optionalFilter(@RequestParam(name = "q", required = false) String q) {
		return q == null ? "" : q;
	}

}
