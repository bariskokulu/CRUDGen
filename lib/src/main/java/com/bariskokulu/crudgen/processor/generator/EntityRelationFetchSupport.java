package com.bariskokulu.crudgen.processor.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.bariskokulu.crudgen.processor.component.DTOElement;
import com.bariskokulu.crudgen.processor.component.DTOFieldElement;
import com.bariskokulu.crudgen.processor.component.EntityElement;
import com.bariskokulu.crudgen.util.RepoType;

public final class EntityRelationFetchSupport {

	private EntityRelationFetchSupport() {
	}

	public static boolean useJpaReadGraph(EntityElement element) {
		return element.getRepoType() == RepoType.JPA
				&& element.getCustomRepoTypeName() == null
				&& element.getExtendRepoTypeName() == null
				&& !readGraphAttributePaths(element).isEmpty();
	}

	public static List<String> readGraphAttributePaths(EntityElement element) {
		Set<String> paths = new LinkedHashSet<>();
		DTOElement readDto = element.getDtos().get("Read");
		if (readDto == null) {
			return Collections.emptyList();
		}
		for (DTOFieldElement field : readDto.getFields()) {
			if (field.isRelation() && !field.isNestedProjection()) {
				paths.add(field.getEntityFieldName());
			}
		}
		return new ArrayList<>(paths);
	}

}
