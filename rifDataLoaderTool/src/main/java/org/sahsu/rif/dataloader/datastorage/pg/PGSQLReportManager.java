package org.sahsu.rif.dataloader.datastorage.pg;

import java.io.File;
import java.io.Writer;
import java.sql.Connection;
import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.DataLoadingResultTheme;
import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfigurationUtility;
import org.sahsu.rif.dataloader.concepts.DataSetFieldConfiguration;
import org.sahsu.rif.dataloader.concepts.FieldChangeAuditLevel;
import org.sahsu.rif.dataloader.concepts.RIFSchemaArea;
import org.sahsu.rif.dataloader.fileformats.PostgreSQLDataLoadingScriptWriter;
import org.sahsu.rif.dataloader.system.RIFTemporaryTablePrefixes;
import org.sahsu.rif.generic.system.RIFServiceException;

/**
 *
 *
 * <hr>
 * Copyright 2015 Imperial College London, developed by the Small Area
 * Health Statistics Unit. 
 *
 * <pre> 
 * This file is part of the Rapid Inquiry Facility (RIF) project.
 * RIF is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * RIF is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with RIF.  If not, see <http://www.gnu.org/licenses/>.
 * </pre>
 *
 * <hr>
 * Kevin Garwood
 * @author kgarwood
 */

/*
 * Code Road Map:
 * --------------
 * Code is organised into the following sections.  Wherever possible, 
 * methods are classified based on an order of precedence described in 
 * parentheses (..).  For example, if you're trying to find a method 
 * 'getName(...)' that is both an interface method and an accessor 
 * method, the order tells you it should appear under interface.
 * 
 * Order of 
 * Precedence     Section
 * ==========     ======
 * (1)            Section Constants
 * (2)            Section Properties
 * (3)            Section Construction
 * (7)            Section Accessors and Mutators
 * (6)            Section Errors and Validation
 * (5)            Section Interfaces
 * (4)            Section Override
 *
 */

class PGSQLReportManager 
	extends AbstractPGSQLDataLoaderStepManager {

	// ==========================================
	// Section Constants
	// ==========================================
	private static final String RESULTS_ZIP_FILE_SUFFIX = "_results.zip";
	
	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public PGSQLReportManager() {

	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void writeResults(
		final Connection connection,
		final Writer logFileWriter,
		final File exportDirectory,
		final DataSetConfiguration dataSetConfiguration)
		throws RIFServiceException {
				
		String coreDataSetName
			= dataSetConfiguration.getName();
		
		RIFSchemaArea rifSchemaArea
			= dataSetConfiguration.getRIFSchemaArea();
		String publishTableName
			= rifSchemaArea.getPublishedTableName(coreDataSetName);
		
		String exportDirectoryPath = exportDirectory.getAbsolutePath();
		
		exportTable(
				connection,
				logFileWriter,
				exportDirectoryPath,
				DataLoadingResultTheme.ARCHIVE_RESULTS,
				publishTableName);


		if (emptyDataQualityTableExists(dataSetConfiguration)) {
			String emptyFieldsDataQualityTableName
				= RIFTemporaryTablePrefixes.EMPTY_CHECK.getTableName(coreDataSetName);
			exportTable(
				connection,
				logFileWriter,
				exportDirectoryPath,
				DataLoadingResultTheme.ARCHIVE_AUDIT_TRAIL,
				emptyFieldsDataQualityTableName);
		}

		if (emptyPerYearDataQualityTableExists(dataSetConfiguration)) {
			String emptyFieldsPerYearDataQualityTableName
				= RIFTemporaryTablePrefixes.EMPTY_PER_YEAR_CHECK.getTableName(coreDataSetName);
			exportTable(
				connection,
				logFileWriter,
				exportDirectoryPath,
				DataLoadingResultTheme.ARCHIVE_AUDIT_TRAIL,
				emptyFieldsPerYearDataQualityTableName);			
		}


		if (changeAuditTableExists(dataSetConfiguration)) {
			String changeTableName
				= RIFTemporaryTablePrefixes.AUD_CHANGES.getTableName(coreDataSetName);
			exportTable(
				connection,
				logFileWriter,
				exportDirectoryPath,
				DataLoadingResultTheme.ARCHIVE_AUDIT_TRAIL,
				changeTableName);			
		}
		
		if (validationAuditTableExists(dataSetConfiguration)) {
			String failedValidationTableName
				= RIFTemporaryTablePrefixes.AUD_FAILED_VALIDATION.getTableName(coreDataSetName);
			exportTable(
				connection,
				logFileWriter,
				exportDirectoryPath,
				DataLoadingResultTheme.ARCHIVE_AUDIT_TRAIL,
				failedValidationTableName);			
		}

		//Generate the script that is supposed to be specific to SQL Server or PostgreSQL
		PostgreSQLDataLoadingScriptWriter scriptWriter
			= new PostgreSQLDataLoadingScriptWriter();
		File dataLoadingScriptFile
			= createDataLoadingScriptFileName(
				exportDirectoryPath, 
				coreDataSetName);
		scriptWriter.writeFile(
			dataLoadingScriptFile, 
			dataSetConfiguration);	
	}
	
	private File createDataLoadingScriptFileName(
		final String exportDirectoryPath,
		final String coreTableName) {
		
		StringBuilder buffer = new StringBuilder();
		buffer.append(exportDirectoryPath);
		buffer.append(File.separator);
		buffer.append(coreTableName);
		buffer.append(".sql");
		
		return new File(buffer.toString());
	}
	
	private boolean emptyDataQualityTableExists(
		final DataSetConfiguration dataSetConfiguration) {
		
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= dataSetConfiguration.getFieldsWithEmptyFieldCheck();
		if (fieldConfigurations.size() > 0) {
			return true;
		}
		
		return false;
	}
	
	private boolean emptyPerYearDataQualityTableExists(
		final DataSetConfiguration dataSetConfiguration) {
		
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= dataSetConfiguration.getFieldsWithEmptyPerYearFieldCheck();
		if (fieldConfigurations.size() > 0) {
			return true;
		}
		
		return false;
	}
	
	
	private boolean changeAuditTableExists(
		final DataSetConfiguration dataSetConfiguration) {
		
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= DataSetConfigurationUtility.getChangeAuditFields(
				dataSetConfiguration,
				FieldChangeAuditLevel.INCLUDE_FIELD_NAME_ONLY);
		if (fieldConfigurations.size() > 0) {
			return true;
		}

		fieldConfigurations
			= DataSetConfigurationUtility.getChangeAuditFields(
				dataSetConfiguration, 
				FieldChangeAuditLevel.INCLUDE_FIELD_CHANGE_DESCRIPTION);
		if (fieldConfigurations.size() > 0) {
			return true;
		}
		
		return false;
	}
	
	private boolean validationAuditTableExists(
		final DataSetConfiguration dataSetConfiguration) {
		
		ArrayList<DataSetFieldConfiguration> fieldConfigurations
			= dataSetConfiguration.getFieldsWithValidationChecks();
		if (fieldConfigurations.size() > 0) {
			return true;
		}

		return false;
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

}


