package rifDataLoaderTool.businessConceptLayer;

import java.util.ArrayList;
import java.util.HashMap;

import rifDataLoaderTool.system.RIFTemporaryTablePrefixes;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceSecurityException;


/**
 * Provides a description of a conversion activity in the RIF that can be used
 * by SQL code generators to produce code for different database targets such as
 * Postgresql and Microsoft SQL Server.  In a table conversion configuration, the fields
 * from a cleaned table (one that has been transformed by cleaning rules, evaluated by
 * validation features and cast to database types) are mapped to fields which are expected
 * in various parts of the RIF schema.  Typically one field in the cleaned table will 
 * map to one field in the converted field.  However, some RIF fields such as age-sex group
 * may require a function to combine separate age and sex fields, or derive the age field
 * from birth date and event date column values.  
 * 
 * <p>
 * Each conversion configuration will specify two important kinds of fields:
 * <ul>
 * <li>
 * required fields
 * </li>
 * <li>
 * optional fields
 * </li>
 * </ul>
 *
 *<p>
 * Conversion is an activity where some fields in cleaned table must map to expected fields, whereas
 * some conversion activities may have extra fields which are retained in RIF tables but which are not
 * used by the RIF tool suite.  For example, suppose RIF managers are converting a cleaned table to 
 * a health code table.  They will need to map one field to the required field "label" and another one
 * to a required field called "description".  The conversion activity won't need extra fields.
 * <p>
 * In another example, a numerator table will feature required fields and may also include extra fields. 
 * </p>
 *
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

public final class OptimiseWorkflowConfiguration {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private DataSet dataSet;

	private ArrayList<OptimiseWorkflowFieldConfiguration> fieldConfigurations;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private OptimiseWorkflowConfiguration(
		final DataSet dataSet) {
		
		fieldConfigurations = new ArrayList<OptimiseWorkflowFieldConfiguration>();

	}

	public static OptimiseWorkflowConfiguration newInstance() {

		DataSet dataSet = DataSet.newInstance();
		OptimiseWorkflowConfiguration convertWorkflowFieldConfiguration
			= new OptimiseWorkflowConfiguration(dataSet);
			
		return convertWorkflowFieldConfiguration;
	}	
	
	public static OptimiseWorkflowConfiguration newInstance(
		final DataSet dataSet) {
		OptimiseWorkflowConfiguration convertWorkflowFieldConfiguration
			= new OptimiseWorkflowConfiguration(dataSet);
		
		return convertWorkflowFieldConfiguration;
	}
	
	public static OptimiseWorkflowConfiguration createCopy(
		final OptimiseWorkflowConfiguration originalConversionConfiguration) {
		
		DataSet originalDataSet = originalConversionConfiguration.getDataSet();
		DataSet clonedDataSet = DataSet.createCopy(originalDataSet);
		
		OptimiseWorkflowConfiguration cloneConvertWorkflowConfiguration
			= OptimiseWorkflowConfiguration.newInstance(clonedDataSet);
			
		//first, make clones of conversion configurations
		ArrayList<OptimiseWorkflowFieldConfiguration> originalFieldConfigurations
			= originalConversionConfiguration.getFieldConfigurations();
		for (OptimiseWorkflowFieldConfiguration originalFieldConfiguration : originalFieldConfigurations) {
						
			//clone conversion fields
			OptimiseWorkflowFieldConfiguration cloneFieldConfiguration
				= OptimiseWorkflowFieldConfiguration.createCopy(
					originalFieldConfiguration);
			cloneConvertWorkflowConfiguration.addFieldConfiguration(cloneFieldConfiguration);
		}
		
		return cloneConvertWorkflowConfiguration;		
	}
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public String getCoreDataSetName() {
		return dataSet.getCoreDataSetName();
	}
	
	public DataSet getDataSet() {
		return dataSet;
	}
	
	public void setDataSet(final DataSet dataSet) {
		this.dataSet = dataSet;
		
		for (OptimiseWorkflowFieldConfiguration fieldConfiguration : fieldConfigurations) {
			fieldConfiguration.setDataSet(dataSet);			
		}		
	}
	
	public void addFieldConfiguration(
		final OptimiseWorkflowFieldConfiguration fieldConfiguration) {
				
		fieldConfigurations.add(fieldConfiguration);		
	}

	public ArrayList<OptimiseWorkflowFieldConfiguration> getFieldConfigurations() {
		
		return fieldConfigurations;
	}
	
		
	// ==========================================
	// Section Errors and Validation
	// ==========================================

	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {

		//@TODO fill in later
		
	}

	public void checkErrors() 
		throws RIFServiceException {
		
		//@TODO fill in later
	}
	
	public String getDisplayName() {
		String tableName
			= RIFTemporaryTablePrefixes.OPTIMISE.getTableName(
				dataSet.getCoreDataSetName());
		return tableName;
	}
	
	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

}


