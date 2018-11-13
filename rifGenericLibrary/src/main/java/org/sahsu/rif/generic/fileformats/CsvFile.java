package org.sahsu.rif.generic.fileformats;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sahsu.rif.generic.system.RIFServiceException;
import org.sahsu.rif.generic.taxonomyservices.TaxonomyTerm;

import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.bean.HeaderColumnNameMappingStrategy;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import com.opencsv.bean.MappingStrategy;

/**
 * A generic class for parsing CSV files, using the OpenCSV library.
 */
public class CsvFile {

	private final Path file;

	public CsvFile(final Path file) throws RIFServiceException {

		if (file.toFile().exists()) {

			this.file = file;
		} else {

			throw new RIFServiceException("File %s not found", file.toString());
		}
	}

	/**
	 * This was an attempt to write a generic parser for any CSV file with headers. It failed
	 * because of Java's type erasure. It might be possible to make it work using Guava's
	 * TypeToken class, but I couldn't get that to work either.
	 * @param bean a class to which the CSV file's rows can be mapped
	 * @param <E> the type of the class which does the mapping
	 * @return the rows from the file, as a {@code List} of objects of E's type
	 * @throws RIFServiceException not really: this is just because {@code FileReader} declares
	 * an Exception
	 */
	public <E> List<E> parse(Class<? extends E> bean) throws RIFServiceException {

		Reader reader;
		try {
			reader = new FileReader(file.toFile());
		} catch (FileNotFoundException e) {

			// This can't actually happen if the current object was constructed, but we have to
			// handle the Exception
			throw new RIFServiceException(e, "It seems impossible, but %s was not found",
			                              file.toString());
		}

		MappingStrategy<E> strategy = new HeaderColumnNameMappingStrategy<>();
		// strategy.setType(bean.getClass());
		return new CsvToBeanBuilder<E>(reader)
				       .withMappingStrategy(strategy)
				       .withType(bean)
				       .build()
				       .parse();
	}

	/**
	 * Parses a file containing disease taxonomy terms. The headings must match those declared
	 * in the annotations in {@code TaxonomyTerm}, namely <code>DIAGNOSIS CODE"</code> and
	 * <code>"LONG DESCRIPTION"</code>.
	 */
	public List<TaxonomyTerm> parseTaxonomyTerms() throws RIFServiceException {

		try (Reader reader = new FileReader(file.toFile())) {

			Map<String, String> csvColumnsToFields = new HashMap<>();
			csvColumnsToFields.put("DIAGNOSIS CODE", "label");
			csvColumnsToFields.put("LONG DESCRIPTION", "description");

			HeaderColumnNameTranslateMappingStrategy<TaxonomyTerm> strategy =
					new HeaderColumnNameTranslateMappingStrategy<>();
			strategy.setType(TaxonomyTerm.class);
			strategy.setColumnMapping(csvColumnsToFields);

			return new CsvToBeanBuilder<TaxonomyTerm>(reader)
					       .withMappingStrategy(strategy)
					       .withType(TaxonomyTerm.class)
					       .build()
					       .parse();

		} catch (IOException e) {

			// This can't actually happen if the current object was constructed, but we have to
			// handle the Exception
			throw new RIFServiceException(e, "It seems impossible, but %s was not found",
			                              file.toString());
		}
	}
}
