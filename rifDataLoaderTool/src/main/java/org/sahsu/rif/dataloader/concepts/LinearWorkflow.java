package org.sahsu.rif.dataloader.concepts;

import java.util.ArrayList;
import java.util.Objects;


/**
 * A main business class that describes the typical workflow expected by the RIF
 * database.  The workflow is designed to process a collection of data set 
 * configurations from a specific start state to a specific stop state.
 * </p>
 * 
 * <p>
 * The LinearWorkflow caters to the use case where a RIF manager is importing a single
 * CSV file in order to create a single published data set.  The steps for this
 * workflow are:
 * <ol>
 * <li><b>Load</b>: Initially load a data set into the database.  In the load table,
 * the column names may be provided by a header line in a CSV file.  If there is no
 * information available about column names, they will be auto-generated.  In the
 * load {@link rifDataLoaderTool.businessConceptLayer.WorkflowState}, all fields
 * are represented as columns with a data type of 'text'.
 * </li>
 * <li><b>Clean</b>: In this stage, fields from the load table can be renamed with 
 * more sensible field names and they can be associated with various rif data types
 * (see {@link rifDataLoaderTool.businessConceptLayer.rifDataTypes}).  During the 
 * clean step, transformation rules associated with each data type are applied to 
 * text column values and the results appear in a temporary table that also has 
 * text column values.  Then, as a second step, the cleaned text columns are converted
 * to more specific data types such as doubles, integers, years, ages, etc.
 * </li>
 * <li><b>Convert</b>: Maps fields from a cleaned table to fields that are expected
 * in the {@link rifDataLoaderTool.businessConceptLayer.RIFSchemaArea}.  For example,
 * if the rif schema area is a numerator data set, then Convert would map a field from
 * the cleaned table to the field "year", which should appear in a published numerator
 * table.  A converted table may act as an input for a Split or Combine step (see
 * {@link rifDataLoaderTool.businessConceptLayer.BranchedWorkflow}.
 * </li>
 * <li>
 * <b>Optimise</b>: This step optimises properties of a converted table so that it can
 * perform well as a published table.  A converted table will usually have a minimum
 * set of fields which are required by some part of the RIF schema.  These fields are
 * usually indexed to enhance performance and do not need to be specifically configured
 * by the RIF manager.  However, other data set fields, which have a 
 * {@link FieldRequirementLevel} of
 * <code>EXTRA_FIELD</code>, may be optionally indexed by the RIF manager. Note that
 * the input for an Optimise step can either be tables created by the Convert step, 
 * or tables which have been produced by a 
 * {@link rifDataLoaderTool.businessConceptLayer.BranchedWorkflow} that uses Split and
 * Combine steps.
 * </li>
 * <li>
 * <b>Check</b>: In this {@link rifDataLoaderTool.businessConceptLayer.WorkflowState},
 * the RIF manager can define data quality checks that can be used by scientists when
 * they assess the results of an extract.
 * </li>
 * <li>
 * <b>Publish</b>: In this {@link rifDataLoaderTool.businessConceptLayer.WorkflowState},
 * a registry of published data sets is updated so that the published data set is 
 * visible to RIF users.
 * </li>
 * </ol>
 * </p>
 * 
 * <p>
 * Each LinearWorkflow has:
 * <ul>
 * <li>a collection of data set configurations</li>
 * <li>a start state</li>
 * <li>a stop state</li>
 * </ul>
 * 
 * <p>
 * The data set configurations must all have the same start state so that they may
 * be processed in a uniform way.
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

public class LinearWorkflow 
	extends AbstractWorkflow {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================


	
	// ==========================================
	// Section Construction
	// ==========================================

	private LinearWorkflow() {
		setStateMachine(LinearWorkflowStateMachine.newInstance());
	}

	public void initialise() {
		setStartWorkflowState(WorkflowState.START);
		setStopWorkflowState(WorkflowState.STOP);
	}
	
	public static LinearWorkflow newInstance() {
		LinearWorkflow linearWorkflow = new LinearWorkflow();
		linearWorkflow.initialise();
				
		return linearWorkflow;
	}
	
	
	public static LinearWorkflow createCopy(
		final LinearWorkflow originalLinearWorkflow) {
		
		LinearWorkflow cloneLinearWorkflow = new LinearWorkflow();
		copyInto(originalLinearWorkflow, cloneLinearWorkflow);
		
		return cloneLinearWorkflow;
	}
	
	public static void copyInto(
		final LinearWorkflow sourceLinearWorkflow, 
		final LinearWorkflow destinationWorkflow) {
		
		copyAttributes(
			sourceLinearWorkflow, 
			destinationWorkflow);		
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	public boolean hasIdenticalContents(final LinearWorkflow otherLinearWorkflow) {
		
		if (otherLinearWorkflow == null) {
			return false;
		}
		
		if (this == otherLinearWorkflow) {
			return true;
		}

		//For the purposes of checking identical contents between two 
		//instances, we will not bother comparing workflowStateMachine
		//objects.  They only hold the current state of the workflow as
		//it is completes execution.

		ArrayList<DataSetConfiguration> dataSetConfigurations
			= getDataSetConfigurations();
		ArrayList<DataSetConfiguration> otherDataSetConfigurations
			= otherLinearWorkflow.getDataSetConfigurations();
		if (DataSetConfiguration.hasIdenticalContents(
			dataSetConfigurations, 
			otherDataSetConfigurations) == false) {

			return false;
		}
		
		WorkflowState startWorkflowState
			= getStartWorkflowState();
		WorkflowState otherStartWorkflowState
			= otherLinearWorkflow.getStartWorkflowState();
		if (startWorkflowState != otherStartWorkflowState) {
			return false;
		}
		
		WorkflowState stopWorkflowState
			= getStartWorkflowState();
		WorkflowState otherStopWorkflowState
			= otherLinearWorkflow.getStopWorkflowState();
		if (stopWorkflowState != otherStopWorkflowState) {
			return false;
		}

		if (Objects.deepEquals(
			isNewRecord(), 
			otherLinearWorkflow.isNewRecord()) == false) {
			return false;
		}
		
		return true;
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


