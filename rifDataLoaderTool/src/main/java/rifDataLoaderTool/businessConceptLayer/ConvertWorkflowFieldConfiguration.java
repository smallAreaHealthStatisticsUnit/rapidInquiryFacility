package rifDataLoaderTool.businessConceptLayer;




import java.util.ArrayList;



import rifDataLoaderTool.businessConceptLayer.rifDataTypes.RIFDataTypeInterface;
import rifServices.system.RIFServiceException;
import rifServices.system.RIFServiceSecurityException;

/**
 * Describes the expected name and data type of a field in some RIF table.  The fields
 * may either describe parts of existing tables or indicate what fields must appear in
 * a new published table.
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

public final class ConvertWorkflowFieldConfiguration 
	extends AbstractRIFDataLoaderToolConcept {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private DataSet dataSet;
	private String cleanFieldName;
	private String convertFieldName;
	private RIFDataTypeInterface rifDataType;
		
	// ==========================================
	// Section Construction
	// ==========================================
	
	private ConvertWorkflowFieldConfiguration() {
		
	}
	
	public static ConvertWorkflowFieldConfiguration newInstance(
		final DataSet dataSet,
		final String cleanFieldName,
		final String convertFieldName,
		final RIFDataTypeInterface rifDataType) {
		
		ConvertWorkflowFieldConfiguration fieldConfiguration
			= new ConvertWorkflowFieldConfiguration();
		fieldConfiguration.setDataSet(dataSet);
		fieldConfiguration.setCleanFieldName(cleanFieldName);
		fieldConfiguration.setConvertFieldName(convertFieldName);
		fieldConfiguration.setRIFDataType(rifDataType);
		
		return fieldConfiguration;
	}

	public static ConvertWorkflowFieldConfiguration createCopy(
		final ConvertWorkflowFieldConfiguration originalFieldConfiguration) {
		
		DataSet originalDataSet = originalFieldConfiguration.getDataSet();
		DataSet cloneDataSet = DataSet.createCopy(originalDataSet);
		
		ConvertWorkflowFieldConfiguration cloneFieldConfiguration
			= new ConvertWorkflowFieldConfiguration();
		cloneFieldConfiguration.setConvertFieldName(
			originalFieldConfiguration.getConvertFieldName());
		cloneFieldConfiguration.setDataSet(cloneDataSet);
		
		RIFDataTypeInterface originalRIFDataType
			= originalFieldConfiguration.getRIFDataType();
		RIFDataTypeInterface cloneRIFDataType = originalRIFDataType.createCopy();
		cloneFieldConfiguration.setRIFDataType(cloneRIFDataType);
		return cloneFieldConfiguration;		
	}
	
	public static ArrayList<ConvertWorkflowFieldConfiguration> createCopy(
		final ArrayList<ConvertWorkflowFieldConfiguration> originalFieldConfigurations) {
		
		if (originalFieldConfigurations == null) {
			return null;
		}
		
		ArrayList<ConvertWorkflowFieldConfiguration> cloneFieldConfigurations
			= new ArrayList<ConvertWorkflowFieldConfiguration>();
		for (ConvertWorkflowFieldConfiguration originalFieldConfiguration : originalFieldConfigurations) {
			ConvertWorkflowFieldConfiguration cloneFieldConfiguration
				= createCopy(originalFieldConfiguration);
			cloneFieldConfigurations.add(cloneFieldConfiguration);
		}
		
		return cloneFieldConfigurations;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public String getConvertFieldName() {
		return convertFieldName;
	}

	public void setConvertFieldName(
		final String convertFieldName) {
		this.convertFieldName = convertFieldName;
	}
	
	public String getCleanFieldName() {
		return cleanFieldName;
	}
	
	public void setCleanFieldName(
		final String cleanFieldName) {
		
		this.cleanFieldName = cleanFieldName;
	}
	
	public RIFDataTypeInterface getRIFDataType() {
		return rifDataType;
	}

	public void setRIFDataType(
		final RIFDataTypeInterface rifDataType) {

		this.rifDataType = rifDataType;
	}

	public DataSet getDataSet() {
		return dataSet;
	}
	
	public void setDataSet(final DataSet dataSet) {
		this.dataSet = dataSet;
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

	
	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: Display Name
	
	public String getDisplayName() {
		return convertFieldName;
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


