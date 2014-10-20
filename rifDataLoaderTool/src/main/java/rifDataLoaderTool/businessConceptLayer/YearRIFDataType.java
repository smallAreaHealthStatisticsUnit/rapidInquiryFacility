package rifDataLoaderTool.businessConceptLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolMessages;

/**
 * A data type to represent a year value.  This data type contains two parameter values which
 * define a range of two digit years that may be considered part of the 20th century.  For example,
 * suppose 72 appears in a year field.  If the limits are [25,99], then 72 would be cleaned and
 * converted to 1972.  However, if the year were 13, then it would be converted to 2013.  The years
 * may be adjusted to suit the needs of a use case.
 * 
 * <p>
 * In future, this type will probably be modified to to rely on a database function.  If this happens,
 * then the 
 * This clas <code>getCleaningFunctionParameterValues</code> method
 * would be used to construct a database call.  Most database function calls resemble the format: 
 * <code>
 * [function_name] ([load table field name], areBlanksAllowed)
 * </code>
 * 
 * <p>
 * but in this case, we would use the <code>getCleaningFunctionParameterValues</code> method to make:
 * <code>
 * clean_year(year, true, 20, 99)
 * </code>
 * <p>
 * where the method provided the phrase "20, 99" part of the construction.
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

public class YearRIFDataType extends AbstractRIFDataType {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private String minimum20thCenturyYear;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private YearRIFDataType(
		final String identifier,
		final String name,
		final String description) {

		super(
			identifier,
			name, 
			description);
		
		addValidationExpression("^(19|20)\\d{2}$");
		setFieldValidationPolicy(RIFFieldValidationPolicy.VALIDATION_RULES);
		setFieldCleaningPolicy(RIFFieldCleaningPolicy.NO_CLEANING);
	}

	public static YearRIFDataType newInstance() {
		String name
			= RIFDataLoaderToolMessages.getMessage("");
		String description
			= RIFDataLoaderToolMessages.getMessage("");

		YearRIFDataType yearRIFDataType
			= new YearRIFDataType(
				"rif_year",
				name, 
				description);
		
		//cannot think of any implicit cleaning rules to add...
		
		return yearRIFDataType;
	}
	
	public YearRIFDataType createCopy() {
		YearRIFDataType cloneYearRIFDataType = newInstance();
		copyAttributes(cloneYearRIFDataType);
		return cloneYearRIFDataType;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
		
	public String getMinimum20thCenturyYear() {
		return minimum20thCenturyYear;
	}

	public void setMinimum20thCenturyYear(String minimum20thCenturyYear) {
		this.minimum20thCenturyYear = minimum20thCenturyYear;
	}	
	
	@Override
	public String getCleaningFunctionParameterValues() {
		StringBuilder buffer = new StringBuilder();
		buffer.append(",");
		buffer.append(minimum20thCenturyYear);
		return buffer.toString();
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


