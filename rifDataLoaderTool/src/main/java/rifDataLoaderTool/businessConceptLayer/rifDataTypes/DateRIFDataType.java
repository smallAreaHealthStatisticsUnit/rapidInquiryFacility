package rifDataLoaderTool.businessConceptLayer.rifDataTypes;

import java.util.ArrayList;

import rifDataLoaderTool.businessConceptLayer.RIFDataTypeInterface;
import rifDataLoaderTool.businessConceptLayer.RIFFieldCleaningPolicy;
import rifDataLoaderTool.businessConceptLayer.RIFFieldValidationPolicy;
import rifDataLoaderTool.system.RIFDataLoaderMessages;
/**
 * a data type for Date.  Note that unlike other data type classes, the validation expressions
 * are not written for the POSIX regular expression languages. Here the validation patterns 
 * show date patterns such as "dd-mmm-yyyy" etc.
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

public final class DateRIFDataType 
	extends AbstractRIFDataType {

	// ==========================================
	// Section Constants
	// ==========================================
	
	// ==========================================
	// Section Properties
	// ==========================================
	
	// ==========================================
	// Section Construction
	// ==========================================

	private DateRIFDataType(
		final String name,
		final String description,
		final String validationRegularExpression) {

		super(
			name, 
			description, 
			validationRegularExpression);
		
		setFieldValidationPolicy(RIFFieldValidationPolicy.VALIDATION_RULES);
		setFieldCleaningPolicy(RIFFieldCleaningPolicy.NO_CLEANING);
		
	}

	public static DateRIFDataType newInstance() {

		String name
			= RIFDataLoaderMessages.getMessage("rifDataType.date.label");

		//this is the order of date formats which are used to validate RIF dates
		String dateFormat1 = "DD Mon YYYY";
		ArrayList<String> validationExpressions = new ArrayList<String>();
		validationExpressions.add("DD Mon YYYY");
		
		StringBuilder dateFormatPatternList = new StringBuilder();
		for (int i = 0; i < validationExpressions.size(); i++) {
			if (i != 0) {
				dateFormatPatternList.append(",");
			}
			dateFormatPatternList.append(validationExpressions.get(i));
		}
		
		String description
			= RIFDataLoaderMessages.getMessage(
				"rifDataType.date.description",
				dateFormatPatternList.toString());
		DateRIFDataType dateRIFDataType
			= new DateRIFDataType(
				"rif_date",
				name, 
				description);

		dateRIFDataType.setValidationExpressions(validationExpressions);
		
		return dateRIFDataType;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================
	
	public RIFDataTypeInterface createCopy() {
		DateRIFDataType cloneDateRIFDataType 
			= newInstance();
		copyAttributes(cloneDateRIFDataType);
		return cloneDateRIFDataType;
	}
		
}


