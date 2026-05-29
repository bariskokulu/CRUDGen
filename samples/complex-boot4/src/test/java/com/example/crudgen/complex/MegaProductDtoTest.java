package com.example.crudgen.complex;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.example.crudgen.complex.gen.MegaProductCreateDTO;

class MegaProductDtoTest {

	@Test
	void createDtoUsesFieldNameRemap() {
		MegaProductCreateDTO dto = new MegaProductCreateDTO("Phone", "SKU-1", "gadgets");
		assertEquals("Phone", dto.getDisplayTitle());
	}

}
