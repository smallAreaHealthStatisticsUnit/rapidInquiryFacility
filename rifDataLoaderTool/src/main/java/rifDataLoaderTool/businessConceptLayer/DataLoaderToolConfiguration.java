package rifDataLoaderTool.businessConceptLayer;

import rifDataLoaderTool.system.RIFDataLoaderToolError;
import rifDataLoaderTool.system.RIFDataLoaderToolMessages;

import rifGenericLibrary.system.RIFServiceException;

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

public class DataLoaderToolConfiguration {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private RIFDatabaseConnectionParameters databaseConnectionsConfiguration;
	private DLGeographyMetaData geographyMetaData;
	private ArrayList<DLHealthTheme> healthThemes;
	private RIFDataTypeFactory rifDataTypeFactory;
	private ArrayList<DataSetConfiguration> denominatorDataSetConfigurations;
	private ArrayList<DataSetConfiguration> numeratorDataSetConfigurations;
	private ArrayList<DataSetConfiguration> covariateDataSetConfigurations;
	private ConfigurationHints configurationHints;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private DataLoaderToolConfiguration() {	
		databaseConnectionsConfiguration
			= RIFDatabaseConnectionParameters.newInstance();
		geographyMetaData = DLGeographyMetaData.newInstance();

		denominatorDataSetConfigurations = new ArrayList<DataSetConfiguration>();
		numeratorDataSetConfigurations = new ArrayList<DataSetConfiguration>();
		covariateDataSetConfigurations = new ArrayList<DataSetConfiguration>();
		
		rifDataTypeFactory = RIFDataTypeFactory.newInstance();
		rifDataTypeFactory.populateFactoryWithBuiltInTypes();
				
		configurationHints = new ConfigurationHints();
	}

	public static DataLoaderToolConfiguration newInstance() {
		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= new DataLoaderToolConfiguration();
		
		return dataLoaderToolConfiguration;
	}
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void setDatabaseConnectionConfiguration(
		final RIFDatabaseConnectionParameters databaseConnectionsConfiguration) {
		
		this.databaseConnectionsConfiguration = databaseConnectionsConfiguration;		
	}
	
	public RIFDatabaseConnectionParameters getDatabaseConnectionConfiguration() {
		return databaseConnectionsConfiguration;
	}
	
	public void setGeographyMetaData(final DLGeographyMetaData geographyMetaData) {
		this.geographyMetaData = geographyMetaData;
	}
	
	public DLGeographyMetaData getGeographyMetaData() {
		return geographyMetaData;
	}
	
	public void setRIFDataTypeFactory(final RIFDataTypeFactory rifDataTypeFactory) {
		this.rifDataTypeFactory = rifDataTypeFactory;
	}
	
	public RIFDataTypeFactory getRIFDataTypeFactory() {
		return rifDataTypeFactory;
	}
	
	public ConfigurationHints getConfigurationHints() {
		return configurationHints;
	}
	
	public void setConfigurationHints(final ConfigurationHints configurationHints) {
		this.configurationHints = configurationHints;
	}
	
	
	public void setDenominatorDataSetConfigurations(
		final ArrayList<DataSetConfiguration> denominatorDataSetConfigurations) {
		
		this.denominatorDataSetConfigurations = denominatorDataSetConfigurations;	
	}
	
	public void addDenominatorDataSetConfiguration(
		final DataSetConfiguration denominatorDataSetConfiguration) {
		
		denominatorDataSetConfigurations.add(denominatorDataSetConfiguration);
	}
	
	public ArrayList<DataSetConfiguration> getDenominatorDataSetConfigurations() {
		return denominatorDataSetConfigurations;
	}

	
	public void setNumeratorDataSetConfigurations(
		final ArrayList<DataSetConfiguration> numeratorDataSetConfigurations) {
		
		this.numeratorDataSetConfigurations = numeratorDataSetConfigurations;	
	}
	
	public void addNumeratorDataSetConfiguration(
		final DataSetConfiguration numeratorDataSetConfiguration) {
		
		numeratorDataSetConfigurations.add(numeratorDataSetConfiguration);
	}
	
	public ArrayList<DataSetConfiguration> getNumeratorDataSetConfigurations() {
		return numeratorDataSetConfigurations;
	}
	
	public void setCovariateDataSetConfigurations(
		final ArrayList<DataSetConfiguration> covariateDataSetConfigurations) {
		
		this.covariateDataSetConfigurations = covariateDataSetConfigurations;	
	}
	
	public void addCovariateDataSetConfiguration(
		final DataSetConfiguration covariateDataSetConfiguration) {
		
		covariateDataSetConfigurations.add(covariateDataSetConfiguration);
	}
	
	public ArrayList<DataSetConfiguration> getCovariateDataSetConfigurations() {
		return covariateDataSetConfigurations;
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	public void checkHealthThemeNameNotExists(
		final DLHealthTheme candidateHealthTheme) 
		throws RIFServiceException {
		
		String candidateDisplayName = candidateHealthTheme.getDisplayName();
		
		for (DLHealthTheme healthTheme : healthThemes) {
			String currentDisplayName
				= healthTheme.getDisplayName();
			if (currentDisplayName.equals(candidateDisplayName)) {
				String errorMessage
					= RIFDataLoaderToolMessages.getMessage(
						"dlHealthTheme.error.nameAlreadyExists",
						candidateDisplayName);
				RIFServiceException rifServiceException
					= new RIFServiceException(
						RIFDataLoaderToolError.DUPLICATE_HEALTH_THEME_NAME, 
						errorMessage);
				throw rifServiceException;				
			}
		}		
	}
	
	public void checkDataSetNameNotExists(
		final DataSetConfiguration candidateDataSetConfiguration) 
		throws RIFServiceException {
		
		String candidateDisplayName = candidateDataSetConfiguration.getDisplayName();

		String errorMessage
			= RIFDataLoaderToolMessages.getMessage(
				"dataSetConfiguration.error.duplicateName",
				candidateDisplayName);		
		RIFServiceException rifServiceException
			= new RIFServiceException(
				RIFDataLoaderToolError.DUPLICATE_DATA_SET_CONFIGURATION_NAME, 
				errorMessage);

		for (DataSetConfiguration denominatorDataSetConfiguration : denominatorDataSetConfigurations) {
			String currentDisplayName = denominatorDataSetConfiguration.getDisplayName();
			if (currentDisplayName.equals(candidateDisplayName)) {
				throw rifServiceException;
			}			
		}

		for (DataSetConfiguration numeratorDataSetConfiguration : numeratorDataSetConfigurations) {
			String currentDisplayName = numeratorDataSetConfiguration.getDisplayName();
			if (currentDisplayName.equals(candidateDisplayName)) {
				throw rifServiceException;
			}			
		}
		
		for (DataSetConfiguration covariateDataSetConfiguration : covariateDataSetConfigurations) {
			String currentDisplayName = covariateDataSetConfiguration.getDisplayName();
			if (currentDisplayName.equals(candidateDisplayName)) {
				throw rifServiceException;
			}			
		}			
	}

	// ==========================================
	// Section Interfaces
	// ==========================================

	// ==========================================
	// Section Override
	// ==========================================

}


