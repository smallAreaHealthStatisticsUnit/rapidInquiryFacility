package rifDataLoaderTool.fileFormats;

import rifDataLoaderTool.businessConceptLayer.TableFieldCleaningConfiguration;
import rifDataLoaderTool.businessConceptLayer.RIFDataTypeInterface;
import rifDataLoaderTool.businessConceptLayer.CleaningRule;



import rifGenericLibrary.presentationLayer.HTMLUtility;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 *
 *
 * <hr>
 * Copyright 2014 Imperial College London, developed by the Small Area
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

public final class CleaningFieldConfigurationHandler {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	// ==========================================
	// Section Construction
	// ==========================================

	public CleaningFieldConfigurationHandler() {

	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public String getHTML(
		final TableFieldCleaningConfiguration tableFieldCleaningConfiguration) {
		
		HTMLUtility htmlUtility = new HTMLUtility();
    	ByteArrayOutputStream outputStream
			= new ByteArrayOutputStream();	
    	htmlUtility.initialise(outputStream, "UTF-8");
    	
		htmlUtility.beginBody();
	
		RIFDataTypeInterface rifDataType
			= tableFieldCleaningConfiguration.getRifDataType();
		ArrayList<CleaningRule> cleaningRules
			= rifDataType.getCleaningRules();
		htmlUtility.beginInvisibleTable();
		for (CleaningRule cleaningRule : cleaningRules) {
			htmlUtility.beginRow();
			htmlUtility.writeBoldColumnValue(cleaningRule.getName());
			htmlUtility.writeColumnValue(cleaningRule.getDescription());
			htmlUtility.endRow();
		}
		htmlUtility.endTable();
		
		htmlUtility.endBody();

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


