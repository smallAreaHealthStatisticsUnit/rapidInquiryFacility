package rifDataLoaderTool.system;


/**
 * Describes the sequence of steps the a RIF Manager would use to process a new data source
 * in the RIF.  Note that unlike {@link rifDataLoaderTool.system.RIFTemporaryTablePrefixes}, 
 * this class describes steps that are meant for a human user to understand.  Each step
 * may be associated with one or more steps meant for database processing.
 * <p>
 * For example, RIF managers will need to load, clean and convert their data as part of the 
 * first three steps of the work flow.  Load corresponds to the creation of one type of table.  However,
 * in database processing, clean has three steps: search and replace, validation and casting, none
 * of which are made into discrete steps for the user.
 * 
 * <p>
 * It is important to bear in mind that there is a work flow for human users and there is a work flow
 * for code generation activities.
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
		"rifDataLoaderActivityStep.load.statusMessage",
		"ld_"),
	CLEAN(
		2, 
		"rifDataLoaderActivityStep.clean.label",
		"rifDataLoaderActivityStep.clean.statusMessage",
		"cl_"),
	CONVERT(
		3, 
		"rifDataLoaderActivityStep.convert.label",
		"rifDataLoaderActivityStep.convert.statusMessage",
		"cv_"),
	COMBINE(
		4, 
		"rifDataLoaderActivityStep.combine.label",
		"rifDataLoaderActivityStep.combine.statusMessage",
		"cb_"),
	OPTIMISE(
		5, 
		"rifDataLoaderActivityStep.optimise.label",
		"rifDataLoaderActivityStep.optimise.statusMessage",
		"op_"),
	CHECK(
		6, 
		"rifDataLoaderActivityStep.optimise.label",
		"rifDataLoaderActivityStep.optimise.statusMessage",
		"ch_"),		
	PUBLISH(
		7, 
		"rifDataLoaderActivityStep.publish.label",
		"rifDataLoaderActivityStep.publish.statusMessage",
		"pb_"),
	DELETE(
		7, 
		"rifDataLoaderActivityStep.delete.label",
		"rifDataLoaderActivityStep.delete.statusMessage",
		"dl_");
		
	private int stepNumber;
	private String stepPropertyName;
	private String statusPropertyName;
	private String statusMessage;
	private String tablePrefix;
	
	private RIFDataLoaderActivityStep(
		final int stepNumber,
		final String stepPropertyName,
		final String statusPropertyName,
		final String tablePrefix) {
		
		this.stepNumber = stepNumber;
		this.stepPropertyName = stepPropertyName;
		this.statusPropertyName = statusPropertyName;
		this.tablePrefix = tablePrefix;
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
	
	
	public String getTablePrefix() {
		return tablePrefix;
	}
	
	public String getTableName(
		final String coreTableName) {
		
		StringBuilder tableName = new StringBuilder();
		tableName.append(tablePrefix);
		tableName.append(getTablePrefix());
		return tableName.toString();
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


