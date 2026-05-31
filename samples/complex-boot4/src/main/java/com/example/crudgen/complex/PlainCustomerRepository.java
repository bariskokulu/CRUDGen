package com.example.crudgen.complex;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PlainCustomerRepository {

	Optional<PlainCustomer> findById(Long id);

	List<PlainCustomer> findAllById(Iterable<Long> ids);

	List<PlainCustomer> findAll();

	Page<PlainCustomer> findAll(Pageable pageable);

	PlainCustomer save(PlainCustomer entity);

	List<PlainCustomer> saveAll(List<PlainCustomer> entities);

	void deleteById(Long id);

	void deleteAllById(List<Long> ids);

}
