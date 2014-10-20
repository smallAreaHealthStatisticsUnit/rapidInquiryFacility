package rifDataLoaderTool.businessConceptLayer;




import java.util.ArrayList;


import rifDataLoaderTool.system.RIFDataLoaderToolException;
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

public class TableFieldConversionConfiguration 
	extends AbstractRIFDataLoaderToolConcept {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	
	private String coreTableName;
	private String conversionTableFieldName;
	private RIFDataType rifDataType;
		
	// ==========================================
	// Section Construction
	// ==========================================
	
	private TableFieldConversionConfiguration() {
		
	}
	
	public static TableFieldConversionConfiguration newInstance(
		final String coreTableName,
		final String convertedFieldName,
		final RIFDataType rifDataType) {
		
		TableFieldConversionConfiguration fieldConfiguration
			= new TableFieldConversionConfiguration();
		fieldConfiguration.setCoreTableName(coreTableName);
		fieldConfiguration.setConversionTableFieldName(convertedFieldName);
		fieldConfiguration.setRIFDataType(rifDataType);
		
		return fieldConfiguration;
	}

	public static TableFieldConversionConfiguration createCopy(
		final TableFieldConversionConfiguration originalFieldConfiguration) {
		
		TableFieldConversionConfiguration cloneFieldConfiguration
			= new TableFieldConversionConfiguration();
		cloneFieldConfiguration.setConversionTableFieldName(
			originalFieldConfiguration.getConversionTableFieldName());
		cloneFieldConfiguration.setCoreTableName(
			originalFieldConfiguration.getCoreTableName());
		RIFDataType originalRIFDataType
			= originalFieldConfiguration.getRIFDataType();
		RIFDataType cloneRIFDataType = originalRIFDataType.createCopy();
		cloneFieldConfiguration.setRIFDataType(cloneRIFDataType);
		return cloneFieldConfiguration;		
	}
	
	public static ArrayList<TableFieldConversionConfiguration> createCopy(
		final ArrayList<TableFieldConversionConfiguration> originalFieldConfigurations) {
		
		if (originalFieldConfigurations == null) {
			return null;
		}
		
		ArrayList<TableFieldConversionConfiguration> cloneFieldConfigurations
			= new ArrayList<TableFieldConversionConfiguration>();
		for (TableFieldConversionConfiguration originalFieldConfiguration : originalFieldConfigurations) {
			TableFieldConversionConfiguration cloneFieldConfiguration
				= createCopy(originalFieldConfiguration);
			cloneFieldConfigurations.add(cloneFieldConfiguration);
		}
		
		return cloneFieldConfigurations;
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public String getCoreTableName() {
		return coreTableName;
	}

	public void setCoreTableName(String coreTableName) {
		this.coreTableName = coreTableName;
	}

	public String getConversionTableFieldName() {
		return conversionTableFieldName;
	}

	public void setConversionTableFieldName(String conversionTableFieldName) {
		this.conversionTableFieldName = conversionTableFieldName;
	}

	public RIFDataType getRIFDataType() {
		return rifDataType;
	}

	public void setRIFDataType(RIFDataType rifDataType) {
		this.rifDataType = rifDataType;
	}

	// ==========================================
	// Section Errors and Validation
	// ==========================================


	public void checkSecurityViolations() 
		throws RIFServiceSecurityException {

		//@TODO fill in later
		
	}

	public void checkErrors() 
		throws RIFDataLoaderToolException {
		
		//@TODO fill in later
	}

	
	// ==========================================
	// Section Interfaces
	// ==========================================

	//Interface: Display Name
	
	public String getDisplayName() {
		return conversionTableFieldName;
	}
	
	// ==========================================
	// Section Override
	// ==========================================

}


