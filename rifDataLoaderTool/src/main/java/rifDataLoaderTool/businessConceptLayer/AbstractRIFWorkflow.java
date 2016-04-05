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

abstract class AbstractRIFWorkflow {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private WorkflowStateMachine workflowStateMachine;
	private ArrayList<DataSetConfiguration> dataSetConfigurations;
	private WorkflowState startWorkflowState;
	private WorkflowState stopWorkflowState;
	
	private boolean isNewRecord;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public AbstractRIFWorkflow() {
		dataSetConfigurations = new ArrayList<DataSetConfiguration>();
		
		isNewRecord = true;
	}

	abstract public void initialise();
	
	public static void copyAttributes(
		final AbstractRIFWorkflow sourceWorkflow, 
		final AbstractRIFWorkflow destinationWorkflow) {
		
		destinationWorkflow.setNewRecord(sourceWorkflow.isNewRecord());
		
		WorkflowState sourceStartWorkflowState
			= sourceWorkflow.getStartWorkflowState();
		destinationWorkflow.setStartWorkflowState(sourceStartWorkflowState);
		
		
		WorkflowState sourceStopWorkflowState
			= sourceWorkflow.getStopWorkflowState();
		destinationWorkflow.setStopWorkflowState(sourceStopWorkflowState);
		
		ArrayList<DataSetConfiguration> sourceDataSetConfigurations
			= sourceWorkflow.getDataSetConfigurations();
		ArrayList<DataSetConfiguration> destinationDataSetConfigurations
			= DataSetConfiguration.createCopy(sourceDataSetConfigurations);
		destinationWorkflow.setDataSetConfigurations(destinationDataSetConfigurations);
	}
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public boolean isNewRecord() {
		return isNewRecord;
	}
	
	public void setNewRecord(
		final boolean isNewRecord) {

		this.isNewRecord = isNewRecord;
	}
	
	public void replaceDataSetConfiguration(
		final DataSetConfiguration originalDataSetConfiguration,
		final DataSetConfiguration revisedDataSetConfiguration) {
		
		int index
			= dataSetConfigurations.indexOf(originalDataSetConfiguration);
		if (index != -1) {
			dataSetConfigurations.set(index, revisedDataSetConfiguration);
		}	

	}
	
	
	public void setStateMachine(
		final WorkflowStateMachine workflowStateMachine) {
		
		this.workflowStateMachine = workflowStateMachine;
	}
	
	public ArrayList<DataSetConfiguration> getDataSetConfigurations() {
		return dataSetConfigurations;
	}
	
	public String[] getDataSetConfigurationNames() {
		ArrayList<String> names = new ArrayList<String>();
		for (DataSetConfiguration dataSetConfiguration : dataSetConfigurations) {
			names.add(dataSetConfiguration.getName());
		}
		
		return names.toArray(new String[0]);
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
	
	public void resetWorkflow() {
		workflowStateMachine.first();
	}
	
	public WorkflowState getCurrentWorkflowState() {
		return workflowStateMachine.getCurrentWorkflowState();
	}

	public boolean next() {
		workflowStateMachine.next();	
		
		WorkflowState currentWorkflowState
			= getCurrentWorkflowState();
		if (currentWorkflowState == WorkflowState.STOP ||
			(currentWorkflowState.getStateSequenceNumber() > stopWorkflowState.getStateSequenceNumber())) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public void clearDataSetConfigurations() {	
		dataSetConfigurations.clear();
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


