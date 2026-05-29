package com.example.crudgen.complex;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.bariskokulu.crudgen.annotation.Endpoint;
import com.bariskokulu.crudgen.annotation.EndpointGen;
import com.bariskokulu.crudgen.util.HTTPMethod;

@EndpointGen(
		controllerPath = "/api/ops",
		securityService = true,
		openApi = true,
		logging = true)
@Component
public class FullHttpOps {

	@Endpoint(method = HTTPMethod.GET, path = "/ping")
	public String ping() {
		return "pong";
	}

	@Endpoint(method = HTTPMethod.POST, path = "/echo")
	public String echo(@RequestBody String body) {
		return body;
	}

	@Endpoint(method = HTTPMethod.PUT, path = "/items/{id}")
	public String putItem(@PathVariable Long id, @RequestBody String body) {
		return id + ":" + body;
	}

	@Endpoint(method = HTTPMethod.PATCH, path = "/items/{id}")
	public String patchItem(@PathVariable Long id, @RequestBody String body) {
		return id + ":" + body;
	}

	@Endpoint(method = HTTPMethod.DELETE, path = "/items/{id}")
	public void deleteItem(@PathVariable Long id) {
	}

	@Endpoint(method = HTTPMethod.GET, path = "/search")
	public String search(@RequestParam String q, @RequestParam(name = "limit", required = false) Integer limit) {
		return q + (limit == null ? "" : ":" + limit);
	}

}
