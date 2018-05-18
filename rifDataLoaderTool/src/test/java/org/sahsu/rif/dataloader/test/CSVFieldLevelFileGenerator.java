package org.sahsu.rif.dataloader.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.*;

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

public class CSVFieldLevelFileGenerator {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private ArrayList<DummyField> dummyFields;
	private HashMap<String, DummyField> dummyFieldFromName;
	private boolean trackCoverage;
	
	private int ithRecordCreated;
	private int maximumRecordsToCreate;
	
	private TestDataCoverageCondition stoppingCondition;
	// ==========================================
	// Section Construction
	// ==========================================

	public CSVFieldLevelFileGenerator(
		final boolean trackCoverage,
		final int maximumRecordsToCreate) {

		initialise(trackCoverage, maximumRecordsToCreate);
	}

	public CSVFieldLevelFileGenerator(
		final int maximumRecordsToCreate) {

		initialise(false, maximumRecordsToCreate);
	}	
	
	private void initialise(
		final boolean trackCoverage, 
		final int maximumRecordsToCreate) {
	
		dummyFields = new ArrayList<DummyField>();
		dummyFieldFromName = new HashMap<String, DummyField>();	
		
		this.trackCoverage = trackCoverage;
		stoppingCondition = TestDataCoverageCondition.NUMBER_OF_RECORDS_ACHIEVED;
		this.maximumRecordsToCreate = maximumRecordsToCreate;
		ithRecordCreated = 0;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void generateDummyFile(final File dummyFile) 
		throws IOException {
		
		BufferedWriter writer
			= new BufferedWriter(new FileWriter(dummyFile));
		writer.write(generateCSVHeaderRow());
		while (ithRecordCreated < maximumRecordsToCreate) {
			if (coverageComplete() == false) {
				writer.write(generateCSVDataRow());
			}
			ithRecordCreated++;
		}
		writer.flush();
		writer.close();
	}

	private String generateCSVHeaderRow() {		
		StringBuilder csvRow = new StringBuilder();
		for (DummyField dummyField : dummyFields) {
			csvRow.append(dummyField.getFieldName());
		}
		
		return csvRow.toString();
	}	
	
	protected String generateCSVDataRow() {		
		StringBuilder csvRow = new StringBuilder();
		for (DummyField dummyField : dummyFields) {
			csvRow.append(dummyField.generateValue());
		}
		
		return csvRow.toString();
	}	
	
	public void stopWhenMaximumRecordsCreated() {
		stoppingCondition 
			= TestDataCoverageCondition.NUMBER_OF_RECORDS_ACHIEVED;
	}
	
	public void stopWhenCommonValuesCovered() {
		stoppingCondition 
			= TestDataCoverageCondition.COMMON_COVERAGE_ACHIEVED;		
	}
	
	public void stopWhenUncommonValuesCovered() {
		stoppingCondition 
			= TestDataCoverageCondition.UNCOMMON_COVERAGE_ACHIEVED;	
	}
	
	public void stopWhenErrorValuesCovered() {
		stoppingCondition 
			= TestDataCoverageCondition.ERROR_COVERAGE_ACHIEVED;
	}
	
	public void stopWhenAllValuesCovered() {
		stoppingCondition 
			= TestDataCoverageCondition.ALL_COVERAGE_ACHIEVED;		
	}
	
	public void addField(final String fieldName) {
		DummyField dummyField = DummyField.newInstance(fieldName);
		dummyField.setTrackCoverage(trackCoverage);
		dummyFieldFromName.put(fieldName, dummyField);
	}
	
	public void setOutcomeLikelihoods(
		final String fieldName,
		final double commonValueLikelihood,
		final double uncommonValueLikelihood,
		final double blankValueLikelihood) {
		
		DummyField dummyField = getDummyField(fieldName);
		dummyField.setOutcomeLikelihoods(
			commonValueLikelihood, 
			uncommonValueLikelihood, 
			blankValueLikelihood);
	}
	
	public void addCommonFieldValue(
		final String fieldName, 
		final String... commonValues) {

		DummyField dummyField = getDummyField(fieldName);
		for (String commonValue : commonValues) {
			dummyField.addCommonFieldValue(commonValue);			
		}
	}
	
	public void addUncommonFieldValue(
		final String fieldName, 
		final String... uncommonValues) {
		
		DummyField dummyField = getDummyField(fieldName);
		for (String uncommonValue : uncommonValues) {
			dummyField.addUncommonFieldValue(uncommonValue);			
		}
	}
	
	public void addErrorFieldValue(
		final String fieldName,
		final String... errorValues) {
		
		DummyField dummyField = getDummyField(fieldName);
		for (String errorValue : errorValues) {
			dummyField.addErrorFieldValue(errorValue);			
		}
	}
	
	private DummyField getDummyField(final String fieldName) {
		DummyField dummyField = dummyFieldFromName.get(fieldName);
		return dummyField;
	}
	
	
	private boolean coverageComplete() {
		if (stoppingCondition == TestDataCoverageCondition.COMMON_COVERAGE_ACHIEVED) {
			for (DummyField dummyField : dummyFields) {
				if (dummyField.fullCommonValueCasesAchieved() == false) {
					return false;
				}
			}
			return true;
		}
		else if (stoppingCondition == TestDataCoverageCondition.UNCOMMON_COVERAGE_ACHIEVED) {
			for (DummyField dummyField : dummyFields) {
				if (dummyField.fullUncommonValueCasesAchieved() == false) {
					return false;
				}
			}
			return true;
		}
		else if (stoppingCondition == TestDataCoverageCondition.ERROR_COVERAGE_ACHIEVED) {
			for (DummyField dummyField : dummyFields) {
				if (dummyField.fullErrorValueCasesAchieved() == false) {
					return false;
				}
			}
			return true;			
		}
		else if (stoppingCondition == TestDataCoverageCondition.ALL_COVERAGE_ACHIEVED) {
			for (DummyField dummyField : dummyFields) {
				if (dummyField.fullCoverageAchieved() == false) {
					return false;
				}
			}
			return true;			
		}
		else {
			return false;
		}
	}

	public ArrayList<DummyField> getDummyFields() {
		return dummyFields;
	}
	
	public String generateValue(final String fieldName) {
		DummyField dummyField = getDummyField(fieldName);
		if (dummyField == null) {
			return null;
		}
		return dummyField.generateValue();
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


