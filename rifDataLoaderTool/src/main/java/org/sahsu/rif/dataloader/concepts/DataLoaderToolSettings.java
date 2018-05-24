package org.sahsu.rif.dataloader.concepts;

import java.util.ArrayList;

/**
 * This is the business class that embodies all the configuration options and
 * work flows that are needed by the Data Loader Tool to process data that
 * are destined to populate the RIF production database.
 * 
 * <p>
 * The class covers five main areas:
 * <ul>
 * <li>
 * <b>database connection parameters</b>: these are used to help the data
 * loader tool create and connect with a temporary database that it will use
 * to process data sets
 * </li>
 * <li>
 * <b>geographies</b>: these describe the geospatial data sets that provide
 * the RIF with maps made at different resolutions (eg: in the UK they may
 * be ward, region, district)
 * </li>
 * <li>
 * <b>data type definitions</b>: these don't define data types as they would
 * appear in a database.  Instead, a data type defines a cleaning and validation
 * policy which would be used to transform a data field from how it would appear
 * in an original data set to how it would appear in a data set that was ready
 * to be loaded into the RIF production database
 * </li>
 * <li>
 * <b>work flows</b>: a workflow describes a collection of settings that will
 * transforma a data set so that it may be ready to be loaded into the RIF
 * production database.
 * </li>
 * <li>
 * <b>configuration hints</b>: are used to link patterns found in the names and
 * field names of CSV files with sets of configuration options.  When the name
 * of a file or a file field matches a hint, a set of configuration settings are
 * applied.  Hints are used to help reduce the amount of manual labour that a
 * user needs to do to configure a lot of file fields.
 * </li>
 * </ul>
 * </p>
 * 
 * <hr>
 * Copyright 2017 Imperial College London, developed by the Small Area
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
	private DatabaseConnectionsConfiguration databaseConnectionParameters;
	private ArrayList<Geography> geographies;
	private RIFDataTypeFactory rifDataTypeFactory;
	private ArrayList<LinearWorkflow> workflows;
	private ConfigurationHints configurationHints;
	
	// ==========================================
	// Section Construction
	// ==========================================

	public DataLoaderToolSettings() {
		
		databaseConnectionParameters
			= DatabaseConnectionsConfiguration.newInstance();
		
		geographies 
			= new ArrayList<Geography>();
		rifDataTypeFactory = RIFDataTypeFactory.newInstance();
		rifDataTypeFactory.populateFactoryWithBuiltInTypes();
		workflows = new ArrayList<LinearWorkflow>();
		configurationHints
			= new ConfigurationHints();
	}

	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	/**
	 * A convenience routine which generates the names of geographical
	 * resolutions.  The method is used to let users map fields they
	 * mark as being geographical resolution 
	 * (see {@link rifDataLoaderTool.businessConceptLayer.FieldPurpose}).
	 */
	/*
	public ArrayList<String> getGeographicalResolutionFields() {
		ArrayList<String> results = new ArrayList<String>();
		for (DLGeography geography : geographies) {
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
	*/
	
	public DatabaseConnectionsConfiguration getDatabaseConnectionParameters() {		
		return databaseConnectionParameters;
	}
	
	public void setDatabaseConnectionParameters(final DatabaseConnectionsConfiguration databaseConnectionParameters) {
		this.databaseConnectionParameters = databaseConnectionParameters;
	}
	
	public ArrayList<Geography> getGeographies() {
		return geographies;
	}
	
	public void setGeographies(final ArrayList<Geography> geographies) {
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


