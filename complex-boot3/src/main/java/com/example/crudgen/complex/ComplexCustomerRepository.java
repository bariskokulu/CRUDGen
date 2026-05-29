package com.example.crudgen.complex;

import java.util.List;
import java.util.Optional;

public interface ComplexCustomerRepository {

	Optional<ComplexCustomer> findById(Long id);

	List<ComplexCustomer> findAll();

	ComplexCustomer save(ComplexCustomer entity);

	void deleteById(Long id);

}
