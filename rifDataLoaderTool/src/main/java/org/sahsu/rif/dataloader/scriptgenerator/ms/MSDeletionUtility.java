package org.sahsu.rif.dataloader.scriptgenerator.ms;

import java.util.ArrayList;

import org.sahsu.rif.dataloader.concepts.DataLoaderToolConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfiguration;
import org.sahsu.rif.dataloader.concepts.DataSetConfigurationUtility;
import org.sahsu.rif.dataloader.concepts.DataSetFieldConfiguration;
import org.sahsu.rif.dataloader.concepts.HealthTheme;
import org.sahsu.rif.generic.datastorage.DeleteRowsQueryFormatter;
import org.sahsu.rif.generic.datastorage.ms.MSSQLDeleteTableQueryFormatter;

/**
 * This class attempts to encapsulate the details associated with removing tables and
 * rows that may already exist when a data loading script is being done.  I've tried
 * to centralise the code for it here, mainly because in future we will need more
 * elaborate implementations of the versions that check first whether a table or
 * rows *should* be deleted.  For example, if a denominator table already exists and is
 * referenced by studies, one approach would be to flag an error.  Another would be
 * to delete the table and create a new version of it.  Both approaches could be used
 * for different activities.  For example, in a surveillance activity, we may want to
 * replace the table with the newest version and then re-run all the studies that
 * referenced it.  For an auditing activity, we may not want to have a table deleted
 * once it has been referred to in a study.
 *
 * <hr>
 * Copyright 2017 Imperial College London, developed by the Small Area
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

public class MSDeletionUtility {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	public enum DeletionPolicyOption {PREVENT_OVERWRITE, ALLOW_OVERWRITE};
	private DeletionPolicyOption deletionPolicy;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public MSDeletionUtility() {
		deletionPolicy = DeletionPolicyOption.ALLOW_OVERWRITE;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void setDeletionPolicy(final DeletionPolicyOption deletionPolicyOption) {
		this.deletionPolicy = deletionPolicyOption;
	}
	
	public String deleteExistingTableEntries(
		final DataLoaderToolConfiguration dataLoaderToolConfiguration) {
		
		StringBuilder deletionScriptText = new StringBuilder();
		
		ArrayList<DataSetConfiguration> denominators
			= dataLoaderToolConfiguration.getDenominatorDataSetConfigurations();
		for (DataSetConfiguration denominator : denominators) {
			deleteDenominatorDataSet(
				deletionScriptText, 
				denominator);
		}
				
		ArrayList<DataSetConfiguration> numerators
			= dataLoaderToolConfiguration.getNumeratorDataSetConfigurations();
		for (DataSetConfiguration numerator : numerators) {
			deleteNumeratorDataSet(
				deletionScriptText, 
				numerator);
		}
				
		ArrayList<DataSetConfiguration> covariates
			= dataLoaderToolConfiguration.getCovariateDataSetConfigurations();
		for (DataSetConfiguration covariate : covariates) {
			deleteCovariateDataSet(
				deletionScriptText, 
				covariate);
		}

		//Attempt to delete health themes
		//ArrayList<HealthTheme> healthThemes
		//	= dataLoaderToolConfiguration.getHealthThemes();
		//for (HealthTheme healthTheme : healthThemes) {
		//	deleteHealthTheme(
		//		deletionScriptText, 
		//		healthTheme);
		//}
		
		return deletionScriptText.toString();
	}

	
	private void verifyDataSetsNotInUse(
		final StringBuilder queryScriptText,
		final DataLoaderToolConfiguration dataLoaderToolConfiguration) {

		/*
		 * For each numerator, find its denominator and geography and test
		 * whether that combination is associated with any studies that might
		 * already be using it.  Then just as we did in PGDeletionUtility,
		 * use the SQLGeneralQueryFormatter class to build up the following
		 * query.  So for each numerator produce this template:
		 * 
		 * DECLARE c1 CURSOR FOR 
         * SELECT COUNT(DISTINCT(a.study_id)) AS total
         * FROM t_rif40_studies a, t_rif40_investigations b
         * WHERE (b.numer_tab = 'NUM_SAHSULAND_CANCER' 
		 * OR  a.denom_tab = 'POP_SAHSULAND_POP')
         * AND a.geography  = 'SAHSULAND'
         * AND A.study_id   = b.study_id;
		 * DECLARE @c1_total AS int;
		 * OPEN c1;
		 * FETCH NEXT FROM c1 INTO @c1_total;
		 * IF @c1_total = 0
 		 * PRINT 'Geography: SAHSULAND is not used by any studies';
		 * ELSE
 		 * RAISERROR('Geography: SAHSULAND is used by: % studies', 16, 1, @c1_total);
		 * CLOSE c1;
		 * DEALLOCATE c1;
		 * GO     
		 */
		
	}
	
	private void deleteHealthTheme(
		final StringBuilder queryScriptText,
		final HealthTheme healthTheme) {
		
		String themeToDelete = healthTheme.getName();
		
		DeleteRowsQueryFormatter queryFormatter
			= new DeleteRowsQueryFormatter();
		queryFormatter.setDatabaseSchemaName("rif40");
		queryFormatter.setFromTable("rif40_health_study_themes");
		queryFormatter.addWhereParameterWithLiteralValue(
			"theme", 
			themeToDelete);
		
		queryScriptText.append(queryFormatter.generateQuery());
		queryScriptText.append("\n");
	}
		
	private void deleteDenominatorDataSet(
		final StringBuilder queryScriptText,
		final DataSetConfiguration denominator) {
			
		deleteRIF40TableEntry(
			queryScriptText, 
			denominator);
		
		deleteDataSetTable(
			queryScriptText,
			"rif_data", 
			denominator);		
	}
	
	private void deleteNumeratorDataSet(
		final StringBuilder queryScriptText,
		final DataSetConfiguration numerator) {
		
		deleteRIF40TableOutcomesEntry(
			queryScriptText,
			numerator);

		deleteRIF40TableEntry(
			queryScriptText,
			numerator);
		
		deleteDataSetTable(
			queryScriptText,
			"rif_data", 
			numerator);		
	}
		
	private void deleteCovariateDataSet(
		final StringBuilder queryScriptText,
		final DataSetConfiguration covariate) {
		
		deleteRIF40CovariatesTableEntry(
			queryScriptText,
			covariate);
		
		deleteDataSetTable(
			queryScriptText,
			"rif_data", 
			covariate);
	}
	
	private void deleteDataSetTable(
		final StringBuilder queryScriptText,
		final String databaseSchemaName, 
		final DataSetConfiguration dataSetConfiguration) {
		
		String tableToDelete 
			= dataSetConfiguration.getPublishedTableName().toUpperCase();
		MSSQLDeleteTableQueryFormatter queryFormatter
			= new MSSQLDeleteTableQueryFormatter();
		queryFormatter.setDatabaseSchemaName(databaseSchemaName);
		queryFormatter.setTableToDelete(tableToDelete);
		
		queryScriptText.append(queryFormatter.generateQuery());
		queryScriptText.append("\n");
	}

	
	private void deleteRIF40CovariatesTableEntry(
		final StringBuilder queryScriptText,
		final DataSetConfiguration covariate) {

		DataSetFieldConfiguration geoLevelResolutionField
			= DataSetConfigurationUtility.getRequiredGeographicalResolutionField(covariate);
		String geoLevelName
			= geoLevelResolutionField.getConvertFieldName().toUpperCase();
		ArrayList<DataSetFieldConfiguration> covariateFields
			= DataSetConfigurationUtility.getCovariateFields(covariate);
			
		for (DataSetFieldConfiguration covariateField : covariateFields) {
			String covariateName = covariateField.getCleanFieldName().toUpperCase();
			
			DeleteRowsQueryFormatter queryFormatter
				= new DeleteRowsQueryFormatter();
			queryFormatter.setDatabaseSchemaName("rif40");
			queryFormatter.setFromTable("rif40_covariates");
			queryFormatter.addWhereParameterWithLiteralValue(
				"geolevel_name", 
				geoLevelName);
			queryFormatter.addWhereParameterWithLiteralValue(
				"covariate_name", 
				covariateName);
		
			queryScriptText.append(queryFormatter.generateQuery());
			queryScriptText.append("\n");		
		
		}

	}

	
	private void deleteRIF40TableEntry(
		final StringBuilder queryScriptText,
		final DataSetConfiguration dataSetConfiguration) {

		String tableToDelete
			= dataSetConfiguration.getPublishedTableName().toUpperCase();
		DeleteRowsQueryFormatter queryFormatter
			= new DeleteRowsQueryFormatter();
		queryFormatter.setDatabaseSchemaName("rif40");
		queryFormatter.setFromTable("rif40_tables");
		queryFormatter.addWhereParameterWithLiteralValue(
			"table_name", 
			tableToDelete);
		
		queryScriptText.append(queryFormatter.generateQuery());
		queryScriptText.append("\n");		
	}
	
	private void deleteRIF40TableOutcomesEntry(
		final StringBuilder queryScriptText,
		final DataSetConfiguration numerator) {
				
		String tableToDelete
			= numerator.getPublishedTableName().toUpperCase();
		DeleteRowsQueryFormatter queryFormatter
			= new DeleteRowsQueryFormatter();
		queryFormatter.setDatabaseSchemaName("rif40");
		queryFormatter.setFromTable("rif40_table_outcomes");
		queryFormatter.addWhereParameterWithLiteralValue(
			"numer_tab", 
			tableToDelete);
	
		queryScriptText.append(queryFormatter.generateQuery());
		queryScriptText.append("\n");		
		
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


