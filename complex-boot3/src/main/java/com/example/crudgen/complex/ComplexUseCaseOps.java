package com.example.crudgen.complex;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.bariskokulu.crudgen.annotation.Endpoint;
import com.bariskokulu.crudgen.annotation.EndpointGen;
import com.bariskokulu.crudgen.util.HTTPMethod;

@EndpointGen(
		controllerPath = "/api/complex-ops",
		securityService = true,
		openApi = true,
		logging = true
)
public class ComplexUseCaseOps {

	@Endpoint(method = HTTPMethod.GET, path = "/ping")
	public String ping() {
		return "pong";
	}

	@Endpoint(method = HTTPMethod.POST, path = "/echo")
	public String echo(@RequestBody String body) {
		return body;
	}

	@Endpoint(method = HTTPMethod.PUT, path = "/put/{id}")
	public String putBody(@PathVariable Long id, @RequestBody String body) {
		return id + body;
	}

	@Endpoint(method = HTTPMethod.PATCH, path = "/patch/{id}")
	public String patchBody(@PathVariable Long id, @RequestBody String body) {
		return id + body;
	}

	@Endpoint(method = HTTPMethod.DELETE, path = "/gone/{id}")
	public void remove(@PathVariable Long id) {
	}

	@Endpoint(method = HTTPMethod.GET, path = "/query-mix")
	public String queryMix(@RequestParam String a, @RequestParam(name = "b", required = false) String b) {
		return a + (b == null ? "" : b);
	}

}
