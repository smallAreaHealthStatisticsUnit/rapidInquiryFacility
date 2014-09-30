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

	LOAD(
		1, 
		"rifDataLoaderActivityStep.load.label",
		"rifDataLoaderActivityStep.load.statusMessage"),
	CLEAN(
		2, 
		"rifDataLoaderActivityStep.clean.label",
		"rifDataLoaderActivityStep.clean.statusMessage"),
	CONVERT(
		3, 
		"rifDataLoaderActivityStep.convert.label",
		"rifDataLoaderActivityStep.convert.statusMessage"),
	COMBINE(
		4, 
		"rifDataLoaderActivityStep.combine.label",
		"rifDataLoaderActivityStep.combine.statusMessage"),
	OPTIMISE(
		5, 
		"rifDataLoaderActivityStep.optimise.label",
		"rifDataLoaderActivityStep.optimise.statusMessage"),
	CHECK(
		6, 
		"rifDataLoaderActivityStep.optimise.label",
		"rifDataLoaderActivityStep.optimise.statusMessage"),		
	PUBLISH(
		7, 
		"rifDataLoaderActivityStep.publish.label",
		"rifDataLoaderActivityStep.publish.statusMessage"),
	DELETE(
		7, 
		"rifDataLoaderActivityStep.delete.label",
		"rifDataLoaderActivityStep.delete.statusMessage");
		
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
		
		return LOAD;
	}
	
	/**
	 * Gets the previous activity step.
	 *
	 * @return the previous activity step
	 */
	public RIFDataLoaderActivityStep getPreviousActivityStep() {

		if (this == RIFDataLoaderActivityStep.PUBLISH) {
			return CHECK;
		}
		else if (this == RIFDataLoaderActivityStep.CHECK) {
			return OPTIMISE;
		}
		else if (this == RIFDataLoaderActivityStep.OPTIMISE) {
			return COMBINE;
		}
		else if (this == RIFDataLoaderActivityStep.COMBINE) {
			return CONVERT;
		}		
		else if (this == RIFDataLoaderActivityStep.CONVERT) {
			return CLEAN;
		}		
		else {
			//CLEAN, DELETE
			return LOAD;
		}
	}
	
	/**
	 * Gets the next activity step.
	 *
	 * @return the next activity step
	 */
	public RIFDataLoaderActivityStep getNextActivityStep() {
		
		if (this == RIFDataLoaderActivityStep.LOAD) {
			return CLEAN;
		}
		else if (this == RIFDataLoaderActivityStep.CLEAN) {
			return CONVERT;
		}
		else if (this == RIFDataLoaderActivityStep.CONVERT) {
			return COMBINE;
		}
		else if (this == RIFDataLoaderActivityStep.COMBINE) {
			return CHECK;
		}
		else if (this == RIFDataLoaderActivityStep.CHECK) {
			return PUBLISH;
		}
		else {
			//DELETE, PUBLISH
			return LOAD;
		}
	}
	
}


