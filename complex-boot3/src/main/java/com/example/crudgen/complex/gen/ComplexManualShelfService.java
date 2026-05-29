package com.example.crudgen.complex.gen;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.crudgen.complex.ComplexManualShelf;

@Service
public class ComplexManualShelfService {

	private final ComplexManualShelfRepository repo;

	public ComplexManualShelfService(ComplexManualShelfRepository repo) {
		this.repo = repo;
	}

	public ComplexManualShelf get(Long id) {
		return repo.findById(id).orElse(null);
	}

	public List<ComplexManualShelf> getAll() {
		return repo.findAll();
	}

	public Page<ComplexManualShelf> getPaged(Pageable pageable) {
		return repo.findAll(pageable);
	}

	@Transactional
	public void delete(Long id) {
		repo.deleteById(id);
	}

	@Transactional
	public void deleteAll(List<Long> ids) {
		repo.deleteAllById(ids);
	}

}
