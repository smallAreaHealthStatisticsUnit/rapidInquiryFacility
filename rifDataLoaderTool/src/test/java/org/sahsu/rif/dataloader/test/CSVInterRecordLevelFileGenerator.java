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

public class CSVInterRecordLevelFileGenerator 
	extends CSVIntraRecordLevelFileGenerator {

	// ==========================================
	// Section Constants
	// ==========================================
	private final double INCLUDE_DUPLICATE_RECORD_THRESHOLD = 0.50;
	private final double USE_NEW_DUPLICATE_RECORD_THRESHOLD = 0.50;
	// ==========================================
	// Section Properties
	// ==========================================
	private ArrayList<String> duplicateFieldCriteria;
	
	private DuplicateRecordPattern currentDuplicateRecordPattern;
	// ==========================================
	// Section Construction
	// ==========================================

	public CSVInterRecordLevelFileGenerator(
		final boolean trackCoverage, 
		final int maximumRecordsToCreate) {
		
		super(
			trackCoverage, 
			maximumRecordsToCreate);
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public void setDuplicateFieldCriteria(final String... duplicateFieldCriteria) {
		
		for (String duplicateFieldCriterion : duplicateFieldCriteria) {
			this.duplicateFieldCriteria.add(duplicateFieldCriterion);
		}
	}
	
	private void considerDuplicateRecordError() {
		Random decisionMaker = new Random();
		double includeDuplicateDecisionValue = decisionMaker.nextDouble();
		if (includeDuplicateDecisionValue < INCLUDE_DUPLICATE_RECORD_THRESHOLD) {
			//we will introduce duplicate field criteria to the next CSV row

			if (currentDuplicateRecordPattern != null) {
				//Do we re-use the existing duplicate pattern or create a new one?
				double useNewDuplicateRecordPattern
					=  decisionMaker.nextDouble();
				if (useNewDuplicateRecordPattern > USE_NEW_DUPLICATE_RECORD_THRESHOLD) {
					//Yes, replace the new record pattern with another one
					currentDuplicateRecordPattern = createDuplicateRecordPattern();
				}
			}
			else {
				currentDuplicateRecordPattern = createDuplicateRecordPattern();				
			}
			currentDuplicateRecordPattern = null;
		}
		else {
			currentDuplicateRecordPattern = null;
		}
	}
	
	private DuplicateRecordPattern createDuplicateRecordPattern() {
		DuplicateRecordPattern currentDuplicateRecordPattern
			= DuplicateRecordPattern.newInstance();
		
		for (String duplicateFieldCriterion : duplicateFieldCriteria) {
			String fieldValue = generateValue(duplicateFieldCriterion);
			currentDuplicateRecordPattern.addField(
				duplicateFieldCriterion, 
				fieldValue);
		}
		
		return currentDuplicateRecordPattern;
	}
	
	@Override
	protected String generateCSVDataRow() {		
		StringBuilder csvRow = new StringBuilder();
		ArrayList<DummyField> dummyFields = getDummyFields();
		considerDuplicateRecordError();
		
		if (currentDuplicateRecordPattern == null) {
			//We won't insert duplicate record field values this time
			return super.generateCSVDataRow();
		}
		else {			
			for (DummyField dummyField : dummyFields) {
				String patternFieldValue
					= currentDuplicateRecordPattern.getFieldValue(
						dummyField.getFieldName());
				if (patternFieldValue != null) {
					csvRow.append(patternFieldValue);					
				}
				else {
					csvRow.append(dummyField.generateValue());					
				}
			}
			return csvRow.toString();			
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


