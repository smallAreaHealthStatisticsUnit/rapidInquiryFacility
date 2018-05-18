package org.sahsu.rif.dataloader.fileformats;

import java.io.ByteArrayOutputStream;

import org.sahsu.rif.dataloader.concepts.DataSetFieldConfiguration;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.dataloader.system.RIFTemporaryTablePrefixes;
import org.sahsu.rif.generic.presentation.HTMLUtility;

/**
 *
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

public final class LoadFieldVarianceReport {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public LoadFieldVarianceReport() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	/*
	 * Field variance reports should only be used during the cleaning step.
	 * In this case, we want to examine the variance
	 */
	public String getHTML(
		final DataSetFieldConfiguration dataSetFieldConfiguration,
		final String[][] varianceData) {
		
		HTMLUtility htmlUtility = new HTMLUtility();
    	ByteArrayOutputStream outputStream
    		= new ByteArrayOutputStream();	
    	htmlUtility.initialise(outputStream, "UTF-8");
		
		
		htmlUtility.beginDocument();
		htmlUtility.beginBody();
		//write report title

		String coreDataSetName
			= dataSetFieldConfiguration.getCoreDataSetName();
				
		String loadTableName
			= RIFTemporaryTablePrefixes.EXTRACT.getTableName(coreDataSetName);
		String fieldOfInterest
			= dataSetFieldConfiguration.getLoadFieldName();
		String reportTitle
			= RIFDataLoaderToolMessages.getMessage(
				"fieldVarianceReport.title",
				loadTableName,
				fieldOfInterest);
		htmlUtility.writeHeader(1, reportTitle);

		
		htmlUtility.beginTable();

		//write header row
		htmlUtility.beginRow();
		String valueFieldName
			= RIFDataLoaderToolMessages.getMessage("fieldVarianceReport.value.label");
		htmlUtility.writeBoldColumnValue(valueFieldName);
		String frequencyFieldName
			= RIFDataLoaderToolMessages.getMessage("fieldVarianceReport.frequency.label");
		htmlUtility.writeBoldColumnValue(frequencyFieldName);
		htmlUtility.endRow();
		
		//write data
		for (int i = 0; i < varianceData.length; i++) {
			htmlUtility.beginRow();
			htmlUtility.writeColumnValue(varianceData[i][0]);
			htmlUtility.writeColumnValue(varianceData[i][1]);
			htmlUtility.endRow();
		}
				
		htmlUtility.endTable();
		
		
		htmlUtility.endBody();
		htmlUtility.endDocument();
		
		return htmlUtility.getHTML();
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


