package org.sahsu.rif.services.system.files.study;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.AdjacencyMatrixRow;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import lombok.Builder;

@Builder
public class AdjacencyMatrixCsv {

	private static final String AREA_ID_COL = "area_id";
	private static final String NUM_ADJACENCIES_COL = "num_adjacencies";
	private static final String ADJACENCY_LIST_COL = "adjacency_list";
	private static final String AREA_ID_BEAN = "areaId";
	private static final String NUM_ADJACENCIES_BEAN = "numAdjacencies";
	private static final String ADJACENCY_LIST_BEAN = "adjacencyList";
	private static final String[] columns = new String[] {
			AREA_ID_COL, NUM_ADJACENCIES_COL, ADJACENCY_LIST_COL };

	private final int studyId;
	private final List<AdjacencyMatrixRow> matrix;
	private final String extractDirectory;

	public void toCsv() throws IOException, RIFServiceException {

		// Get the directory for the adjacency matrix
		Path adjacencyMatrixFile = ScratchDirectories.builder()
				                           .studyId(studyId)
				                           .directory(extractDirectory)
				                           .build()
				                           .dataDir()
				                           .resolve("tmp_s" + studyId + "_adjacency_matrix.csv");

		System.out.printf("Should create a file called %s%n", adjacencyMatrixFile);

		try(Writer out = new FileWriter(adjacencyMatrixFile.toFile())) {

			ProperMappingStrategy<AdjacencyMatrixRow> mappingStrategy =
					new ProperMappingStrategy<>();
			mappingStrategy.setType(AdjacencyMatrixRow.class);
			mappingStrategy.setColumnMapping(columns);

			StatefulBeanToCsv<AdjacencyMatrixRow> beanToCsv =
					new StatefulBeanToCsvBuilder<AdjacencyMatrixRow>(out)
							.withMappingStrategy(mappingStrategy).build();

			beanToCsv.write(matrix);

		} catch (CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {

			throw new RIFServiceException(e, "Problem generating CSV file %s", adjacencyMatrixFile);
		}
	}

	/**
	 * OpenCsv doesn't quite give us what we need as far as creating a CSV file with the
	 * columns ordered correctly and with headers goes. This mapping strategy fixes that.
	 * @param <T>
	 */
	private final class ProperMappingStrategy<T> extends ColumnPositionMappingStrategy<T> {

		@Override
		public String[] generateHeader(T bean) throws CsvRequiredFieldEmptyException {

			// Make sure things are initialised. The parent class returns an empty array, so we
			// just ignore it.
			super.generateHeader(bean);

			return columns;
		}
	}
}
