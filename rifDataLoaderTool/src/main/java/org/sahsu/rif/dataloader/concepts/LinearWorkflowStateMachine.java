package org.sahsu.rif.dataloader.concepts;

import java.util.ArrayList;

/**
 * This is the main work flow where a single data source is promoted to
 * the published state using a sequence of temporary tables that are in 
 * a 1:1 relationship with the data source.  
 * 
 * For example, at the end of the Clean operation, there is one cleaned
 * table that corresponds with the original data set.  For Convert, it means
 * that a single data source will be associated with a single converted table
 * in the schema.
 * 
 * This type of workflow contrasts with a branched workflow, where data 
 * sets are combined or split.  
 *
 * <hr>
 * Copyright 2015 Imperial College London, developed by the Small Area
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

public class LinearWorkflowStateMachine 
	implements WorkflowStateMachine {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private WorkflowState currentWorkflowState;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private LinearWorkflowStateMachine() {
		first();
	}

	private LinearWorkflowStateMachine(
		final WorkflowState currentWorkflowState) {
		
		this.currentWorkflowState = currentWorkflowState;
	}
	
	public static LinearWorkflowStateMachine newInstance() {
		LinearWorkflowStateMachine stateMachine
			= new LinearWorkflowStateMachine();
		return stateMachine;
	}
	
	public static LinearWorkflowStateMachine createCopy(
		final LinearWorkflowStateMachine originalStateMachine) {
		
		LinearWorkflowStateMachine cloneStateMachine
			= new LinearWorkflowStateMachine(
				originalStateMachine.getCurrentWorkflowState());
		return cloneStateMachine;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void first() {
		currentWorkflowState = WorkflowState.START;
	}
	
	public void previous() {

		if (currentWorkflowState == WorkflowState.PUBLISH) {
			currentWorkflowState = WorkflowState.CHECK;
		}
		else if (currentWorkflowState == WorkflowState.CHECK) {
			currentWorkflowState = WorkflowState.OPTIMISE;
		}
		else if (currentWorkflowState == WorkflowState.OPTIMISE) {
			currentWorkflowState = WorkflowState.CONVERT;
		}		
		else if (currentWorkflowState == WorkflowState.CONVERT) {
			currentWorkflowState = WorkflowState.CLEAN;
		}
		else if (currentWorkflowState == WorkflowState.CLEAN) {
			currentWorkflowState = WorkflowState.EXTRACT;
		}
		else {
			//DELETE
			currentWorkflowState = WorkflowState.START;
		}		
	}

	public void next() {

		if (currentWorkflowState == WorkflowState.START) {
			currentWorkflowState = WorkflowState.EXTRACT;
		}
		else if (currentWorkflowState == WorkflowState.EXTRACT) {
			currentWorkflowState = WorkflowState.CLEAN;
		}
		else if (currentWorkflowState == WorkflowState.CLEAN) {
			currentWorkflowState = WorkflowState.CONVERT;
		}
		else if (currentWorkflowState == WorkflowState.CONVERT) {
			currentWorkflowState = WorkflowState.OPTIMISE;
		}
		else if (currentWorkflowState == WorkflowState.OPTIMISE) {
			currentWorkflowState = WorkflowState.CHECK;
		}
		else if (currentWorkflowState == WorkflowState.CHECK) {
			currentWorkflowState = WorkflowState.PUBLISH;
		}
		else if (currentWorkflowState == WorkflowState.PUBLISH) {
			currentWorkflowState = WorkflowState.STOP;
		}		
	}
	
	public void last() {
		currentWorkflowState = WorkflowState.STOP;		
	}
	
	public WorkflowState getCurrentWorkflowState() {

		return currentWorkflowState;
	}
	
	public ArrayList<WorkflowState> getAllWorkStatesInOrder() {
		ArrayList<WorkflowState> allWorkflowStates
			= new ArrayList<WorkflowState>();
		allWorkflowStates.add(WorkflowState.START);
		allWorkflowStates.add(WorkflowState.EXTRACT);
		allWorkflowStates.add(WorkflowState.CLEAN);
		allWorkflowStates.add(WorkflowState.CONVERT);
		allWorkflowStates.add(WorkflowState.OPTIMISE);
		allWorkflowStates.add(WorkflowState.CHECK);
		allWorkflowStates.add(WorkflowState.PUBLISH);
		allWorkflowStates.add(WorkflowState.STOP);
		
		return allWorkflowStates;
	}
	
	
	public String[] getAllWorkStateNamesInOrder() {
		ArrayList<String> allWorkflowStateNames
			= new ArrayList<String>();
		allWorkflowStateNames.add(WorkflowState.START.getStateName());
		allWorkflowStateNames.add(WorkflowState.EXTRACT.getStateName());
		allWorkflowStateNames.add(WorkflowState.CLEAN.getStateName());
		allWorkflowStateNames.add(WorkflowState.CONVERT.getStateName());
		allWorkflowStateNames.add(WorkflowState.OPTIMISE.getStateName());
		allWorkflowStateNames.add(WorkflowState.CHECK.getStateName());
		allWorkflowStateNames.add(WorkflowState.PUBLISH.getStateName());
		allWorkflowStateNames.add(WorkflowState.STOP.getStateName());
		
		String[] results 
			= allWorkflowStateNames.toArray(new String[0]);
		return results;
		
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


