package com.example.crudgen.complex.support;

import com.bariskokulu.crudgen.lifecycle.EntityLifecycleCallbacks;
import com.example.crudgen.complex.MegaProduct;

public class RecordingLifecycleCallbacks implements EntityLifecycleCallbacks<MegaProduct> {

	private int beforeCreateCount;
	private int afterCreateCount;

	public void reset() {
		beforeCreateCount = 0;
		afterCreateCount = 0;
	}

	public int getBeforeCreateCount() {
		return beforeCreateCount;
	}

	public int getAfterCreateCount() {
		return afterCreateCount;
	}

	@Override
	public void beforeCreate(MegaProduct entity) {
		beforeCreateCount++;
	}

	@Override
	public void afterCreate(MegaProduct entity) {
		afterCreateCount++;
	}

	@Override
	public void beforeUpdate(MegaProduct existing) {
	}

	@Override
	public void afterUpdate(MegaProduct saved) {
	}

	@Override
	public void beforeDelete(MegaProduct entity) {
	}

	@Override
	public void afterDelete(MegaProduct entity) {
	}

}
