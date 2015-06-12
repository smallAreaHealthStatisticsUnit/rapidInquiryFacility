package rifDataLoaderTool.businessConceptLayer;

import java.util.ArrayList;

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

public final class CleanWorkflowConfiguration 
	extends AbstractRIFDataLoaderToolConcept {

	// ==========================================
	// Section Constants
	// ==========================================
	
	// ==========================================
	// Section Properties
	// ==========================================
	private DataSource dataSource;
	private ArrayList<CleanWorkflowFieldConfiguration> cleanWorkflowFieldConfigurations;
	private String coreTableName;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private CleanWorkflowConfiguration() {
		
		cleanWorkflowFieldConfigurations 
			= new ArrayList<CleanWorkflowFieldConfiguration>();	
	}
	
	public static CleanWorkflowConfiguration newInstance(
		final String coreTableName) {
		CleanWorkflowConfiguration cleanWorkflowConfiguration
			= new CleanWorkflowConfiguration();
		cleanWorkflowConfiguration.setCoreTableName(coreTableName);
		
		return cleanWorkflowConfiguration;
	}
	
	public static CleanWorkflowConfiguration newInstance() {
		CleanWorkflowConfiguration cleanWorkflowConfiguration
			= new CleanWorkflowConfiguration();
		
		return cleanWorkflowConfiguration;
	}	
	
	public static CleanWorkflowConfiguration createCopy(
		final CleanWorkflowConfiguration originalTableConfiguration) {
				
		CleanWorkflowConfiguration cloneTableConfiguration
			= new CleanWorkflowConfiguration();
		cloneTableConfiguration.setCoreTableName(originalTableConfiguration.getCoreTableName());
		ArrayList<CleanWorkflowFieldConfiguration> originalFieldConfigurations
			= originalTableConfiguration.getIncludedFieldCleaningConfigurations();
		ArrayList<CleanWorkflowFieldConfiguration> cloneFieldConfigurations
			= CleanWorkflowFieldConfiguration.createCopy(originalFieldConfigurations);
		cloneTableConfiguration.setTableFieldCleaningConfigurations(cloneFieldConfigurations);
		
		
		DataSource originalDataSource
			= originalTableConfiguration.getDataSource();
		DataSource cloneDataSource
			= DataSource.createCopy(originalDataSource);
		cloneTableConfiguration.setDataSource(cloneDataSource);
		return cloneTableConfiguration;
	}
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================
	
	public void addCleanWorkflowFieldConfiguration(
		final CleanWorkflowFieldConfiguration cleanWorkflowFieldConfiguration) {	
		cleanWorkflowFieldConfigurations.add(cleanWorkflowFieldConfiguration);		
	}
	
	public void addFieldConfiguration(
		final String coreTableName,
		final String loadTableFieldName,
		final String cleanedTableFieldName,
		final RIFDataTypeInterface rifDataType) {

		CleanWorkflowFieldConfiguration	tableFieldCleaningConfiguration
			= CleanWorkflowFieldConfiguration.newInstance(
				coreTableName,
				loadTableFieldName,
				cleanedTableFieldName,
				rifDataType);

		cleanWorkflowFieldConfigurations.add(tableFieldCleaningConfiguration);
	}
	
	public String getCoreTableName() {
		return coreTableName;
	}

	public void setCoreTableName(final String coreTableName) {
		this.coreTableName = coreTableName;
	}
	
	public String[] getLoadFieldNames() {
		ArrayList<String> fieldNames = new ArrayList<String>();
		for (CleanWorkflowFieldConfiguration fieldCleaningConfiguration : cleanWorkflowFieldConfigurations) {
			fieldNames.add(fieldCleaningConfiguration.getLoadTableFieldName());
		}
		return fieldNames.toArray(new String[0]);		
	}
	
	public String[] getCleanFieldNames() {
		ArrayList<String> fieldNames = new ArrayList<String>();
		for (CleanWorkflowFieldConfiguration fieldCleaningConfiguration : cleanWorkflowFieldConfigurations) {
			fieldNames.add(fieldCleaningConfiguration.getCleanedTableFieldName());
		}
		return fieldNames.toArray(new String[0]);		
	}
	
	public int getFieldCount() {
		return cleanWorkflowFieldConfigurations.size();
	}
	
	public CleanWorkflowFieldConfiguration getFieldCleaningConfiguration(
		final int index) {
	
		return cleanWorkflowFieldConfigurations.get(index);
	}
	
	public ArrayList<CleanWorkflowFieldConfiguration> getAllFieldCleaningConfigurations() {
		return cleanWorkflowFieldConfigurations;
	}
	
	/**
	 * filters set of field configurations so that only those that have been marked to be included in
	 * RIF processing are actually included.  Sometimes a RIF manager may try to import a file
	 * but then discard a number of columns which have no relevance to the RIF
	 * @return
	 */
	public ArrayList<CleanWorkflowFieldConfiguration> getIncludedFieldCleaningConfigurations() {
		ArrayList<CleanWorkflowFieldConfiguration> fieldConfigurationsToInclude
			= new ArrayList<CleanWorkflowFieldConfiguration>();
		for (CleanWorkflowFieldConfiguration fieldCleaningConfiguration : cleanWorkflowFieldConfigurations) {
			if (fieldCleaningConfiguration.includeFieldInRIFprocessing()) {
				fieldConfigurationsToInclude.add(fieldCleaningConfiguration);				
			}
		}
		
		return fieldConfigurationsToInclude;
	}
	
	public void setTableFieldCleaningConfigurations(
		final ArrayList<CleanWorkflowFieldConfiguration> fieldCleaningConfigurations) {
		
		this.cleanWorkflowFieldConfigurations = fieldCleaningConfigurations;
	}
	
	@Override
	public String getDisplayName() {
		return coreTableName;
	}
	
	public void setDataSource(
		final DataSource dataSource) {
		
		this.dataSource = dataSource;
	}
	
	public DataSource getDataSource() {
		
		return dataSource;
	}
	
	public void clearFieldConfigurations() {
		cleanWorkflowFieldConfigurations.clear();
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


