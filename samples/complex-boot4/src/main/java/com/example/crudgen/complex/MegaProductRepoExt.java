package com.example.crudgen.complex;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MegaProductRepoExt extends JpaRepository<MegaProduct, Long>, JpaSpecificationExecutor<MegaProduct> {
}
