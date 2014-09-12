package rifDataLoaderTool.system;


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

public enum RIFDataLoaderActivityStep {

	LOAD_RAW_DATA_TABLE(
			1, 
			"rifDataLoaderActivityStep.loadRawDataTable.label",
			"rifDataLoaderActivityStep.loadRawDataTable.statusMessage"),
		CLEAN_DATA_TABLE(
			2, 
			"rifDataLoaderActivityStep.cleanDataTable.label",
			"rifDataLoaderActivityStep.cleanDataTable.statusMessage"),
		APPLY_RIF_SPECIFIC_TRANSFORMATION(
			3, 
			"rifDataLoaderActivityStep.applyRIFSpecificTransformation.label",
			"rifDataLoaderActivityStep.applyRIFSpecificTransformation.statusMessage"),
		COMBINE_TABLES(
			4, 
			"rifDataLoaderActivityStep.combineTables.label",
			"rifDataLoaderActivityStep.combineTables.statusMessage"),
		OPTIMISE_TABLE(
			5, 
			"rifDataLoaderActivityStep.optimiseTable.label",
			"rifDataLoaderActivityStep.optimiseTable.statusMessage"),
		PUBLISH_TABLE(
			6, 
			"rifDataLoaderActivityStep.publishTable.label",
			"rifDataLoaderActivityStep.publishTable.statusMessage");
		
	private int stepNumber;
	private String stepPropertyName;
	private String statusPropertyName;
	private String statusMessage;
	
	private RIFDataLoaderActivityStep(
		final int stepNumber,
		final String stepPropertyName,
		final String statusPropertyName) {
		
		this.stepNumber = stepNumber;
		this.stepPropertyName = stepPropertyName;
		this.statusPropertyName = statusPropertyName;
	}
	
	public int getStepNumber() {
		return stepNumber;
	}
	
	public String getStepName() {
		return RIFDataLoaderMessages.getMessage(stepPropertyName);
	}
	
	public String getCompletedStatusMessage() {
		return RIFDataLoaderMessages.getMessage(statusPropertyName);		
	}
	
	public int getTotalSteps() {
		return 6;
	}
	
	/**
	 * Gets the first activity step.
	 *
	 * @return the first activity step
	 */
	public static RIFDataLoaderActivityStep getFirstActivityStep() {
		
		return LOAD_RAW_DATA_TABLE;
	}
	
	/**
	 * Gets the previous activity step.
	 *
	 * @return the previous activity step
	 */
	public RIFDataLoaderActivityStep getPreviousActivityStep() {

		if (this == RIFDataLoaderActivityStep.PUBLISH_TABLE) {
			return OPTIMISE_TABLE;
		}
		else if (this == RIFDataLoaderActivityStep.OPTIMISE_TABLE) {
			return COMBINE_TABLES;
		}
		else if (this == RIFDataLoaderActivityStep.COMBINE_TABLES) {
			return APPLY_RIF_SPECIFIC_TRANSFORMATION;
		}
		else if (this == RIFDataLoaderActivityStep.APPLY_RIF_SPECIFIC_TRANSFORMATION) {
			return CLEAN_DATA_TABLE;
		}
		else {
			return LOAD_RAW_DATA_TABLE;
		}
	}
	
	/**
	 * Gets the next activity step.
	 *
	 * @return the next activity step
	 */
	public RIFDataLoaderActivityStep getNextActivityStep() {
		
		if (this == RIFDataLoaderActivityStep.LOAD_RAW_DATA_TABLE) {
			return CLEAN_DATA_TABLE;
		}
		else if (this == RIFDataLoaderActivityStep.CLEAN_DATA_TABLE) {
			return APPLY_RIF_SPECIFIC_TRANSFORMATION;
		}
		else if (this == RIFDataLoaderActivityStep.APPLY_RIF_SPECIFIC_TRANSFORMATION) {
			return COMBINE_TABLES;
		}
		else if (this == RIFDataLoaderActivityStep.COMBINE_TABLES) {
			return OPTIMISE_TABLE;
		}
		else {
			return PUBLISH_TABLE;
		}
	}
	
}


