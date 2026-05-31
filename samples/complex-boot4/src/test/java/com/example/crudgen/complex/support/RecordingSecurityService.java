package com.example.crudgen.complex.support;

import java.util.ArrayList;
import java.util.List;

import com.bariskokulu.crudgen.security.CrudGenSecurityService;

public class RecordingSecurityService implements CrudGenSecurityService {

	private final List<String> entityAccessCalls = new ArrayList<>();

	public List<String> getEntityAccessCalls() {
		return entityAccessCalls;
	}

	public void reset() {
		entityAccessCalls.clear();
	}

	@Override
	public void checkEntityAccess(String entityClassName, String method, Object... params) {
		entityAccessCalls.add(entityClassName + "#" + method);
	}

	@Override
	public void checkUseAccess(String useCaseKey, Object... params) {
	}

}
