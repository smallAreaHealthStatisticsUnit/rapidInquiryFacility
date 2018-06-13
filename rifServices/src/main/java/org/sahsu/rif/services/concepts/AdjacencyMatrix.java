package org.sahsu.rif.services.concepts;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Builder
@Getter
@ToString
public class AdjacencyMatrix {

	@NonNull
	private final String areaId;

	@NonNull
	private final int numAdjacencies;

	@NonNull
	private final String adjacencyList;
}
