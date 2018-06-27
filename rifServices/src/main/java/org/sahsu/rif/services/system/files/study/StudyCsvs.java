package org.sahsu.rif.services.system.files.study;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.services.concepts.AdjacencyMatrixRow;

import com.opencsv.CSVWriter;
import com.opencsv.ResultSetColumnNameHelperService;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;

import lombok.Builder;

@Builder
public class StudyCsvs {

	private static final String AREA_ID_COL = "area_id";
	private static final String NUM_ADJACENCIES_COL = "num_adjacencies";
	private static final String ADJACENCY_LIST_COL = "adjacency_list";
	private static final String AREA_ID_BEAN = "areaId";
	private static final String NUM_ADJACENCIES_BEAN = "numAdjacencies";
	private static final String ADJACENCY_LIST_BEAN = "adjacencyList";
	private static final String[] columns = new String[] {
			AREA_ID_COL, NUM_ADJACENCIES_COL, ADJACENCY_LIST_COL };

	private final int studyId;
	private final String extractDirectory;

	public void adjacencyMatrixToCsv(final List<AdjacencyMatrixRow> matrix) throws IOException, RIFServiceException {

		// Get the directory for the adjacency matrix
		Path adjacencyMatrixFile = dataDir().resolve("tmp_s" + studyId + "_adjacency_matrix.csv");

		System.out.printf("Should create a file called %s%n", adjacencyMatrixFile);

		try(Writer out = new FileWriter(adjacencyMatrixFile.toFile())) {

			OrderedNamedHeadersMappingStrategy<AdjacencyMatrixRow> mappingStrategy =
					new OrderedNamedHeadersMappingStrategy<>();
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

	public void extractTableToCsv(ResultSet extractResults) throws IOException, SQLException {

		Path extractFile = dataDir().resolve("tmp_s" + studyId + "_extract.csv");

		try(CSVWriter out = new CSVWriter(new FileWriter(extractFile.toFile()))) {

			out.writeAll(extractResults, true);
		}

		// Path extractFile = dataDir().resolve("tmp_s" + studyId + "_extract.csv");
		//
		// try(CSVWriter out = new CSVWriter(new FileWriter(extractFile.toFile()))) {
		//
		// 	// The old version of the CSV was generated from an R data frame, which includes
		// 	// the row number with an empty header. So we emulate that here.
		// 	ResultSetColumnNameHelperService columnService = new ResultSetColumnNameHelperService();
		// 	String[] columnNames = columnService.getColumnNames(extractResults);
		// 	String[] fullColumnNames = new String[columnNames.length + 1];
		// 	fullColumnNames[0] = "";
		// 	System.arraycopy(columnNames, 0, fullColumnNames, 1, columnNames.length);
		//
		// 	// Write the column names
		// 	out.writeNext(fullColumnNames);
		//
		// 	// Now the ResultSet contents
		// 	String[] columns = new String[fullColumnNames.length];
		// 	int rowCount = 1;
		// 	while (extractResults.next()) {
		//
		// 		columns[0] = Integer.toString(rowCount);
		// 		for (int i  = 1; i <= columnNames.length; i++) {
		//
		// 			columns[i] = extractResults.getString(i);
		// 		}
		// 		out.writeNext(columns);
		// 		rowCount++;
		// 	}
		// }
	}

	private Path dataDir() throws IOException {

		return ScratchDirectories.builder()
				       .studyId(studyId)
				       .directory(extractDirectory)
				       .build()
				       .dataDir();
	}

	/**
	 * OpenCsv doesn't quite give us what we need as far as creating a CSV file with the
	 * columns ordered correctly and with headers goes. This mapping strategy fixes that.
	 * @param <T>
	 */
	private final class OrderedNamedHeadersMappingStrategy<T> extends ColumnPositionMappingStrategy<T> {

		@Override
		public String[] generateHeader(T bean) throws CsvRequiredFieldEmptyException {

			// Make sure things are initialised. The parent class returns an empty array, so we
			// just ignore it.
			super.generateHeader(bean);

			return columns;
		}
	}
}
