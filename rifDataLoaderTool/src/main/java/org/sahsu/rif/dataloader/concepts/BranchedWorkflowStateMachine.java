package org.sahsu.rif.dataloader.concepts;

import java.util.ArrayList;

/**
 * Describes the steps of a workflow that is used to combine or split files.
 * In most cases, we expect users to load a single CSV file that will be transformed
 * through these steps:
 * <ol>
 * <li>Load</li>
 * <li>Clean</li>
 * <li>Convert</li>
 * <li>Optimise</li>
 * <li>Check</li>
 * <li>Publish</li>
 * </ol>
 *
 * This workflow is described in 
 * {@link LinearWorkflowStateMachine}.
 * In cases where a group wants to combine multiple data sets or split them into
 * separate tables, additional steps for "Split" and "Combine" are needed.  
 * 
 * <p>
 * For example, we anticipate that we will need combine and split to process the 
 * UK's HES data sets, which have millions of rows and dozens of columns.  For 
 * example, SAHSU separates HES columns into different tables:
 * <ul>
 * <li>core columns: the most commonly used fields.</li>
 * <li>extension columns: rarely used fields </li>
 * <li>conf columns: fields that are considered highly sensitive and will be encrypted
 * and have different access permissions </li>
 * </ul>
 *
 * <p>
 * We receive files in yearly updates, so we could have many files that cover different
 * fiscal or calendar years.  In the interests of efficiency, we may want to combine
 * yearly records into a single table.  So given several large HES CSV files, we might
 * first split the columns into tables having different themes and then combine multiple
 * years of data.  
 * </p>
 * 
 * <p>
 * We anticipate that some groups may combine, split, or do some combination of the two.
 * However, we also expect that the typical workflow will not need these two steps.
 * Having an arbitrary sequence of repeating steps can complicate the way our design
 * would handle both linear and branched work flows.  Therefore, we have separated
 * split and combine and put them into their own mini-workflow that can be inserted
 * in the linear work flow.  The inputs of a branched workflow are tables that have
 * been converted and the outputs are tables that can be accepted by the Optimise step.
 * </p>
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

public class BranchedWorkflowStateMachine 
	implements WorkflowStateMachine {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private int currentIndex;
	private ArrayList<WorkflowState> workFlowStates;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public BranchedWorkflowStateMachine() {
		workFlowStates = new ArrayList<WorkflowState>();
	}
	
	public void startWorkflow() {
		workFlowStates.add(WorkflowState.CONVERT);
	}
	
	public void addSplitStep() {
		workFlowStates.add(WorkflowState.SPLIT);
	}
	
	public void addCombineStep() {
		workFlowStates.add(WorkflowState.COMBINE);		
	}
	
	public void finishWorkflow() {
		workFlowStates.add(WorkflowState.OPTIMISE);		
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public WorkflowState getFirstState() {
		return workFlowStates.get(0);			
	}
	
	public WorkflowState getLastState() {
		int numberOfWorkFlowStates = workFlowStates.size();
		return workFlowStates.get(numberOfWorkFlowStates - 1);		
	}
	
	public void first() {
		currentIndex = 0;
		
	}
	
	public void previous() {
		currentIndex--;
		if (currentIndex < 0) {
			currentIndex = 0;
		}
	}
	
	public void next() {
		currentIndex++;
		int numberOfWorkflowStates = workFlowStates.size();
		if (currentIndex == numberOfWorkflowStates) {
			currentIndex = numberOfWorkflowStates;
		}
	}
	
	public void last() {
		currentIndex = workFlowStates.size() - 1;		
	}
	
	public WorkflowState getCurrentWorkflowState() {

		return workFlowStates.get(currentIndex);
	}
	
	public ArrayList<WorkflowState> getAllWorkStatesInOrder() {
		return workFlowStates;
	}	
	
	
	public String[] getAllWorkStateNamesInOrder() {
		ArrayList<String> allWorkflowStateNames
			= new ArrayList<String>();
		for (WorkflowState workFlowState : workFlowStates) {
			allWorkflowStateNames.add(workFlowState.getStateName());			
		}
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


