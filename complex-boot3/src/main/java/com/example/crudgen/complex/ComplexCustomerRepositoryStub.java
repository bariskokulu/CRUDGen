package com.example.crudgen.complex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Repository;

@Repository
public class ComplexCustomerRepositoryStub implements ComplexCustomerRepository {

	private final Map<Long, ComplexCustomer> store = new ConcurrentHashMap<>();
	private final AtomicLong seq = new AtomicLong(1);

	@Override
	public Optional<ComplexCustomer> findById(Long id) {
		return Optional.ofNullable(store.get(id));
	}

	@Override
	public List<ComplexCustomer> findAll() {
		return new ArrayList<>(store.values());
	}

	@Override
	public ComplexCustomer save(ComplexCustomer entity) {
		Long id = entity.getId();
		if (id == null) {
			id = seq.getAndIncrement();
		}
		ComplexCustomer copy = new ComplexCustomer();
		copy.setId(id);
		copy.setCode(entity.getCode());
		store.put(id, copy);
		return copy;
	}

	@Override
	public void deleteById(Long id) {
		store.remove(id);
	}

}
