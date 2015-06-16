package rifDataLoaderTool.businessConceptLayer;

import java.util.ArrayList;

import rifDataLoaderTool.businessConceptLayer.rifDataTypes.RIFDataTypeInterface;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceSecurityException;



/**
 * Describes the properties of a table cleaning operation which can be used by
 * SQL code generators to create queries for different target databases such as
 * Postgresql and Microsoft SQL Server.
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

public final class CheckWorkflowConfiguration 
	extends AbstractRIFDataLoaderToolConcept {

	// ==========================================
	// Section Constants
	// ==========================================
	
	// ==========================================
	// Section Properties
	// ==========================================
	private DataSet dataSet;
	private ArrayList<String> duplicateRowCheckFields;
	private ArrayList<String> cleanedRowCheckFields;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private CheckWorkflowConfiguration() {
		duplicateRowCheckFields = new ArrayList<String>();
		cleanedRowCheckFields = new ArrayList<String>();
	}

	public static CheckWorkflowConfiguration newInstance() {
		
		CheckWorkflowConfiguration cleanWorkflowConfiguration
			= new CheckWorkflowConfiguration();
		DataSet dataSet = DataSet.newInstance();
		cleanWorkflowConfiguration.setDataSet(dataSet);
			
		return cleanWorkflowConfiguration;
	}
	
	public static CheckWorkflowConfiguration newInstance(
		final DataSet dataSet) {
		CheckWorkflowConfiguration cleanWorkflowConfiguration
			= new CheckWorkflowConfiguration();
		cleanWorkflowConfiguration.setDataSet(dataSet);
		
		return cleanWorkflowConfiguration;
	}
	
	public static CheckWorkflowConfiguration createCopy(
		final CheckWorkflowConfiguration originalTableConfiguration) {
	
		DataSet originalDataSet = originalTableConfiguration.getDataSet();	
		DataSet clonedDataSet = DataSet.createCopy(originalDataSet);
		CheckWorkflowConfiguration cloneCheckConfiguration
			= CheckWorkflowConfiguration.newInstance(clonedDataSet);
		ArrayList<String> originalDuplicateRowCheckFields
			= originalTableConfiguration.getDuplicateRowCheckFields();
		for (String originalDuplicateRowCheckField : originalDuplicateRowCheckFields) {
			cloneCheckConfiguration.addDuplicateRowCheckField(originalDuplicateRowCheckField);
		}
		
		ArrayList<String> originalCleanedRowCheckFields
			= originalTableConfiguration.getCleanedRowCheckFields();
		for (String originalCleanedRowCheckField : originalCleanedRowCheckFields) {
			cloneCheckConfiguration.addDuplicateRowCheckField(originalCleanedRowCheckField);
		}
			
				
		return cloneCheckConfiguration;
	}
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public String getCoreDataSetName() {		
		return dataSet.getCoreDataSetName();
	}	
	
	public ArrayList<String> getDuplicateRowCheckFields() {
		return duplicateRowCheckFields;
	}
		
	public void addDuplicateRowCheckField(final String duplicateRowCheckFieldName) {
		duplicateRowCheckFields.add(duplicateRowCheckFieldName);		
	}

	
	public ArrayList<String> getCleanedRowCheckFields() {
		return cleanedRowCheckFields;
	}
	
	public void addCleanedRowCheckField(final String cleanedRowCheckFieldName) {
		cleanedRowCheckFields.add(cleanedRowCheckFieldName);		
	}


	
	@Override
	public String getDisplayName() {
		return dataSet.getDisplayName();
	}
	
	public void setDataSet(
		final DataSet dataSet) {
		
		this.dataSet = dataSet;
	}
	
	public DataSet getDataSet() {		
		return dataSet;
	}
	
	public void clearFieldConfigurations() {
		duplicateRowCheckFields.clear();
		cleanedRowCheckFields.clear();
	}
	
	public void printFields() {
		System.out.println("Check Workflow Configuration");
		for (String duplicateRowCheckField : duplicateRowCheckFields) {
			System.out.println(
				"Check Duplicate Row Field:"+
				duplicateRowCheckField);			
		}	
		
		for (String cleanedRowCheckField : cleanedRowCheckFields) {
			System.out.println(
				"Check Cleaned Row Field:"+
				cleanedRowCheckField);			
		}	
		
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	@Override
	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {

		//@TODO
	}
	
	@Override
	public void checkErrors() 
		throws RIFServiceException {

		//@TODO
		
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

	
	
}


