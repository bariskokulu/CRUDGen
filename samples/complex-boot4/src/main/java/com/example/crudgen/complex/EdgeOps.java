package com.example.crudgen.complex;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.bariskokulu.crudgen.annotation.Endpoint;
import com.bariskokulu.crudgen.annotation.EndpointGen;
import com.bariskokulu.crudgen.util.HTTPMethod;

@EndpointGen(
		packageName = "com.example.crudgen.complex.gen",
		controllerName = "EdgeOpsController",
		controllerPath = "/api/edge",
		securityService = false,
		openApi = false,
		logging = false)
@Component
public class EdgeOps {

	@Endpoint(method = HTTPMethod.GET, path = "/by-code/{code}")
	public String byCode(@PathVariable String code) {
		return code;
	}

	@Endpoint(method = HTTPMethod.GET, path = "/filter")
	public String filter(@RequestParam(name = "tag", required = false) String tag) {
		return tag == null ? "" : tag;
	}

}
