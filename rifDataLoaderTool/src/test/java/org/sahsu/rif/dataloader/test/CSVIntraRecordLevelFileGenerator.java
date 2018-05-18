package org.sahsu.rif.dataloader.test;

import java.util.ArrayList;
import java.util.Random;
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

public class CSVIntraRecordLevelFileGenerator 
	extends CSVFieldLevelFileGenerator {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================

	private ArrayList<FieldCombinationErrorPattern> fieldCombinationErrorPatterns;	
	private FieldCombinationErrorPattern currentFieldCombinationErrorPattern;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public CSVIntraRecordLevelFileGenerator(
		final boolean trackCoverage,
		final int maximumRecordsToCreate) {
		
		super(trackCoverage, maximumRecordsToCreate);
		
		fieldCombinationErrorPatterns = new ArrayList<FieldCombinationErrorPattern>();		
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	/**
	 * An example:
	 * sex
	 * "M"
	 * "C56.9" (ovarian cancer)
	 * 
	 * @param fieldNameA
	 * @param fieldValueA
	 * @param fieldNameB
	 * @param fieldValueB
	 */
	public void addFieldCombinationErrorPattern(
		final String fieldNameA, 
		final String fieldValueA,
		final String fieldNameB,
		final String fieldValueB) {
		
		FieldCombinationErrorPattern fieldCombinationErrorPattern
			= FieldCombinationErrorPattern.newInstance(
				fieldNameA, 
				fieldValueA, 
				fieldNameB, 
				fieldValueB);
		fieldCombinationErrorPatterns.add(fieldCombinationErrorPattern);
	}
	
	@Override
	protected String generateCSVDataRow() {		
		StringBuilder csvRow = new StringBuilder();
		ArrayList<DummyField> dummyFields = getDummyFields();
		considerIntraRecordError();
		if (currentFieldCombinationErrorPattern != null) {
			for (DummyField dummyField : dummyFields) {
				String patternFieldValue
					= currentFieldCombinationErrorPattern.getFieldValue(
						dummyField.getFieldName());
				if (patternFieldValue != null) {
					csvRow.append(patternFieldValue);					
				}
				else {
					csvRow.append(dummyField.generateValue());					
				}
				csvRow.append(dummyField.generateValue());
			}
		}
		else {
			//There is no field combination error so just generate a row as
			//if field values were being generated independently of one another
			return super.generateCSVDataRow();
		}
		
		return csvRow.toString();
	}
	
	private void considerIntraRecordError() {
		Random decisionMaker = new Random();
		double decisionValue = decisionMaker.nextDouble();
		
		if (decisionValue < 0.5) {
			Random randomIndexGenerator = new Random();
			int randomIndex 
				= randomIndexGenerator.nextInt(fieldCombinationErrorPatterns.size());
			currentFieldCombinationErrorPattern
				= fieldCombinationErrorPatterns.get(randomIndex);
		}
		else {
			currentFieldCombinationErrorPattern = null;
		}		
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


