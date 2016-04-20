package rifDataLoaderTool.businessConceptLayer;

import java.util.ArrayList;

/**
 *
 *
 * <hr>
 * Copyright 2016 Imperial College London, developed by the Small Area
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

public class DataLoaderToolSettings {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private RIFDatabaseConnectionParameters databaseConnectionParameters;
	private ArrayList<DataLoaderToolGeography> geographies;
	private RIFDataTypeFactory rifDataTypeFactory;
	private ArrayList<LinearWorkflow> workflows;
	private ConfigurationHints configurationHints;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataLoaderToolSettings() {
		
		databaseConnectionParameters
			= RIFDatabaseConnectionParameters.newInstance();
		
		geographies 
			= new ArrayList<DataLoaderToolGeography>();
		rifDataTypeFactory = RIFDataTypeFactory.newInstance();
		rifDataTypeFactory.populateFactoryWithBuiltInTypes();
		workflows = new ArrayList<LinearWorkflow>();
		configurationHints
			= new ConfigurationHints();
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public ArrayList<String> getGeographicalResolutionFields() {
		ArrayList<String> results = new ArrayList<String>();
		for (DataLoaderToolGeography geography : geographies) {
			ArrayList<ShapeFile> shapeFiles
				= geography.getShapeFiles();
			for (ShapeFile shapeFile : shapeFiles) {
				String geographicalResolutionName
					= shapeFile.getDatabaseFieldName();
				results.add(geographicalResolutionName);
			}
		}
		return results;		
	}
	
	public RIFDatabaseConnectionParameters getDatabaseConnectionParameters() {		
		return databaseConnectionParameters;
	}
	
	public void setDatabaseConnectionParameters(final RIFDatabaseConnectionParameters databaseConnectionParameters) {
		this.databaseConnectionParameters = databaseConnectionParameters;
	}
	
	public ArrayList<DataLoaderToolGeography> getGeographies() {
		return geographies;
	}
	
	public void setGeographies(final ArrayList<DataLoaderToolGeography> geographies) {
		this.geographies = geographies;
	}
	
	public RIFDataTypeFactory getRIFDataTypeFactory() {
		return rifDataTypeFactory;
	}
	
	public void setRIFDataTypeFactory(final RIFDataTypeFactory rifDataTypeFactory) {
		this.rifDataTypeFactory = rifDataTypeFactory;
	}
	
	public ArrayList<LinearWorkflow> getWorkflows() {
		return workflows;
	}
	
	public void setWorkflows(final ArrayList<LinearWorkflow> workflows) {
		this.workflows = workflows;
	}
	
	public boolean areDatabaseConnectionSettingsValid() {
		return true;		
	}
	
	public boolean areGeographiesValid() {
		if (geographies.isEmpty() == false) {
			return true;
		}
		
		return false;
	}
		
	public boolean areDataTypesValid() {
		ArrayList<RIFDataType> rifDataTypes
			= rifDataTypeFactory.getRegisteredDataTypes();
		if (rifDataTypes.isEmpty() == false) {
			return true;
		}
		
		return false;
	}
	
	public ConfigurationHints getConfigurationHints() {
		return configurationHints;		
	}
		
	public void setConfigurationHints(final ConfigurationHints configurationHints) {
		this.configurationHints = configurationHints;
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


