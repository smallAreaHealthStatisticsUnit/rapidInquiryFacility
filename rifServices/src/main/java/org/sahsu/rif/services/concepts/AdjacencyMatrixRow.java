package org.sahsu.rif.services.concepts;

import com.opencsv.bean.CsvBindAndJoinByPosition;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Builder
@Getter
@ToString
public class AdjacencyMatrixRow {

	@NonNull
	@CsvBindByPosition(position = 0)
	private final String areaId;

	@CsvBindByPosition(position = 1)
	private final int numAdjacencies;

	@NonNull
	@CsvBindByPosition(position = 2)
	private final String adjacencyList;
}
