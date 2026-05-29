package com.example.crudgen.complex.support;

import com.bariskokulu.crudgen.security.CrudGenSecurityService;

public class AllowAllSecurityService implements CrudGenSecurityService {

	@Override
	public void checkEntityAccess(String entityClassName, String method, Object... params) {
	}

	@Override
	public void checkUseAccess(String method, Object... params) {
	}

}
