package rifDataLoaderTool.businessConceptLayer;

import rifDataLoaderTool.system.RIFDataLoaderMessages;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;

import java.text.Collator;
import java.util.ArrayList;

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

public enum WorkflowState {

	START(
		1,
		"start",
		"workflowState.start.label",
		"workflowState.start.statusMessage",		
		null),
	LOAD(
		2, 
		"load",
		"workflowState.load.label",
		"workflowState.load.statusMessage",
		"ld_"),
	CLEAN(
		3, 
		"clean",
		"workflowState.clean.label",
		"workflowState.clean.statusMessage",
		"cl_"),
	CONVERT(
		4, 
		"convert",
		"workflowState.convert.label",
		"workflowState.convert.statusMessage",
		"cv_"),
	SPLIT(
		5, 
		"split",
		"workflowState.split.label",
		"workflowState.split.statusMessage",
		"cb_"),
	COMBINE(
		6, 
		"combine",
		"workflowState.combine.label",
		"workflowState.combine.statusMessage",
		"cb_"),		
	OPTIMISE(
		7, 
		"optimise",
		"workflowState.optimise.label",
		"workflowState.optimise.statusMessage",
		"op_"),
	CHECK(
		8, 
		"check",
		"workflowState.check.label",
		"workflowState.check.statusMessage",
		"ch_"),		
	PUBLISH(
		9, 
		"publish",
		"workflowState.publish.label",
		"workflowState.publish.statusMessage",
		"pb_"),
	STOP(
		10,
		"stop",
		"workflowState.stop.label",
		"workflowState.stop.statusMessage",		
		null),
	DELETE(
		11, 
		"delete",
		"workflowState.delete.label",
		"workflowState.delete.statusMessage",
		"dl_");
		
	private int stateSequenceNumber;
	private String code;
	private String statePropertyName;
	private String statusPropertyName;
	private String tablePrefix;
	
	private WorkflowState(
		final int stepNumber,
		final String code,
		final String stepPropertyName,
		final String statusPropertyName,
		final String tablePrefix) {
		
		this.stateSequenceNumber = stepNumber;
		this.code = code;
		this.statePropertyName = stepPropertyName;
		this.statusPropertyName = statusPropertyName;
		this.tablePrefix = tablePrefix;
	}
	
	public static boolean areStatesInOrder(
		final WorkflowState startWorkState,
		final WorkflowState endWorkState) {
		
		if (endWorkState.getStateSequenceNumber() >= startWorkState.getStateSequenceNumber()) {
			return true;
		}
		
		return false;		
	}
	
	public int getStateSequenceNumber() {
		return stateSequenceNumber;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getStateName() {
		return RIFDataLoaderMessages.getMessage(statePropertyName);
	}
	
	public String getCompletedStatusMessage() {
		return RIFDataLoaderMessages.getMessage(statusPropertyName);		
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
	
	
	public static String[] getAllStateNames() {
		ArrayList<String> stateNames = new ArrayList<String>();
		stateNames.add(LOAD.getStateName());
		stateNames.add(CLEAN.getStateName());
		stateNames.add(CONVERT.getStateName());
		stateNames.add(OPTIMISE.getStateName());
		stateNames.add(CHECK.getStateName());
		stateNames.add(PUBLISH.getStateName());
				
		String[] results
			= stateNames.toArray(new String[0]);
		return results;		
	}
	
	public static WorkflowState getWorkflowStateFromCode(
		final String code) {
		
		Collator collator = RIFDataLoaderToolMessages.getCollator();
		if (collator.equals(code, LOAD.getCode())) {
			return LOAD;
		}
		else if (collator.equals(code, CLEAN.getCode())) {
			return CLEAN;
		}
		else if (collator.equals(code, CONVERT.getCode())) {
			return CONVERT;
		}
		else if (collator.equals(code, SPLIT.getCode())) {
			return SPLIT;
		}
		else if (collator.equals(code, COMBINE.getCode())) {
			return COMBINE;
		}
		else if (collator.equals(code, OPTIMISE.getCode())) {
			return OPTIMISE;
		}
		else if (collator.equals(code, CHECK.getCode())) {
			return CHECK;
		}
		else if (collator.equals(code, PUBLISH.getCode())) {
			return PUBLISH;
		}
		else {
			assert false;
			return null;
		}

		
		
		
		
	}
	
}


