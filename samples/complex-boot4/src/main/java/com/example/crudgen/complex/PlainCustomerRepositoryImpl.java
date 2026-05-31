package com.example.crudgen.complex;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class PlainCustomerRepositoryImpl implements PlainCustomerRepository {

	private final Map<Long, PlainCustomer> store = new ConcurrentHashMap<>();
	private final AtomicLong seq = new AtomicLong(1);

	@Override
	public Optional<PlainCustomer> findById(Long id) {
		return Optional.ofNullable(store.get(id));
	}

	@Override
	public List<PlainCustomer> findAllById(Iterable<Long> ids) {
		List<PlainCustomer> result = new ArrayList<>();
		for (Long id : ids) {
			PlainCustomer customer = store.get(id);
			if (customer != null) {
				result.add(customer);
			}
		}
		return result;
	}

	@Override
	public List<PlainCustomer> findAll() {
		return new ArrayList<>(store.values());
	}

	@Override
	public Page<PlainCustomer> findAll(Pageable pageable) {
		List<PlainCustomer> all = findAll();
		int start = (int) pageable.getOffset();
		int end = Math.min(start + pageable.getPageSize(), all.size());
		List<PlainCustomer> slice = start >= all.size() ? List.of() : all.subList(start, end);
		return new PageImpl<>(slice, pageable, all.size());
	}

	@Override
	public PlainCustomer save(PlainCustomer entity) {
		Long id = entity.getId();
		if (id == null) {
			id = seq.getAndIncrement();
		}
		PlainCustomer copy = new PlainCustomer();
		copy.setId(id);
		copy.setCode(entity.getCode());
		store.put(id, copy);
		return copy;
	}

	@Override
	public List<PlainCustomer> saveAll(List<PlainCustomer> entities) {
		return entities.stream().map(this::save).collect(Collectors.toList());
	}

	@Override
	public void deleteById(Long id) {
		store.remove(id);
	}

	@Override
	public void deleteAllById(List<Long> ids) {
		ids.forEach(store::remove);
	}

}
