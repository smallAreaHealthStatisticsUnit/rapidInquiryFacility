package rifDataLoaderTool.businessConceptLayer;


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

public class OptimiseWorkflowFieldConfiguration {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private DataSet dataSet;
	private String fieldName;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private OptimiseWorkflowFieldConfiguration() {
		DataSet dataSet = DataSet.newInstance();
		
		initialise(
			dataSet, 
			"");
	}

	private OptimiseWorkflowFieldConfiguration(
		final DataSet dataSet,
		final String fieldName) {
				
		initialise(
			dataSet, 
			fieldName);
	}	
	
	private void initialise(
		final DataSet dataSet,
		final String fieldName) {
			
		this.dataSet = dataSet;
		this.fieldName = fieldName;
	}	
	
	public static OptimiseWorkflowFieldConfiguration newInstance() {
		DataSet dataSet = DataSet.newInstance();

		OptimiseWorkflowFieldConfiguration optimiseWorkflowFieldConfiguration
			= new OptimiseWorkflowFieldConfiguration(
				dataSet,
				"");
		return optimiseWorkflowFieldConfiguration;
	}

	public static OptimiseWorkflowFieldConfiguration newInstance(
		final DataSet dataSet,
		final String fieldName) {
		
		OptimiseWorkflowFieldConfiguration optimiseWorkflowFieldConfiguration
			= new OptimiseWorkflowFieldConfiguration(
				dataSet,
				"");
		return optimiseWorkflowFieldConfiguration;
	}
	
	public static OptimiseWorkflowFieldConfiguration createCopy(
		final OptimiseWorkflowFieldConfiguration originalOptimiseWorkflowFieldConfiguration) {
			
			DataSet originalDataSet
				= originalOptimiseWorkflowFieldConfiguration.getDataSet();
			DataSet cloneDataSet
				= DataSet.createCopy(originalDataSet);
						
			OptimiseWorkflowFieldConfiguration cloneOptimiseWorkflowFieldConfiguration
				= new OptimiseWorkflowFieldConfiguration();
			cloneOptimiseWorkflowFieldConfiguration.setDataSet(cloneDataSet);
			
			String originalFieldName
				= originalOptimiseWorkflowFieldConfiguration.getFieldName();
			cloneOptimiseWorkflowFieldConfiguration.setFieldName(originalFieldName);
			
			return cloneOptimiseWorkflowFieldConfiguration;
		}	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public DataSet getDataSet() {
		return dataSet;
	}
	
	public void setDataSet(
		final DataSet dataSet) {

		this.dataSet = dataSet;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
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


