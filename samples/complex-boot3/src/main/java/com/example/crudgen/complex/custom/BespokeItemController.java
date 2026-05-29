package com.example.crudgen.complex.custom;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.crudgen.complex.BespokeItem;
import com.example.crudgen.complex.BespokeItemReadDTO;
import com.example.crudgen.complex.BespokeItemService;

@RestController
@RequestMapping("/api/bespoke")
public class BespokeItemController {

	private final BespokeItemService service;

	public BespokeItemController(BespokeItemService service) {
		this.service = service;
	}

	@GetMapping("/by-key/{externalKey}")
	public ResponseEntity<BespokeItemReadDTO> byKey(@PathVariable("externalKey") String externalKey) {
		BespokeItem item = service.findByExternalKey(externalKey);
		if (item == null) {
			return ResponseEntity.notFound().build();
		}
		return ResponseEntity.ok(new BespokeItemReadDTO(item.getExternalKey(), item.getPayload()));
	}

}
