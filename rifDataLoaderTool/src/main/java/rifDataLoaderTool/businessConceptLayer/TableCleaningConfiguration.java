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

public final class TableCleaningConfiguration 
	extends AbstractRIFDataLoaderToolConcept {

	// ==========================================
	// Section Constants
	// ==========================================
	
	// ==========================================
	// Section Properties
	// ==========================================
	private DataSource dataSource;
	private ArrayList<TableFieldCleaningConfiguration> fieldCleaningConfigurations;
	private String coreTableName;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private TableCleaningConfiguration(
		final String coreTableName) {
		
		fieldCleaningConfigurations = new ArrayList<TableFieldCleaningConfiguration>();
		this.coreTableName = coreTableName;		
	}
	
	public static TableCleaningConfiguration newInstance(
		final String coreTableName) {
		TableCleaningConfiguration tableCleaningConfiguration
			= new TableCleaningConfiguration(coreTableName);
		
		return tableCleaningConfiguration;
	}
	
	public static TableCleaningConfiguration createCopy(
		final TableCleaningConfiguration originalTableConfiguration) {
				
		TableCleaningConfiguration cloneTableConfiguration
			= new TableCleaningConfiguration(originalTableConfiguration.getCoreTableName());
		
		ArrayList<TableFieldCleaningConfiguration> originalFieldConfigurations
			= originalTableConfiguration.getIncludedFieldCleaningConfigurations();
		ArrayList<TableFieldCleaningConfiguration> cloneFieldConfigurations
			= TableFieldCleaningConfiguration.createCopy(originalFieldConfigurations);
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
	
	public int getTableFieldCount() {
		return fieldCleaningConfigurations.size();
	}
	
	public void addTableFieldCleaningConfiguration(
		final TableFieldCleaningConfiguration tableFieldCleaningConfiguration) {
		
		fieldCleaningConfigurations.add(tableFieldCleaningConfiguration);		
	}
	
	public void addFieldConfiguration(
		final String coreTableName,
		final String loadTableFieldName,
		final String cleanedTableFieldName,
		final RIFDataTypeInterface rifDataType) {

		TableFieldCleaningConfiguration	tableFieldCleaningConfiguration
			= TableFieldCleaningConfiguration.newInstance(
				coreTableName,
				loadTableFieldName,
				cleanedTableFieldName,
				rifDataType);

		fieldCleaningConfigurations.add(tableFieldCleaningConfiguration);
	}
	
	public String getCoreTableName() {
		return coreTableName;
	}

	public String[] getLoadTableFieldNames() {
		ArrayList<String> fieldNames = new ArrayList<String>();
		for (TableFieldCleaningConfiguration fieldCleaningConfiguration : fieldCleaningConfigurations) {
			fieldNames.add(fieldCleaningConfiguration.getLoadTableFieldName());
		}
		return fieldNames.toArray(new String[0]);		
	}
	
	public String[] getCleanedTableFieldNames() {
		ArrayList<String> fieldNames = new ArrayList<String>();
		for (TableFieldCleaningConfiguration fieldCleaningConfiguration : fieldCleaningConfigurations) {
			fieldNames.add(fieldCleaningConfiguration.getCleanedTableFieldName());
		}
		return fieldNames.toArray(new String[0]);		
	}
	
	public int getFieldCount() {
		return fieldCleaningConfigurations.size();
	}
	
	public TableFieldCleaningConfiguration getFieldCleaningConfiguration(
		final int index) {
	
		return fieldCleaningConfigurations.get(index);
	}
	
	public ArrayList<TableFieldCleaningConfiguration> getAllFieldCleaningConfigurations() {
		return fieldCleaningConfigurations;
	}
	
	/**
	 * filters set of field configurations so that only those that have been marked to be included in
	 * RIF processing are actually included.  Sometimes a RIF manager may try to import a file
	 * but then discard a number of columns which have no relevance to the RIF
	 * @return
	 */
	public ArrayList<TableFieldCleaningConfiguration> getIncludedFieldCleaningConfigurations() {
		ArrayList<TableFieldCleaningConfiguration> fieldConfigurationsToInclude
			= new ArrayList<TableFieldCleaningConfiguration>();
		for (TableFieldCleaningConfiguration fieldCleaningConfiguration : fieldCleaningConfigurations) {
			if (fieldCleaningConfiguration.includeFieldInRIFprocessing()) {
				fieldConfigurationsToInclude.add(fieldCleaningConfiguration);				
			}
		}
		
		return fieldConfigurationsToInclude;
	}
	
	public void setTableFieldCleaningConfigurations(
		final ArrayList<TableFieldCleaningConfiguration> fieldCleaningConfigurations) {
		
		this.fieldCleaningConfigurations = fieldCleaningConfigurations;
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
		fieldCleaningConfigurations.clear();
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


