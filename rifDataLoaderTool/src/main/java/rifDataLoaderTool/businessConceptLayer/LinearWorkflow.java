package rifDataLoaderTool.businessConceptLayer;


import java.util.ArrayList;


/**
 *
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

public class LinearWorkflow {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private LinearWorkflowStateMachine stateMachine;
	private WorkflowState startWorkflowState;
	private WorkflowState stopWorkflowState;
	
	private ArrayList<DataSetConfiguration> dataSetConfigurations;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private LinearWorkflow() {
		stateMachine = LinearWorkflowStateMachine.newInstance();
		startWorkflowState = WorkflowState.LOAD;
		stopWorkflowState = WorkflowState.PUBLISH;
		
		dataSetConfigurations = new ArrayList<DataSetConfiguration>();
	}

	public static LinearWorkflow newInstance() {
		LinearWorkflow linearWorkflow = new LinearWorkflow();
				
		return linearWorkflow;
	}
	
	
	public static LinearWorkflow createCopy(
		final LinearWorkflow originalWorkflow) {
		
		LinearWorkflow cloneWorkflow = new LinearWorkflow();
		
		
		return cloneWorkflow;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void resetWorkflow() {
		stateMachine.first();
	}
	
	public WorkflowState getCurrentWorkflowState() {
		return stateMachine.getCurrentWorkflowState();
	}
	
	public boolean hasNext() {
		WorkflowState currentWorkflowState
			= getCurrentWorkflowState();
		
		if (currentWorkflowState == stopWorkflowState) {
			return false;
		}
		return true;
	}
	
	public void next() {
		stateMachine.next();		
	}
	
	public ArrayList<DataSetConfiguration> getDataSetConfigurations() {
		return dataSetConfigurations;
	}
	
	public void addDataSetConfiguration(
		final DataSetConfiguration dataSetConfiguration) {
		
		dataSetConfigurations.add(dataSetConfiguration);
	}

	public void setDataSetConfigurations(
		final ArrayList<DataSetConfiguration> dataSetConfigurations) {
		
		this.dataSetConfigurations = dataSetConfigurations;
	}
		
	public WorkflowState getStartWorkflowState() {
		return startWorkflowState;
	}
	
	public void setStartWorkflowState(
		final WorkflowState startWorkflowState) {

		this.startWorkflowState = startWorkflowState;
	}
	
	public WorkflowState getStopWorkflowState() {
		return stopWorkflowState;
	}

	public void setStopWorkflowState(
		final WorkflowState stopWorkflowState) {

		this.stopWorkflowState = stopWorkflowState;
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


