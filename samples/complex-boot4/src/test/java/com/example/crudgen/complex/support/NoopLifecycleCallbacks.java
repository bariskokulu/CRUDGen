package com.example.crudgen.complex.support;

import com.bariskokulu.crudgen.lifecycle.EntityLifecycleCallbacks;
import com.example.crudgen.complex.MegaProduct;

import org.springframework.stereotype.Component;

@Component
public class NoopLifecycleCallbacks implements EntityLifecycleCallbacks<MegaProduct> {

	@Override
	public void beforeCreate(MegaProduct entity) {
	}

	@Override
	public void afterCreate(MegaProduct entity) {
	}

	@Override
	public void beforeUpdate(MegaProduct entity) {
	}

	@Override
	public void afterUpdate(MegaProduct entity) {
	}

	@Override
	public void beforeDelete(MegaProduct entity) {
	}

	@Override
	public void afterDelete(MegaProduct entity) {
	}

}
