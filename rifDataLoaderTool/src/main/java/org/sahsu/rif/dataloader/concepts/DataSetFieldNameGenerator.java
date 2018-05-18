package org.sahsu.rif.dataloader.concepts;

import java.text.Collator;

import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.system.Messages;

/**
 * Generates names for different fields in 
 * {@link rifDataLoaderTool.businessConceptLayer.DataSetConfiguration} and 
 *{@link rifDataLoaderTool.businessConceptLayer.DataSetFieldConfiguration}.
 * In most cases, the names will be derived based on the names and field names
 * of CSV files.  However, the Data Loader Tool editor allows users to add
 * additonal fields to a DataSetConfiguration.  When the new field is created,
 * it will be given defaults based on values that are generated from the routines
 * in this class.
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

public class DataSetFieldNameGenerator {

	// ==========================================
	// Section Constants
	// ==========================================
	
	private static Messages GENERIC_MESSAGES = Messages.genericMessages();
	
	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public DataSetFieldNameGenerator() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public static String generateDataSetConfigurationName(final AbstractWorkflow workflow) {
		String baseDataSetName
			= RIFDataLoaderToolMessages.getMessage("dataSetConfiguration.baseDataSetName");
		String [] existingDataSetNames
			= workflow.getDataSetConfigurationNames();
		return generateName(
			baseDataSetName, 
			existingDataSetNames);
	}
	
	public static String generateCoreFieldName(final DataSetConfiguration dataSetConfiguration) {
		String baseFieldName
			= RIFDataLoaderToolMessages.getMessage("dataSetFieldConfiguration.baseFieldName");
		String[] existingCoreFieldNames
			= dataSetConfiguration.getCoreFieldNames();
		return generateName(
			baseFieldName, 
			existingCoreFieldNames);
	}
	
	public static String generateLoadFieldName(final DataSetConfiguration dataSetConfiguration) {
		String baseFieldName
			= RIFDataLoaderToolMessages.getMessage("dataSetFieldConfiguration.baseFieldName");
		String[] existingLoadFieldNames
			= dataSetConfiguration.getLoadFieldNames();
		return generateName(
			baseFieldName, 
			existingLoadFieldNames);
	}
	
	public static String generateCleanFieldName(final DataSetConfiguration dataSetConfiguration) {
		String baseFieldName
			= RIFDataLoaderToolMessages.getMessage("dataSetFieldConfiguration.baseFieldName");
		String[] existingCleanFieldNames
			= dataSetConfiguration.getCleanFieldNames();
		return generateName(
			baseFieldName,
			existingCleanFieldNames);
	}

	private static String generateName(
		final String baseFieldName,
		final String[] existingFieldNames) {

		Collator collator = GENERIC_MESSAGES.getCollator();
		int i = 1;
		String candidateFieldName = baseFieldName + String.valueOf(i);
		if (existingFieldNames.length == 0) {
			return candidateFieldName;
		}

		//assume there is a name collision
		boolean nameCollisionExists = true;
		while (nameCollisionExists == true) {
			candidateFieldName = baseFieldName + String.valueOf(i);
			int j;
			for (j = 0; j < existingFieldNames.length; j++) {
				if (collator.equals(candidateFieldName, existingFieldNames[j]) == true) {
					nameCollisionExists = true;
					break;
				}
			}
			if (j == existingFieldNames.length) {
				nameCollisionExists = false;
			}
			else {
				i++;				
			}
		}
		
		return candidateFieldName;
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


