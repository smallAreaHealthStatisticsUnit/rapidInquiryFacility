package org.sahsu.rif.dataloader.concepts;

import java.util.ArrayList;

import org.sahsu.rif.dataloader.system.RIFDataLoaderToolError;
import org.sahsu.rif.dataloader.system.RIFDataLoaderToolMessages;
import org.sahsu.rif.generic.system.RIFServiceException;


/**
 *
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

public class DataLoaderToolConfiguration {

	// ==========================================
	// Section Constants
	// ==========================================

	// ==========================================
	// Section Properties
	// ==========================================
	private DatabaseConnectionsConfiguration databaseConnections;
	private GeographyMetaData geographyMetaData;
	private ArrayList<HealthTheme> healthThemes;
	private RIFDataTypeFactory rifDataTypeFactory;
	private ArrayList<DataSetConfiguration> denominators;
	private ArrayList<DataSetConfiguration> numerators;
	private ArrayList<DataSetConfiguration> covariates;
	private ConfigurationHints configurationHints;
	
	// ==========================================
	// Section Construction
	// ==========================================

	private DataLoaderToolConfiguration() {	
		databaseConnections
			= RIFDataLoaderToolStartupProperties.createStartupDBConfiguration();

		
		geographyMetaData = GeographyMetaData.newInstance();

		healthThemes = new ArrayList<HealthTheme>();
		
		denominators = new ArrayList<DataSetConfiguration>();
		numerators = new ArrayList<DataSetConfiguration>();
		covariates = new ArrayList<DataSetConfiguration>();
		
		rifDataTypeFactory = RIFDataTypeFactory.newInstance();
		rifDataTypeFactory.populateFactoryWithBuiltInTypes();
				
		configurationHints = new ConfigurationHints();
	}

	public static DataLoaderToolConfiguration newInstance() {
		DataLoaderToolConfiguration dataLoaderToolConfiguration
			= new DataLoaderToolConfiguration();
		
		return dataLoaderToolConfiguration;
	}
	
	public boolean hasIdenticalContents(
		final DataLoaderToolConfiguration otherDataLoaderToolConfiguration) {
		
		if (otherDataLoaderToolConfiguration == null) {
			return false;
		}
		
		DatabaseConnectionsConfiguration otherDatabaseConnectionsConfiguration
			= otherDataLoaderToolConfiguration.getDatabaseConnectionConfiguration();
		if (databaseConnections.hasIdenticalContents(
			otherDatabaseConnectionsConfiguration) == false) {
			System.out.println("Other DB connections is DIFFERENT");
			return false;
		}
		
		GeographyMetaData otherGeographyMetaData
			= otherDataLoaderToolConfiguration.getGeographyMetaData();		
		if (geographyMetaData.hasIdenticalContents(otherGeographyMetaData) == false) {
			System.out.println("Other geography meta data is DIFFERENT");
			return false;
		}

		ArrayList<HealthTheme> otherHealthThemes
			= otherDataLoaderToolConfiguration.getHealthThemes();		
		if (HealthTheme.hasIdenticalContents(healthThemes, otherHealthThemes) == false) {
			System.out.println("Other health themes is DIFFERENT");
			return false;
		}

		ArrayList<RIFDataType> rifDataTypes
			= rifDataTypeFactory.getRegisteredDataTypes();
		ArrayList<RIFDataType> otherRIFDataTypes
			= otherDataLoaderToolConfiguration.getRIFDataTypeFactory().getRegisteredDataTypes();
		
		if (RIFDataType.hasIdenticalContents(rifDataTypes, otherRIFDataTypes) == false) {
			System.out.println("Other rif data types collection is different");
			return false;
		}
		
		ArrayList<DataSetConfiguration> otherDenominatorDataSets
			= otherDataLoaderToolConfiguration.getDenominatorDataSetConfigurations();
		if (DataSetConfiguration.hasIdenticalContents(
			denominators, 
			otherDenominatorDataSets) == false) {
			
			System.out.println("Other denominators changed");
			return false;
		}

		ArrayList<DataSetConfiguration> otherNumeratorDataSets
			= otherDataLoaderToolConfiguration.getNumeratorDataSetConfigurations();
		if (DataSetConfiguration.hasIdenticalContents(
			numerators, 
			otherNumeratorDataSets) == false) {
				
			System.out.println("Other numerators changed");
			return false;
		}
		

		ArrayList<DataSetConfiguration> otherCovariateDataSets
			= otherDataLoaderToolConfiguration.getCovariateDataSetConfigurations();
		if (DataSetConfiguration.hasIdenticalContents(
			covariates, 
			otherCovariateDataSets) == false) {
				
			System.out.println("Other covariates changed");
			return false;
		}
		
		ConfigurationHints otherConfigurationHints
			= otherDataLoaderToolConfiguration.getConfigurationHints();
		if (configurationHints.hasIdenticalContents(otherConfigurationHints) == false) {
			return false;
		}
		
		
		
		return true;
	}
	
	
	// ==========================================
	// Section Accessors and Mutators
	// ==========================================

	public void setDatabaseConnectionConfiguration(
		final DatabaseConnectionsConfiguration databaseConnections) {
		
		this.databaseConnections = databaseConnections;		
	}
	
	public DatabaseConnectionsConfiguration getDatabaseConnectionConfiguration() {
		return databaseConnections;
	}
	
	public void setGeographyMetaData(final GeographyMetaData geographyMetaData) {
		this.geographyMetaData = geographyMetaData;
	}
	
	public GeographyMetaData getGeographyMetaData() {
		return geographyMetaData;
	}
	
	public ArrayList<HealthTheme> getHealthThemes() {
		return healthThemes;
	}
	
	public String[] getHealthThemeNames() {
		ArrayList<String> healthThemeNames = new ArrayList<String>();
		for (HealthTheme healthTheme : healthThemes) {
			healthThemeNames.add(healthTheme.getDisplayName());
		}		
		return healthThemeNames.toArray(new String[0]);
	}
	
	public HealthTheme getHealthTheme(final String targetHealthThemeName) {
		for (HealthTheme healthTheme : healthThemes) {
			String currentHealthThemeName
				= healthTheme.getDisplayName();
			if (currentHealthThemeName.equals(targetHealthThemeName)) {
				return healthTheme;
			}
		}		
		return null;
	}
	
	public void setHealthThemes(final ArrayList<HealthTheme> healthThemes) {
		this.healthThemes = healthThemes;
	}
	
	public void addHealthTheme(final HealthTheme healthTheme) {
		healthThemes.add(healthTheme);
	}
	
	public void updateHealthTheme(
		final HealthTheme originalHealthTheme,
		final HealthTheme revisedHealthTheme) {
		
		HealthTheme.copyInto(revisedHealthTheme, originalHealthTheme);
	}
	
	public void deleteHealthTheme(final HealthTheme healthTheme) {
		healthThemes.remove(healthTheme);
	}
	
	public int getNumberOfHealthThemes() {
		return healthThemes.size();
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
	
	public ArrayList<DataSetConfiguration> getAllDataSetConfigurations() {
		
		ArrayList<DataSetConfiguration> allDataSetConfigurations
			= new ArrayList<DataSetConfiguration>();
		allDataSetConfigurations.addAll(denominators);
		allDataSetConfigurations.addAll(numerators);
		allDataSetConfigurations.addAll(covariates);
		
		return allDataSetConfigurations;
	}
	
	public void setDenominatorDataSetConfigurations(
		final ArrayList<DataSetConfiguration> denominators) {
		
		this.denominators = denominators;	
	}
	
	public void addDenominatorDataSetConfiguration(
		final DataSetConfiguration denominator) {
		
		denominators.add(denominator);
	}
	
	public void updateDenominator(
		final DataSetConfiguration originalDenominator,
		final DataSetConfiguration revisedDenominator) {
		
		DataSetConfiguration.copyInto(revisedDenominator, originalDenominator);
	}
	
	
	public void deleteDenominatorDataSetConfiguration(
		final DataSetConfiguration denominatorToDelete) {
	
		denominators.remove(denominatorToDelete);
	}		

	public ArrayList<DataSetConfiguration> getDenominatorDataSetConfigurations() {
		return denominators;
	}
	
	public String[] getDenominatorNames() {
		ArrayList<String> denominatorNames = new ArrayList<String>();
		for (DataSetConfiguration denominator : denominators) {
			denominatorNames.add(denominator.getDisplayName());
		}

		return denominatorNames.toArray(new String[0]);
	}
	
	public DataSetConfiguration getDenominator(final String denominatorName) {
		for (DataSetConfiguration denominator : denominators) {
			String currentDenominatorName
				= denominator.getDisplayName();
			if (currentDenominatorName.equals(denominatorName)) {
				return denominator;
			}
		}
		return null;		
	}

	public int getNumberOfDenominators() {
		return denominators.size();
	}
	
	public void setNumeratorDataSetConfigurations(
		final ArrayList<DataSetConfiguration> numerators) {
		
		this.numerators = numerators;	
	}
	
	public void addNumeratorDataSetConfiguration(
		final DataSetConfiguration numerator) {
		
		numerators.add(numerator);
	}

	public void updateNumerator(
		final DataSetConfiguration originalNumerator,
		final DataSetConfiguration revisedNumerator) {
		
		DataSetConfiguration.copyInto(revisedNumerator, originalNumerator);
	}
	
	public void deleteNumeratorDataSetConfiguration(
		final DataSetConfiguration numeratorToDelete) {
		
		numerators.remove(numeratorToDelete);
	}		
	
	public ArrayList<DataSetConfiguration> getNumeratorDataSetConfigurations() {
		return numerators;
	}
	
	public int getNumberOfNumerators() {
		return numerators.size();
	}
	
	public void setCovariateDataSetConfigurations(
		final ArrayList<DataSetConfiguration> covariates) {
		
		this.covariates = covariates;	
	}
	
	public void addCovariateDataSetConfiguration(
		final DataSetConfiguration covariate) {
		
		covariates.add(covariate);
	}

	public void updateCovariate(
		final DataSetConfiguration originalCovariate,
		final DataSetConfiguration revisedCovariate) {
		
		DataSetConfiguration.copyInto(revisedCovariate, originalCovariate);
	}
	
	
	public void deleteCovariateDataSetConfiguration(
		final DataSetConfiguration covariateToDelete) {
		
		covariates.remove(covariateToDelete);
	}		

	
	public ArrayList<DataSetConfiguration> getCovariateDataSetConfigurations() {
		return covariates;
	}

	public int getNumberOfCovariates() {
		return covariates.size();
	}
	
	// ==========================================
	// Section Errors and Validation
	// ==========================================
	public void checkHealthThemeNameNotExists(
		final HealthTheme candidateHealthTheme) 
		throws RIFServiceException {
		
		String candidateDisplayName = candidateHealthTheme.getDisplayName();
		
		for (HealthTheme healthTheme : healthThemes) {
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

		for (DataSetConfiguration denominatorDataSetConfiguration : denominators) {
			String currentDisplayName = denominatorDataSetConfiguration.getDisplayName();
			if (currentDisplayName.equals(candidateDisplayName)) {
				throw rifServiceException;
			}			
		}

		for (DataSetConfiguration numeratorDataSetConfiguration : numerators) {
			String currentDisplayName = numeratorDataSetConfiguration.getDisplayName();
			if (currentDisplayName.equals(candidateDisplayName)) {
				throw rifServiceException;
			}			
		}
		
		for (DataSetConfiguration covariateDataSetConfiguration : covariates) {
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


