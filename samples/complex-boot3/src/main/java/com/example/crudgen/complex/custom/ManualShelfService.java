package com.example.crudgen.complex.custom;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.crudgen.complex.ManualShelf;
import com.example.crudgen.complex.gen.ManualShelfRepository;

@Service
public class ManualShelfService {

	private final ManualShelfRepository repo;

	public ManualShelfService(ManualShelfRepository repo) {
		this.repo = repo;
	}

	public ManualShelf get(Long id) {
		return repo.findById(id).orElse(null);
	}

	public List<ManualShelf> getAll() {
		return repo.findAll();
	}

	public Page<ManualShelf> getPaged(Pageable pageable) {
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

	@Transactional
	public ManualShelf save(ManualShelf entity) {
		return repo.save(entity);
	}

	@Transactional
	public List<ManualShelf> saveAll(List<ManualShelf> entities) {
		return repo.saveAll(entities);
	}

}
